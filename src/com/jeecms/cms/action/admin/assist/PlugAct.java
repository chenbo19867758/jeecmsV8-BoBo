package com.jeecms.cms.action.admin.assist;

import static com.jeecms.common.page.SimplePage.cpn;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.jeecms.cms.Constants;
import com.jeecms.cms.entity.assist.CmsPlug;
import com.jeecms.cms.manager.assist.CmsPlugMng;
import com.jeecms.cms.manager.assist.CmsResourceMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.upload.FileRepository;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.tpl.TplManager;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

/**
 * @author Tom
 */
@Controller
public class PlugAct {
	private static final Logger log = LoggerFactory.getLogger(PlugAct.class);
	private static final String PLUG_CONFIG_INI="plug.ini";
	private static final String PLUG_CONFIG_KEY_REPAIR="plug_repair";

	@RequiresPermissions("plug:v_list")
	@RequestMapping(value = "/plug/v_list.do")
	public String list(Integer pageNo,HttpServletRequest request,
			 ModelMap model) {
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination",pagination);
		model.addAttribute("pageNo",pagination.getPageNo());
		return "plug/list";
	}
	
	@RequiresPermissions("plug:v_add")
	@RequestMapping("/plug/v_add.do")
	public String add(ModelMap model) {
		return "plug/add";
	}

	@RequiresPermissions("plug:v_edit")
	@RequestMapping("/plug/v_edit.do")
	public String edit(Integer id, Integer pageNo, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("plug", manager.findById(id));
		model.addAttribute("pageNo",pageNo);
		return "plug/edit";
	}

