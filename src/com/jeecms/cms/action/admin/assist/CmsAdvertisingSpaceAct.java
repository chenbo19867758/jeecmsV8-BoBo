package com.jeecms.cms.action.admin.assist;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsAdvertisingSpace;
import com.jeecms.cms.manager.assist.CmsAdvertisingSpaceMng;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsAdvertisingSpaceAct {
	private static final Logger log = LoggerFactory
			.getLogger(CmsAdvertisingSpaceAct.class);

	@RequiresPermissions("advertising_space:v_list")
	@RequestMapping("/advertising_space/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		List<CmsAdvertisingSpace> list = manager.getList(site.getId());
		model.addAttribute("list", list);
		return "advertising_space/list";
	}

	@RequiresPermissions("advertising_space:v_add")
	@RequestMapping("/advertising_space/v_add.do")
	public String add(ModelMap model) {
		return "advertising_space/add";
	}

	@RequiresPermissions("advertising_space:v_edit")
	@RequestMapping("/advertising_space/v_edit.do")
	public String edit(Integer id, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("cmsAdvertisingSpace", manager.findById(id));
		model.addAttribute("pageNo", pageNo);
		return "advertising_space/edit";
	}
	
	@RequiresPermissions("advertising_space:v_ajax_edit")
	@RequestMapping("/advertising_space/v_ajax_edit.do")
	public void ajaxEdit(Integer id, HttpServletRequest request,HttpServletResponse response, ModelMap model) throws JSONException {
		JSONObject object = new JSONObject();
		CmsAdvertisingSpace space=manager.findById(id);
		if(space!=null){
			object.put("id", space.getId());
			object.put("name", space.getName());
			object.put("description", space.getDescription());
			object.put("enabled", space.getEnabled());
		}
		ResponseUtils.renderJson(response, object.toString());
	}

	@RequiresPermissions("advertising_space:o_save")
	@RequestMapping("/advertising_space/o_save.do")
	public String save(CmsAdvertisingSpace bean, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.save(bean);
		log.info("save CmsAdvertisingSpace id={}", bean.getId());
		cmsLogMng.operating(request, "cmsAdvertisingSpace.log.save", "id="
				+ bean.getId() + ";name=" + bean.getName());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("advertising_space:o_update")
	@RequestMapping("/advertising_space/o_update.do")
	public String update(CmsAdvertisingSpace bean, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean);
		log.info("update CmsAdvertisingSpace id={}.", bean.getId());
		cmsLogMng.operating(request, "cmsAdvertisingSpace.log.update", "id="
				+ bean.getId() + ";name=" + bean.getName());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("advertising_space:o_delete")
	@RequestMapping("/advertising_space/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsAdvertisingSpace[] beans = manager.deleteByIds(ids);
		for (CmsAdvertisingSpace bean : beans) {
			log.info("delete CmsAdvertisingSpace id={}", bean.getId());
			cmsLogMng.operating(request, "cmsAdvertisingSpace.log.delete",
					"id=" + bean.getId() + ";name=" + bean.getName());
		}
		return list(pageNo, request, model);
	}

	private WebErrors validateSave(CmsAdvertisingSpace bean,
			HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		bean.setSite(site);
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

	private WebErrors validateUpdate(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
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
		}
		return errors;
	}

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		CmsAdvertisingSpace entity = manager.findById(id);
		if (errors.ifNotExist(entity, CmsAdvertisingSpace.class, id)) {
			return true;
		}
		if (!entity.getSite().getId().equals(siteId)) {
			errors.notInSite(CmsAdvertisingSpace.class, id);
			return true;
		}
		return false;
	}

	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private CmsAdvertisingSpaceMng manager;
}