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

import com.jeecms.cms.entity.assist.CmsGuestbookCtg;
import com.jeecms.cms.manager.assist.CmsGuestbookCtgMng;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsGuestbookCtgAct {
	private static final Logger log = LoggerFactory
			.getLogger(CmsGuestbookCtgAct.class);

	@RequiresPermissions("guestbook_ctg:v_list")
	@RequestMapping("/guestbook_ctg/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		List<CmsGuestbookCtg> list = manager.getList(site.getId());
		model.addAttribute("list", list);
		return "guestbook_ctg/list";
	}

	@RequiresPermissions("guestbook_ctg:v_add")
	@RequestMapping("/guestbook_ctg/v_add.do")
	public String add(ModelMap model) {
		return "guestbook_ctg/add";
	}

	@RequiresPermissions("guestbook_ctg:v_edit")
	@RequestMapping("/guestbook_ctg/v_edit.do")
	public String edit(Integer id, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("cmsGuestbookCtg", manager.findById(id));
		model.addAttribute("pageNo", pageNo);
		return "guestbook_ctg/edit";
	}
	
	@RequiresPermissions("guestbook_ctg:v_ajax_edit")
	@RequestMapping("/guestbook_ctg/v_ajax_edit.do")
	public void ajaxEdit(Integer id, HttpServletRequest request,HttpServletResponse response, ModelMap model) throws JSONException {
		JSONObject object = new JSONObject();
		CmsGuestbookCtg ctg=manager.findById(id);
		if(ctg!=null){
			object.put("id", ctg.getId());
			object.put("name", ctg.getName());
			object.put("description", ctg.getDescription());
			object.put("priority", ctg.getPriority());
		}
		ResponseUtils.renderJson(response, object.toString());
	}

	@RequiresPermissions("guestbook_ctg:o_save")
	@RequestMapping("/guestbook_ctg/o_save.do")
	public String save(CmsGuestbookCtg bean, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.save(bean);
		log.info("save CmsGuestbookCtg id={}", bean.getId());
		cmsLogMng.operating(request, "cmsGuestbookCtg.log.save", "id="
				+ bean.getId() + ";name=" + bean.getName());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("guestbook_ctg:o_update")
	@RequestMapping("/guestbook_ctg/o_update.do")
	public String update(CmsGuestbookCtg bean, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean);
		log.info("update CmsGuestbookCtg id={}.", bean.getId());
		cmsLogMng.operating(request, "cmsGuestbookCtg.log.update", "id="
				+ bean.getId() + ";name=" + bean.getName());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("guestbook_ctg:o_delete")
	@RequestMapping("/guestbook_ctg/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsGuestbookCtg[] beans = manager.deleteByIds(ids);
		for (CmsGuestbookCtg bean : beans) {
			log.info("delete CmsGuestbookCtg id={}", bean.getId());
			cmsLogMng.operating(request, "cmsGuestbookCtg.log.delete", "id="
					+ bean.getId() + ";name=" + bean.getName());
		}
		return list(pageNo, request, model);
	}

	private WebErrors validateSave(CmsGuestbookCtg bean,
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
		CmsGuestbookCtg entity = manager.findById(id);
		if (errors.ifNotExist(entity, CmsGuestbookCtg.class, id)) {
			return true;
		}
		if (!entity.getSite().getId().equals(siteId)) {
			errors.notInSite(CmsGuestbookCtg.class, id);
			return true;
		}
		return false;
	}

	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private CmsGuestbookCtgMng manager;
}