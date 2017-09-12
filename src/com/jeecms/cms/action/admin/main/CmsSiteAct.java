package com.jeecms.cms.action.admin.main;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsConfig;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.manager.CmsConfigMng;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.manager.FtpMng;
import com.jeecms.core.web.WebCoreErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsSiteAct {
	private static final Logger log = LoggerFactory.getLogger(CmsSiteAct.class);
	
	@RequiresPermissions("site:site_main")
	@RequestMapping("/site/site_main.do")
	public String siteMain(ModelMap model) {
		return "site/site_main";
	}
	
	@RequiresPermissions("site:v_left")
	@RequestMapping("/site/v_left.do")
	public String left() {
		return "site/left";
	}
	
	@RequiresPermissions("site:v_tree")
	@RequestMapping(value = "/site/v_tree.do")
	public String selectParent(String root, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		log.debug("tree path={}", root);
		boolean isRoot;
		// jquery treeview的根请求为root=source
		if (StringUtils.isBlank(root) || "source".equals(root)) {
			isRoot = true;
		} else {
			isRoot = false;
		}
		model.addAttribute("isRoot", isRoot);
		List<CmsSite> siteList;
		siteList= manager.getList();
		
		model.addAttribute("list", siteList);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "site/tree";
	}
	
	@RequiresPermissions("site:v_list")
	@RequestMapping("/site/v_list.do")
	public String list(HttpServletRequest request,
			ModelMap model) {
		List<CmsSite> list;
		list = manager.getList();
		model.addAttribute("list", list);
		return "site/list";
	}

	@RequiresPermissions("site:v_add")
	@RequestMapping("/site/v_add.do")
	public String add(ModelMap model) {
		List<Ftp> ftpList = ftpMng.getList();
		model.addAttribute("config", configMng.get());
		model.addAttribute("ftpList", ftpList);
		return "site/add";
	}

	@RequiresPermissions("site:v_edit")
	@RequestMapping("/site/v_edit.do")
	public String edit(Integer id,HttpServletRequest request, ModelMap model) {
		WebCoreErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		List<Ftp> ftpList = ftpMng.getList();
		model.addAttribute("config", configMng.get());
		model.addAttribute("ftpList", ftpList);
		model.addAttribute("cmsSite", manager.findById(id));
		return "site/edit";
	}

	@RequiresPermissions("site:o_save")
	@RequestMapping("/site/o_save.do")
	public String save(CmsSite bean, Integer uploadFtpId,
			Integer syncPageFtpId,
			HttpServletRequest request, ModelMap model) throws IOException {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		WebCoreErrors errors = validateSave(bean, uploadFtpId, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.save(site, user, bean, uploadFtpId,syncPageFtpId);
		log.info("save CmsSite id={}", bean.getId());
		cmsLogMng.operating(request, "cmsSite.log.save", "id=" + bean.getId()
				+ ";name=" + bean.getName());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("site:o_update")
	@RequestMapping("/site/o_update.do")
	public String update(CmsSite bean, Integer uploadFtpId,
			Integer syncPageFtpId, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebCoreErrors errors = validateUpdate(bean.getId(), uploadFtpId, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean, uploadFtpId,syncPageFtpId);
		log.info("update CmsSite id={}.", bean.getId());
		cmsLogMng.operating(request, "cmsSite.log.update", "id=" + bean.getId()
				+ ";name=" + bean.getName());
		return list(request, model);
	}

	@RequiresPermissions("site:o_delete")
	@RequestMapping("/site/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebCoreErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsSite[] beans = manager.deleteByIds(ids);
		for (CmsSite bean : beans) {
			log.info("delete CmsSite id={}", bean.getId());
			cmsLogMng.operating(request, "cmsSite.log.delete", "id="
					+ bean.getId() + ";name=" + bean.getName());
		}
		return list(request, model);
	}

	@RequiresPermissions("site:v_checkDomain")
	@RequestMapping("/site/v_checkDomain.do")
	public void checkDomainJson(Integer siteId,String domain, HttpServletResponse response) {
		String pass;
		if (StringUtils.isBlank(domain)) {
			pass = "false";
		} else {
			CmsSite s=manager.findByDomain(domain);
			if(s==null){
				pass= "true";
			}else{
				if(s.getId().equals(siteId)){
					pass= "true";
				}else{
					pass= "false";
				}
			}
		}
		ResponseUtils.renderJson(response, pass);
	}


	private WebCoreErrors validateSave(CmsSite bean, Integer uploadFtpId,
			HttpServletRequest request) {
		WebCoreErrors errors = WebCoreErrors.create(request);
		if (vldFtpExist(uploadFtpId, errors)) {
			return errors;
		}
		// 加上config信息
		bean.setConfig(configMng.get());
		return errors;
	}

	private WebCoreErrors validateEdit(Integer id, HttpServletRequest request) {
		WebCoreErrors errors = WebCoreErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		return errors;
	}

	private WebCoreErrors validateUpdate(Integer id, Integer uploadFtpId,
			HttpServletRequest request) {
		WebCoreErrors errors = WebCoreErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		if (vldFtpExist(uploadFtpId, errors)) {
			return errors;
		}
		return errors;
	}

	private WebCoreErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebCoreErrors errors = WebCoreErrors.create(request);
		errors.ifEmpty(ids, "ids");
		for (Integer id : ids) {
			vldExist(id, errors);
		}
		return errors;
	}

	private boolean vldFtpExist(Integer id, WebCoreErrors errors) {
		if (id == null) {
			return false;
		}
		Ftp entity = ftpMng.findById(id);
		return errors.ifNotExist(entity, Ftp.class, id);
	}

	private boolean vldExist(Integer id, WebCoreErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		CmsSite entity = manager.findById(id);
		if (errors.ifNotExist(entity, CmsSite.class, id)) {
			return true;
		}
		return false;
	}

	@Autowired
	private CmsConfigMng configMng;
	@Autowired
	private FtpMng ftpMng;
	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private CmsSiteMng manager;
}