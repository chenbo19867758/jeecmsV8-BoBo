package com.jeecms.cms.action.admin.main;

import static com.jeecms.common.page.SimplePage.cpn;

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

import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.UnifiedUser;
import com.jeecms.core.manager.UnifiedUserMng;
import com.jeecms.core.web.WebErrors;

@Controller
public class UnifiedUserAct {
	private static final Logger log = LoggerFactory.getLogger(UnifiedUserAct.class);
	
	@RequiresPermissions("unified_user:v_list")
	@RequestMapping("/unified_user/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination", pagination);
		return "unified_user/list";
	}

	@RequiresPermissions("unified_user:v_list")
	@RequestMapping("/unified_user/v_add.do")
	public String add(ModelMap model) {
		return "unified_user/add";
	}

	@RequiresPermissions("unified_user:v_edit")
	@RequestMapping("/unified_user/v_edit.do")
	public String edit(Integer id, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("user", manager.findById(id));
		return "unified_user/edit";
	}

	@RequiresPermissions("unified_user:o_save")
	@RequestMapping("/unified_user/o_save.do")
	public String save(String username, String email, String password,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateSave(username, email, password, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		UnifiedUser user = manager.save(username, email, password, request
				.getRemoteAddr());
		log.info("save UnifiedUser id={}, username={}", user.getId(), user
				.getUsername());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("unified_user:o_update")
	@RequestMapping("/unified_user/o_update.do")
	public String update(Integer id, String email, String password,
			Integer pageNo, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateUpdate(id, email, password, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		UnifiedUser user = manager.update(id, password, email);
		log.info("update UnifiedUser id={}.", user.getId());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("unified_user:o_delete")
	@RequestMapping("/unified_user/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		UnifiedUser[] beans = manager.deleteByIds(ids);
		for (UnifiedUser bean : beans) {
			log.info("delete UnifiedUser id={}", bean.getId());
		}
		return list(pageNo, request, model);
	}

	@RequiresPermissions("unified_user:v_check_username")
	@RequestMapping("/unified_user/v_check_username.do")
	public String checkUsername(String username, HttpServletRequest request,
			HttpServletResponse response) {
		if (StringUtils.isBlank(username) || manager.usernameExist(username)) {
			ResponseUtils.renderJson(response, "false");
		} else {
			ResponseUtils.renderJson(response, "true");
		}
		return null;
	}

	@RequiresPermissions("unified_user:v_check_email")
	@RequestMapping("/unified_user/v_check_email.do")
	public String checkEmail(String email, HttpServletRequest request,
			HttpServletResponse response) {
		if (StringUtils.isBlank(email) || manager.emailExist(email)) {
			ResponseUtils.renderJson(response, "false");
		} else {
			ResponseUtils.renderJson(response, "true");
		}
		return null;
	}

	private WebErrors validateSave(String username, String email,
			String password, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		return errors;
	}

	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateUpdate(Integer id, String email, String password,
			HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		return errors;
	}

	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Integer id : ids) {
			if (vldExist(id, errors)) {
				return errors;
			}
		}

		return errors;
	}

	private boolean vldExist(Integer id, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		UnifiedUser entity = manager.findById(id);
		if (errors.ifNotExist(entity, UnifiedUser.class, id)) {
			return true;
		}
		return false;
	}

	@Autowired
	private UnifiedUserMng manager;
}