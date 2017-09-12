package com.jeecms.cms.action.member;

import static com.jeecms.cms.Constants.TPLDIR_MEMBER;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.jeecms.common.image.ImageScale;
import com.jeecms.common.image.ImageUtils;
import com.jeecms.common.upload.FileRepository;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.entity.MarkConfig;
import com.jeecms.core.entity.MemberConfig;
import com.jeecms.core.manager.DbFileMng;
import com.jeecms.core.web.WebCoreErrors;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.util.FrontUtils;

@Controller
public class ImageUploadAct {
	private static final Logger log = LoggerFactory
			.getLogger(ImageUploadAct.class);
	/**
	 * 用户头像路径
	 */
	private static final String USER_IMG_PATH = "/user/images";
	/**
	 * 结果页
	 */
	private static final String RESULT_PAGE = "tpl.iframe_upload";
	/**
	 * 错误信息参数
	 */
	public static final String ERROR = "error";

	@RequestMapping("/member/o_upload_image.jspx")
	public String execute(
			String filename,
			Integer uploadNum,
			Boolean mark,
			@RequestParam(value = "uploadFile", required = false) MultipartFile file,
			HttpServletRequest request, ModelMap model) {
		WebCoreErrors errors = validate(filename, file, request);
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		FrontUtils.frontData(request, model, site);
		MemberConfig mcfg = site.getConfig().getMemberConfig();
		if (!mcfg.isMemberOn()) {
			return FrontUtils.showMessage(request, model, "member.memberClose");
		}
		if (user == null) {
			return FrontUtils.showLogin(request, model, site);
		}
		if (errors.hasErrors()) {
			model.addAttribute(ERROR, errors.getErrors().get(0));
			return FrontUtils.getTplPath(request, site.getSolutionPath(),
					TPLDIR_MEMBER, RESULT_PAGE);
		}
		MarkConfig conf = site.getConfig().getMarkConfig();
		if (mark == null) {
			mark = conf.getOn();
		}

		String origName = file.getOriginalFilename();
		String ext = FilenameUtils.getExtension(origName).toLowerCase(
				Locale.ENGLISH);
		try {
			String fileUrl;
			if (site.getConfig().getUploadToDb()) {
				String dbFilePath = site.getConfig().getDbFileUri();
				if (!StringUtils.isBlank(filename)) {
					filename = filename.substring(dbFilePath.length());
					if (mark) {
						File tempFile = mark(file, conf);
						fileUrl = dbFileMng.storeByFilename(filename,
								new FileInputStream(tempFile));
						tempFile.delete();
					} else {
						fileUrl = dbFileMng.storeByFilename(filename, file
								.getInputStream());
					}
				} else {
					if (mark) {
						File tempFile = mark(file, conf);
						fileUrl = dbFileMng.storeByExt(site.getUploadPath(),
								ext, new FileInputStream(tempFile));
						tempFile.delete();
					} else {
						fileUrl = dbFileMng.storeByExt(site.getUploadPath(),
								ext, file.getInputStream());
					}
					// 加上访问地址
					fileUrl = request.getContextPath() + dbFilePath + fileUrl;
				}
			} else if (site.getUploadFtp() != null) {
				Ftp ftp = site.getUploadFtp();
				String ftpUrl = ftp.getUrl();
				if (!StringUtils.isBlank(filename)) {
					filename = filename.substring(ftpUrl.length());
					if (mark) {
						File tempFile = mark(file, conf);
						fileUrl = ftp.storeByFilename(filename,
								new FileInputStream(tempFile));
						tempFile.delete();
					} else {
						fileUrl = ftp.storeByFilename(filename, file
								.getInputStream());
					}
				} else {
					if (mark) {
						File tempFile = mark(file, conf);
						fileUrl = ftp.storeByExt(site.getUploadPath(), ext,
								new FileInputStream(tempFile));
						tempFile.delete();
					} else {
						fileUrl = ftp.storeByExt(site.getUploadPath(), ext,
								file.getInputStream());
					}
					// 加上url前缀
					fileUrl = ftpUrl + fileUrl;
				}
			} else {
				String ctx = request.getContextPath();
				if (!StringUtils.isBlank(filename)) {
					filename = filename.substring(ctx.length());
					if (mark) {
						File tempFile = mark(file, conf);
						fileUrl = fileRepository.storeByFilename(filename,
								tempFile);
						tempFile.delete();
					} else {
						fileUrl = fileRepository
								.storeByFilename(filename, file);
					}
				} else {
					if (mark) {
						File tempFile = mark(file, conf);
						fileUrl = fileRepository.storeByExt(USER_IMG_PATH, ext, tempFile);
						tempFile.delete();
					} else {
						fileUrl = fileRepository.storeByExt(USER_IMG_PATH, ext, file);
					}
					// 加上部署路径
					fileUrl = ctx + fileUrl;
				}
			}
			model.addAttribute("uploadPath", fileUrl);
			model.addAttribute("uploadNum", uploadNum);
		} catch (IllegalStateException e) {
			model.addAttribute(ERROR, e.getMessage());
			log.error("upload file error!", e);
		} catch (IOException e) {
			model.addAttribute(ERROR, e.getMessage());
			log.error("upload file error!", e);
		} catch (Exception e) {
			model.addAttribute(ERROR, e.getMessage());
			log.error("upload file error!", e);
		}
		return FrontUtils.getTplPath(request, site.getSolutionPath(),
				TPLDIR_MEMBER, RESULT_PAGE);
	}

