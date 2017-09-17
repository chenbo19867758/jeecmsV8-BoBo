package com.jeecms.core.security;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * CmsUserFilter
 * userFilter首先是判断是不是登录页面，如果是的话，就是allowd，
 * 如果不是,如果之前登录过，并且isRememberMe=true。则可访问，但authc的不能
 * 
 * 首先如果是访问的登录页面，则允许用户访问，如果不是，则从获得当前用户，看用户是否rememberMe了，
 * rememberMe并且在有效期内登录过，则会允许访问其路径，但不会允许访问authc的路径。
 * 
 * user是介于，anon和authc直之间的。
 * 换句话来说：而“/authenticated=user”表示访问该地址的用户是身份验证通过或RememberMe 登录的都可以。
 * 或者说，某个页面需要登录才能看，但这个页面信息又不太重要，就可已使用这个。
 * rememberMe的原理，是将登录的用户名，以某种加密的方式，存入cookie中，名字就叫rememberMe，
 * 下次登录就会从cookie里面找，如果有，pricipal就不会为空。另外得注意，调用subject.logout()的会将这个cookie清除
 * 
 * 也就是说，从rememberMe 中找到记录，登录成功后还是会进入到 userFilter 只是底层 userFilter以及继承的其他过滤器来处理。
 * 继承的过滤器通过后，不用再进入 CmsUserFilter中 重写的 redirectToLogin 方法，redirectToLogin 这里只是重写了未登录的处理逻辑
 * 
 * CmsUserFilter 入口为 继承 OncePerRequestFilter 中的doFilter 方法 ->doFilterInternal ,子继承中的方法实现，后一步步调用判断。
 * 
 * 这里重写 userfiler的目的，区分，前台登录和后台登录失败转向的登录地址。
 */
public class CmsUserFilter extends UserFilter {

	private String adminPrefix; // shiro-context.xml中 authcFilter parent="adminUrlBean" adminUrlBean 注入
	private String adminLogin; // shiro-context.xml中 authcFilter parent="adminUrlBean" adminUrlBean 注入

	// 未登录重定向到登陆页
	protected void redirectToLogin(ServletRequest req, ServletResponse resp) throws IOException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String loginUrl;
		// 后台地址跳转到后台登录地址，前台需要登录的跳转到shiro配置的登录地址
		if (request.getRequestURI().startsWith(request.getContextPath() + getAdminPrefix())) {
			loginUrl = getAdminLogin();  // 后台登录页面
		} else {
			loginUrl = getLoginUrl(); // 前台登录页面， shiro-context.xml中配置的<property name="loginUrl" value="/login.jspx" /> 
		}
		WebUtils.issueRedirect(request, response, loginUrl);
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