	@RequiresPermissions("plug:o_save")
	@RequestMapping("/plug/o_save.do")
	public String save(CmsPlug bean, HttpServletRequest request, ModelMap model) throws IOException {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		//新功能插件检测包含文件是否冲突
		File file=new File(realPathResolver.get(bean.getPath()));
		if(file.exists()){
			if(!isRepairPlug(file)){
				boolean fileConflict=isFileConflict(file);
				bean.setFileConflict(fileConflict);
			}else{
				bean.setFileConflict(false);
			}
		}
		bean.setUploadTime(Calendar.getInstance().getTime());
		bean = manager.save(bean);
		log.info("save CmsPlug id={}", bean.getId());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("plug:o_update")
	@RequestMapping("/plug/o_update.do")
	public String update(CmsPlug bean,String path,
			Integer pageNo, HttpServletRequest request,
			ModelMap model) throws IOException {
		WebErrors errors = validateUpdate(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		//新功能插件需要检测文件是否冲突
		if(StringUtils.isNotBlank(path)){
			File file=new File(realPathResolver.get(bean.getPath()));
			if(file.exists()){
				if(!isRepairPlug(file)){
					boolean fileConflict=isFileConflict(file);
					bean.setFileConflict(fileConflict);
				}else{
					bean.setFileConflict(false);
				}
			}
		}
		System.out.println("1->"+bean.getUsed());
		bean = manager.update(bean);
		System.out.println("2->"+bean.getUsed());
		log.info("update CmsPlug id={}.", bean.getId());
		return list(pageNo, request, model);
	}

	/**
	 * 上传
	 * 
	 * @param file
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequiresPermissions("plug:o_upload")
	@RequestMapping(value = "/plug/o_upload.do")
	public String uploadSubmit(
			@RequestParam(value = "plugFile", required = false) MultipartFile file,
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException {
		WebErrors errors = validateUpload(file, request);
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getErrors().get(0));
			return "plug/upload_iframe";
		}
		String origName = file.getOriginalFilename();
		String ext = FilenameUtils.getExtension(origName).toLowerCase(
				Locale.ENGLISH);
		// TODO 检查允许上传的后缀
		try {
			String fileUrl;
			String filename=Constants.PLUG_PATH+file.getOriginalFilename();
			File oldFile=new File(realPathResolver.get(filename));
			if(oldFile.exists()){
				oldFile.delete();
			}
			fileUrl = fileRepository.storeByFilePath(Constants.PLUG_PATH,file.getOriginalFilename(), file);
			model.addAttribute("plugPath", fileUrl);
			model.addAttribute("plugExt", ext);
		} catch (IllegalStateException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		} catch (IOException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		}
		cmsLogMng.operating(request, "plug.log.upload", "filename=" + file.getName());
		return "plug/upload_iframe";
	}
	
	/**
	 * 安装
	 * @param name
	 * @param request
	 * @param response
	 * @param model
	 * @throws IOException
	 * @throws JSONException 
	 */
	@RequiresPermissions("plug:o_install")
	@RequestMapping(value = "/plug/o_install.do")
	public void install(Integer id,HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws IOException, JSONException {
		CmsUser user = CmsUtils.getUser(request);
		JSONObject object = new JSONObject();
		if (user == null) {
			object.put("login", false);
		} else {
			//解压zip
			CmsPlug plug=manager.findById(id);
			if(plug!=null&&fileExist(plug.getPath())){
				File zipFile =new File(realPathResolver.get(plug.getPath()));
				boolean isRepairPlug=isRepairPlug(zipFile);
				//修复类插件不管是否冲突，可以安装
				if(isRepairPlug){
					installPlug(zipFile, plug, true,request);
				}else{
					//新功能有冲突不解压
					boolean fileConflict=isFileConflict(zipFile);
					if(fileConflict){
						object.put("conflict", true);
					}else{
						object.put("conflict", false);
						installPlug(zipFile, plug,false, request);
					}
				}
				object.put("exist", true);
			}else{
				object.put("exist", false);
			}
			object.put("login", true);
		}
		ResponseUtils.renderJson(response, object.toString());
	}
	
	private void installPlug(File zipFile,CmsPlug plug,boolean isRepairPlug,
			HttpServletRequest request) throws IOException{
		//resourceMng.unZipFile(zipFile);
		plug.setInstallTime(Calendar.getInstance().getTime());
		plug.setUsed(true);
		plug.setPlugRepair(isRepairPlug);
		resourceMng.installPlug(zipFile, plug);
		cmsLogMng.operating(request, "plug.log.install", "name=" +plug.getName());
	}

	/**
	 * 卸载
	 * @param name
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 * @throws JSONException 
	 */
	@RequiresPermissions("plug:o_uninstall")
	@RequestMapping("/plug/o_uninstall.do")
	public void uninstall(Integer id, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) 
					throws IOException, JSONException {
		CmsUser user = CmsUtils.getUser(request);
		JSONObject object = new JSONObject();
		if (user == null) {
			object.put("login", false);
		} else {
			CmsPlug plug=manager.findById(id);
			if(plug!=null&&fileExist(plug.getPath())){
				File file = new File(realPathResolver.get(plug.getPath()));
				//是修复类插件则不允许卸载，如卸载则可能系统运行错误
				if(plug.getPlugRepair()){
					object.put("repair", true);
				}else{
					object.put("repair", false);
					boolean fileConflict=plug.getFileConflict();
					if(!fileConflict){
						resourceMng.deleteZipFile(file);
						plug.setUninstallTime(Calendar.getInstance().getTime());
						plug.setUsed(false);
						manager.update(plug);
						log.info("delete plug name={}", plug.getPath());
						cmsLogMng.operating(request, "plug.log.uninstall", "filename=" + plug.getName());
						object.put("conflict", false);
					}else{
						object.put("conflict", true);
					}
				}
				object.put("exist", true);
			}else{
				object.put("exist", false);
			}
			object.put("login", true);
		}
		ResponseUtils.renderJson(response, object.toString());
	}
	
	@RequiresPermissions("plug:o_delete")
	@RequestMapping("/plug/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsPlug[] beans = manager.deleteByIds(ids);
		for (CmsPlug bean : beans) {
			tplManager.delete(new String[] { bean.getPath() });
			log.info("delete CmsPlug id={}", bean.getId());
		}
		return list(pageNo, request, model);
	}
	
	private  boolean isRepairPlug(File file) {
		boolean isRepairPlug=false;
		String plugIni="";
		String repairStr="";
		try {
			plugIni=resourceMng.readFileFromZip(file, PLUG_CONFIG_INI);
			if(StringUtils.isNotBlank(plugIni)){
				String[]configs=plugIni.split(";");
				for(String c:configs){
					String[] configAtt=c.split("=");
					if(configAtt!=null&&configAtt.length==2){
						if(configAtt[0].equals(PLUG_CONFIG_KEY_REPAIR)){
							repairStr=configAtt[1];
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(StringUtils.isNotBlank(repairStr)&&repairStr.toLowerCase().equals("true")){
			isRepairPlug=true;
		}
		return isRepairPlug;
	}
	
	@SuppressWarnings("unchecked")
	private boolean isFileConflict(File file) throws IOException{
		ZipFile zip = new ZipFile(file, "GBK");
		ZipEntry entry;
		String name;
		String filename;
		File outFile;
		boolean fileConflict=false;
		Enumeration<ZipEntry> en = zip.getEntries();
		while (en.hasMoreElements()) {
			entry = en.nextElement();
			name = entry.getName();
			if (!entry.isDirectory()) {
				name = entry.getName();
				filename =  name;
				outFile = new File(realPathResolver.get(filename));
				if(outFile.exists()){
					fileConflict=true;
					break;
				}
			}
		}
		zip.close();
		return fileConflict;
	}
	
	
	
	private WebErrors validateSave(CmsPlug bean, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		return errors;
	}
	
	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateUpdate(CmsPlug plug, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(plug.getId(), site.getId(), errors)) {
			return errors;
		}
		CmsPlug dbPlug=manager.findById(plug.getId());
		//使用中插件不允许修改路径
		if(dbPlug!=null&&dbPlug.getUsed()&&(
				StringUtils.isNotBlank(plug.getPath())
				&&!dbPlug.getPath().equals(plug.getPath()))
				){
			errors.addErrorCode("error.plug.upload",dbPlug.getName());
		}
		return errors;
	}


	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Integer id : ids) {
			vldExist(id, site.getId(), errors);
			vldUsed(id, errors);
		}
		return errors;
	}
	
	private WebErrors validateUpload(MultipartFile file,
			HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (errors.ifNull(file, "file")) {
			return errors;
		}
		String filename=file.getOriginalFilename();
		if(filename!=null&&filename.indexOf("\0")!=-1){
			errors.addErrorCode("upload.error.filename", filename);
		}
		String filePath=Constants.PLUG_PATH+filename;
		CmsPlug plug=manager.findByPath(filePath);
		File tempFile =new File(realPathResolver.get(filePath));
		//使用中的而且插件已经存在则不允许重新上传
		if(plug!=null&&plug.getUsed()&&tempFile.exists()){
			errors.addErrorCode("error.plug.upload",plug.getName());
		}
		return errors;
	}

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		CmsPlug entity = manager.findById(id);
		if(errors.ifNotExist(entity, CmsPlug.class, id)) {
			return true;
		}
		return false;
	}
	
	private boolean vldUsed(Integer id, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		CmsPlug entity = manager.findById(id);
		if(entity.getUsed()){
			errors.addErrorCode("error.plug.delele", entity.getName());
		}
		return false;
	}
	
	private boolean fileExist(String filePath) {
		File file=new File(realPathResolver.get(filePath));
		return file.exists();
	}
	
	@Autowired
	private CmsPlugMng manager;
	@Autowired
	private CmsResourceMng resourceMng;
	@Autowired
	private TplManager tplManager;
	@Autowired
	protected FileRepository fileRepository;
	@Autowired
	private RealPathResolver realPathResolver;
	@Autowired
	private CmsLogMng cmsLogMng;
}
