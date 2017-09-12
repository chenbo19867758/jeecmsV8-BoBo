package com.jeecms.cms.action.admin;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.common.image.ImageScale;
import com.jeecms.common.upload.FileRepository;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.manager.DbFileMng;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class ImageCutAct {
	private static final Logger log = LoggerFactory
			.getLogger(ImageCutAct.class);
	/**
	 * 图片选择页面
	 */
	public static final String IMAGE_SELECT_RESULT = "/common/image_area_select";
	/**
	 * 图片裁剪完成页面
	 */
	public static final String IMAGE_CUTED = "/common/image_cuted";
	/**
	 * 错误信息参数
	 */
	public static final String ERROR = "error";

	@RequiresPermissions("common:v_image_area_select")
	@RequestMapping("/common/v_image_area_select.do")
	public String imageAreaSelect(String uploadBase, String imgSrcPath,
			Integer zoomWidth, Integer zoomHeight, String uploadNum,
			HttpServletRequest request, ModelMap model) {
		model.addAttribute("uploadBase", uploadBase);
		model.addAttribute("imgSrcPath", imgSrcPath);
		model.addAttribute("zoomWidth", zoomWidth);
		model.addAttribute("zoomHeight", zoomHeight);
		model.addAttribute("uploadNum", uploadNum);
		return IMAGE_SELECT_RESULT;
	}

	@RequiresPermissions("common:o_image_cut")
	@RequestMapping("/common/o_image_cut.do")
	public String imageCut(String imgSrcPath, Integer imgTop, Integer imgLeft,
			Integer imgWidth, Integer imgHeight, Integer reMinWidth,
			Integer reMinHeight, Float imgScale, String uploadNum,
			HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		try {
			if (imgWidth > 0) {
				if (site.getConfig().getUploadToDb()) {
					String dbFilePath = site.getConfig().getDbFileUri();
					imgSrcPath = imgSrcPath.substring(dbFilePath.length());
					File file = dbFileMng.retrieve(imgSrcPath);
					imageScale.resizeFix(file, file, reMinWidth, reMinHeight,
							getLen(imgTop, imgScale),
							getLen(imgLeft, imgScale), getLen(imgWidth,
									imgScale), getLen(imgHeight, imgScale));
					dbFileMng.restore(imgSrcPath, file);
				} else if (site.getUploadFtp() != null) {
					Ftp ftp = site.getUploadFtp();
					String ftpUrl = ftp.getUrl();
					imgSrcPath = imgSrcPath.substring(ftpUrl.length());
					String fileName=imgSrcPath.substring(imgSrcPath.lastIndexOf("/"));
					File file = ftp.retrieve(imgSrcPath,fileName);
					imageScale.resizeFix(file, file, reMinWidth, reMinHeight,
							getLen(imgTop, imgScale),
							getLen(imgLeft, imgScale), getLen(imgWidth,
									imgScale), getLen(imgHeight, imgScale));
					ftp.restore(imgSrcPath, file);
				} else {
					String ctx = request.getContextPath();
					imgSrcPath = imgSrcPath.substring(ctx.length());
					File file = fileRepository.retrieve(imgSrcPath);
					imageScale.resizeFix(file, file, reMinWidth, reMinHeight,
							getLen(imgTop, imgScale),
							getLen(imgLeft, imgScale), getLen(imgWidth,
									imgScale), getLen(imgHeight, imgScale));
				}
			} else {
				if (site.getConfig().getUploadToDb()) {
					String dbFilePath = site.getConfig().getDbFileUri();
					imgSrcPath = imgSrcPath.substring(dbFilePath.length());
					File file = dbFileMng.retrieve(imgSrcPath);
					imageScale.resizeFix(file, file, reMinWidth, reMinHeight);
					dbFileMng.restore(imgSrcPath, file);
				} else if (site.getUploadFtp() != null) {
					Ftp ftp = site.getUploadFtp();
					String ftpUrl = ftp.getUrl();
					imgSrcPath = imgSrcPath.substring(ftpUrl.length());
					String fileName=imgSrcPath.substring(imgSrcPath.lastIndexOf("/"));
					File file = ftp.retrieve(imgSrcPath,fileName);
					imageScale.resizeFix(file, file, reMinWidth, reMinHeight);
					ftp.restore(imgSrcPath, file);
				} else {
					String ctx = request.getContextPath();
					imgSrcPath = imgSrcPath.substring(ctx.length());
					File file = fileRepository.retrieve(imgSrcPath);
					imageScale.resizeFix(file, file, reMinWidth, reMinHeight);
				}
			}
			model.addAttribute("uploadNum", uploadNum);
		} catch (Exception e) {
			log.error("cut image error", e);
			model.addAttribute(ERROR, e.getMessage());
		}
		return IMAGE_CUTED;
	}

	private int getLen(int len, float imgScale) {
		return Math.round(len / imgScale);
	}

	private ImageScale imageScale;

	private FileRepository fileRepository;
	private DbFileMng dbFileMng;

	@Autowired
	public void setImageScale(ImageScale imageScale) {
		this.imageScale = imageScale;
	}

	@Autowired
	public void setFileRepository(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	@Autowired
	public void setDbFileMng(DbFileMng dbFileMng) {
		this.dbFileMng = dbFileMng;
	}

}
