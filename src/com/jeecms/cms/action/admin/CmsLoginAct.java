package com.jeecms.cms.action.admin;

import static org.apache.shiro.web.filter.authc.FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jeecms.common.web.CookieUtils;
import com.jeecms.core.manager.ConfigMng;
import com.jeecms.core.manager.UnifiedUserMng;

@Controller
public class CmsLoginAct {
	/**
	 * 站点id cookie
	 */
	public static final String SITE_COOKIE = "_site_id_cookie";

	@RequestMapping(value = "/login.do", method = RequestMethod.GET)
	public String input(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		//可能进入没有权限子站(cookie记录站点)需要清除cookie
		CookieUtils.cancleCookie(request, response, SITE_COOKIE, null);
		Integer errorTimes=configMng.getConfigLogin().getErrorTimes();
		model.addAttribute("errorTimes", errorTimes);
		return "login";
	}

	@RequestMapping(value = "/login.do", method = RequestMethod.POST)
	public String submit(String username, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		Object error = request.getAttribute(DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
		if (error != null) {
			model.addAttribute("error", error);
			Integer errorRemaining= unifiedUserMng.errorRemaining(username);
			model.addAttribute("errorRemaining", errorRemaining);
		}
		return "login";
	}
	@Autowired
	private UnifiedUserMng unifiedUserMng;
	@Autowired
	private ConfigMng configMng;
}
