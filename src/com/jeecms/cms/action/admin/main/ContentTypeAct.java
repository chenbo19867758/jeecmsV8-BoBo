package com.jeecms.cms.action.admin.main;

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

import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.ContentType;
import com.jeecms.cms.manager.main.ContentTypeMng;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class ContentTypeAct {
	private static final Logger log = LoggerFactory.getLogger(ContentTypeAct.class);
	
	@RequiresPermissions("type:v_list")
	@RequestMapping("/type/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		List<ContentType> list = manager.getList(true);
		model.addAttribute("list", list);
		return "type/list";
	}

	@RequiresPermissions("type:v_add")
	@RequestMapping("/type/v_add.do")
	public String add(ModelMap model) {
		return "type/add";
	}

	@RequiresPermissions("type:v_edit")
	@RequestMapping("/type/v_edit.do")
	public String edit(Integer id, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("contentType", manager.findById(id));
		return "type/edit";
	}

	@RequiresPermissions("type:o_save")
	@RequestMapping("/type/o_save.do")
	public String save(ContentType bean, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.save(bean);
		log.info("save ContentType id={}", bean.getId());
		cmsLogMng.operating(request, "contentType.log.save", "id="
				+ bean.getId() + ";name=" + bean.getName());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("type:o_update")
	@RequestMapping("/type/o_update.do")
	public String update(ContentType bean, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean);
		log.info("update ContentType id={}.", bean.getId());
		cmsLogMng.operating(request, "contentType.log.update", "id="
				+ bean.getId() + ";name=" + bean.getName());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("type:o_delete")
	@RequestMapping("/type/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		ContentType[] beans = manager.deleteByIds(ids);
		for (ContentType bean : beans) {
			log.info("delete ContentType id={}", bean.getId());
			cmsLogMng.operating(request, "contentType.log.delete", "id="
					+ bean.getId() + ";name=" + bean.getName());
		}
		return list(pageNo, request, model);
	}
	
	@RequiresPermissions("type:v_check_id")
	@RequestMapping(value = "/type/v_check_id.do")
	public void checkPath(Integer id,
			HttpServletRequest request, HttpServletResponse response) {
		String pass;
		if (id==null) {
			pass = "false";
		} else {
			ContentType c = manager.findById(id);
			if(c==null){
				pass="true" ;
			}else{
				pass="false";
			}
		}
		ResponseUtils.renderJson(response, pass);
	}

	private WebErrors validateSave(ContentType bean, HttpServletRequest request) {
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
		errors.ifEmpty(ids, "ids");
		for (Integer id : ids) {
			vldExist(id, site.getId(), errors);
		}
		return errors;
	}

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		ContentType entity = manager.findById(id);
		if (errors.ifNotExist(entity, ContentType.class, id)) {
			return true;
		}
		return false;
	}

	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private ContentTypeMng manager;
}