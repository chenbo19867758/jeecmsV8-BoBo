package com.jeecms.cms.action.member;

import static com.jeecms.cms.Constants.TPLDIR_MEMBER;
import static org.apache.shiro.web.filter.authc.FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jeecms.common.web.session.SessionProvider;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.manager.ConfigMng;
import com.jeecms.core.manager.UnifiedUserMng;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.util.FrontUtils;

@Controller
public class CasLoginAct {
	public static final String COOKIE_ERROR_REMAINING = "_error_remaining";
	public static final String LOGIN_INPUT = "tpl.loginInput";
	public static final String LOGIN_STATUS = "tpl.loginStatus";
	public static final String TPL_INDEX = "tpl.index";


	@RequestMapping(value = "/login.jspx", method = RequestMethod.GET)
	public String input(String returnUrl,HttpServletRequest request,
			HttpServletResponse response,ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		String sol = site.getSolutionPath();
		Integer errorTimes=configMng.getConfigLogin().getErrorTimes();
		model.addAttribute("errorTimes", errorTimes);
		model.addAttribute("site",site);
		if(StringUtils.isBlank(returnUrl)){
			session.setAttribute(request, response, "loginSource", null);
		}
		Object source=session.getAttribute(request, "loginSource");
		if(source!=null){
			String loginSource=(String) source;
			model.addAttribute("loginSource",loginSource);
		}
		FrontUtils.frontData(request, model, site);
		return FrontUtils.getTplPath(request, sol, TPLDIR_MEMBER, LOGIN_INPUT);
	}

	@RequestMapping(value = "/login.jspx", method = RequestMethod.POST)
	public String submit(String username, HttpServletRequest request,
			HttpServletResponse response,ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		String sol = site.getSolutionPath();
		Object error = request.getAttribute(DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
		if (error != null) {
			model.addAttribute("error",error);
			model.addAttribute("errorRemaining", unifiedUserMng.errorRemaining(username));
		}
		session.setAttribute(request, response, "loginSource", null);
		FrontUtils.frontData(request, model, site);
		return FrontUtils.getTplPath(request, sol, TPLDIR_MEMBER, LOGIN_INPUT);
	}
	
	@Autowired
	private UnifiedUserMng unifiedUserMng;
	@Autowired
	private ConfigMng configMng;
	@Autowired
	private SessionProvider session;
}
