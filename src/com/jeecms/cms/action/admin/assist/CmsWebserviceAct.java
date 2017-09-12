package com.jeecms.cms.action.admin.assist;

import static com.jeecms.common.page.SimplePage.cpn;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsWebservice;
import com.jeecms.cms.manager.assist.CmsWebserviceMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;

@Controller
public class CmsWebserviceAct {
	private static final Logger log = LoggerFactory.getLogger(CmsWebserviceAct.class);

	@RequiresPermissions("webservice:v_list")
	@RequestMapping("/webservice/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request, ModelMap model) {
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination",pagination);
		return "webservice/list";
	}

	@RequiresPermissions("webservice:v_add")
	@RequestMapping("/webservice/v_add.do")
	public String add(ModelMap model) {
		return "webservice/add";
	}

	@RequiresPermissions("webservice:v_edit")
	@RequestMapping("/webservice/v_edit.do")
	public String edit(Integer id, HttpServletRequest request, ModelMap model) {
		model.addAttribute("cmsWebservice", manager.findById(id));
		return "webservice/edit";
	}

	@RequiresPermissions("webservice:o_save")
	@RequestMapping("/webservice/o_save.do")
	public String save(CmsWebservice bean, String[] paramName, String[] defaultValue,
			HttpServletRequest request, ModelMap model) {
		bean = manager.save(bean,paramName,defaultValue);
		log.info("save CmsWebservice id={}", bean.getId());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("webservice:o_update")
	@RequestMapping("/webservice/o_update.do")
	public String update(CmsWebservice bean,String[] paramName, String[] defaultValue,
			Integer pageNo, HttpServletRequest request,ModelMap model) {
		bean = manager.update(bean,paramName,defaultValue);
		log.info("update CmsWebservice id={}.", bean.getId());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("webservice:o_delete")
	@RequestMapping("/webservice/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		manager.deleteByIds(ids);
		return  list(pageNo, request, model);
	}
	
	@Autowired
	private CmsWebserviceMng manager;
}