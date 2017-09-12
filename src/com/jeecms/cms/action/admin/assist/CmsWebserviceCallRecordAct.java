package com.jeecms.cms.action.admin.assist;

import static com.jeecms.common.page.SimplePage.cpn;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.manager.assist.CmsWebserviceCallRecordMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;

@Controller
public class CmsWebserviceCallRecordAct {

	@RequiresPermissions("webserviceCallRecord:v_list")
	@RequestMapping("/webserviceCallRecord/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request, ModelMap model) {
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination",pagination);
		return "webserviceCallRecord/list";
	}
	
	@RequiresPermissions("webserviceCallRecord:o_delete")
	@RequestMapping("/webserviceCallRecord/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		manager.deleteByIds(ids);
		return list(pageNo, request, model);
	}
	@Autowired
	private CmsWebserviceCallRecordMng manager;
}