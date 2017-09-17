package com.jeecms.core.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;

import com.jeecms.common.web.CookieUtils;


/**
 * CmsUserFilter
 * 
 * 这里重写 CmsLogoutFilter的目的，区分，前台登录和后台 登出 转向的地址。
 */
public class CmsLogoutFilter extends LogoutFilter {
	/**
	 * 返回URL
	 */
	public static final String RETURN_URL = "returnUrl";
	
	public static final String USER_LOG_OUT_FLAG = "logout";
	
	private String adminPrefix; // shiro-context.xml中 authcFilter parent="adminUrlBean" adminUrlBean 注入
	private String adminLogin; // shiro-context.xml中 authcFilter parent="adminUrlBean" adminUrlBean 注入

	protected String getRedirectUrl(ServletRequest req, ServletResponse resp,Subject subject) {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response=(HttpServletResponse) resp;
		String redirectUrl = request.getParameter(RETURN_URL);
		String domain = request.getServerName();
		if (domain.indexOf(".") > -1) {
			domain= domain.substring(domain.indexOf(".") + 1);
		}
		CookieUtils.addCookie(request, response,  "JSESSIONID",  null,0,domain,"/");
		CookieUtils.addCookie(request, response,  "sso_logout",  "true",null,domain,"/");
		if (StringUtils.isBlank(redirectUrl)) {
			if (request.getRequestURI().startsWith(request.getContextPath() + getAdminPrefix())) {
				redirectUrl = getAdminLogin(); // 后台登录页
			} else {
				redirectUrl = getRedirectUrl(); // 前台登录页面，这里为 默认的RedirectUrl 为 LogoutFilter 中定义的 DEFAULT_REDIRECT_URL = "/";
			}
		}
		return redirectUrl;
	}

	public String getAdminPrefix() {
		return adminPrefix;
	}

	public void setAdminPrefix(String adminPrefix) {
		this.adminPrefix = adminPrefix;
	}

	public String getAdminLogin() {
		return adminLogin;
	}

	public void setAdminLogin(String adminLogin) {
		this.adminLogin = adminLogin;
	}

	
}