	private WebCoreErrors validate(String filename, MultipartFile file,
			HttpServletRequest request) {
		WebCoreErrors errors = WebCoreErrors.create(request);
		if (file == null) {
			errors.addErrorCode("imageupload.error.noFileToUpload");
			return errors;
		}
		if (StringUtils.isBlank(filename)) {
			filename = file.getOriginalFilename();
		}
		String ext = FilenameUtils.getExtension(filename);
		if(filename!=null&&(filename.contains("/")||filename.contains("\\")||filename.indexOf("\0")!=-1)){
			errors.addErrorCode("upload.error.filename", filename);
			return errors;
		}
		if (!ImageUtils.isValidImageExt(ext)) {
			errors.addErrorCode("imageupload.error.notSupportExt", ext);
			return errors;
		}
		try {
			if (!ImageUtils.isImage(file.getInputStream())) {
				errors.addErrorCode("imageupload.error.notImage", ext);
				return errors;
			}
		} catch (IOException e) {
			log.error("image upload error", e);
			errors.addErrorCode("imageupload.error.ioError", ext);
			return errors;
		}
		return errors;
	}

	private File mark(MultipartFile file, MarkConfig conf) throws Exception {
		String path = System.getProperty("java.io.tmpdir");
		File tempFile = new File(path, String.valueOf(System
				.currentTimeMillis()));
		file.transferTo(tempFile);
		boolean imgMark = !StringUtils.isBlank(conf.getImagePath());
		if (imgMark) {
			File markImg = new File(realPathResolver.get(conf.getImagePath()));
			imageScale.imageMark(tempFile, tempFile, conf.getMinWidth(), conf
					.getMinHeight(), conf.getPos(), conf.getOffsetX(), conf
					.getOffsetY(), markImg);
		} else {
			imageScale.imageMark(tempFile, tempFile, conf.getMinWidth(), conf
					.getMinHeight(), conf.getPos(), conf.getOffsetX(), conf
					.getOffsetY(), conf.getContent(), Color.decode(conf
					.getColor()), conf.getSize(), conf.getAlpha());
		}
		return tempFile;
	}

	private FileRepository fileRepository;
	private DbFileMng dbFileMng;
	private ImageScale imageScale;
	private RealPathResolver realPathResolver;

	@Autowired
	public void setFileRepository(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	@Autowired
	public void setDbFileMng(DbFileMng dbFileMng) {
		this.dbFileMng = dbFileMng;
	}

	@Autowired
	public void setImageScale(ImageScale imageScale) {
		this.imageScale = imageScale;
	}

	@Autowired
	public void setRealPathResolver(RealPathResolver realPathResolver) {
		this.realPathResolver = realPathResolver;
	}
}