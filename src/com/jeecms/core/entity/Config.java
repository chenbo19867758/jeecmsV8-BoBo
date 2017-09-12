package com.jeecms.core.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.jeecms.common.email.EmailSender;
import com.jeecms.common.email.MessageTemplate;
import com.jeecms.core.entity.base.BaseConfig;

public class Config extends BaseConfig {
	private static final long serialVersionUID = 1L;

	/* [CONSTRUCTOR MARKER BEGIN] */
	public Config() {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Config(java.lang.String id) {
		super(id);
	}

	/* [CONSTRUCTOR MARKER END] */

	public static class ConfigLogin {
		public static String LOGIN_ERROR_INTERVAL = "login_error_interval";
		public static String LOGIN_ERROR_TIMES = "login_error_times";

		private Map<String, String> attr;

		public static ConfigLogin create(Map<String, String> map) {
			ConfigLogin configLogin = new ConfigLogin();
			configLogin.setAttr(map);
			return configLogin;
		}

		public Map<String, String> getAttr() {
			if (attr == null) {
				attr = new HashMap<String, String>();
			}
			return attr;
		}

		public void setAttr(Map<String, String> attr) {
			this.attr = attr;
		}

		public Integer getErrorInterval() {
			String interval = getAttr().get(LOGIN_ERROR_INTERVAL);
			if (NumberUtils.isDigits(interval)) {
				return Integer.parseInt(interval);
			} else {
				// 默认间隔30分钟
				return 30;
			}
		}

		public void setErrorInterval(Integer errorInterval) {
			if (errorInterval != null) {
				getAttr().put(LOGIN_ERROR_INTERVAL, errorInterval.toString());
			} else {
				getAttr().put(LOGIN_ERROR_INTERVAL, null);
			}
		}

		public Integer getErrorTimes() {
			String times = getAttr().get(LOGIN_ERROR_TIMES);
			if (NumberUtils.isDigits(times)) {
				return Integer.parseInt(times);
			} else {
				// 默认3次
				return 3;
			}
		}

		public void setErrorTimes(Integer errorTimes) {
			if (errorTimes != null) {
				getAttr().put(LOGIN_ERROR_TIMES, errorTimes.toString());
			} else {
				getAttr().put(LOGIN_ERROR_TIMES, null);
			}
		}
	}

	public static class ConfigEmailSender implements EmailSender {
		public static String EMAIL_HOST = "email_host";
		public static String EMAIL_PORT = "email_port";
		public static String EMAIL_ENCODING = "email_encoding";
		public static String EMAIL_USERNAME = "email_username";
		public static String EMAIL_PASSWORD = "email_password";
		public static String EMAIL_PERSONAL = "email_personal";

		private Map<String, String> attr;

		public static ConfigEmailSender create(Map<String, String> map) {
			if (map == null || StringUtils.isBlank(map.get(EMAIL_HOST))
					|| StringUtils.isBlank(map.get(EMAIL_USERNAME))) {
				// 信息不完整返回null。
				return null;
			}
			ConfigEmailSender sender = new ConfigEmailSender();
			sender.attr = map;
			return sender;

		}

		public Map<String, String> getAttr() {
			if (attr == null) {
				attr = new HashMap<String, String>();
			}
			return attr;
		}

		public String getHost() {
			return getAttr().get(EMAIL_HOST);
		}

		public void setHost(String host) {
			getAttr().put(EMAIL_HOST, host);
		}

		public Integer getPort() {
			String port = getAttr().get(EMAIL_PORT);
			if (StringUtils.isNotBlank(port) && NumberUtils.isDigits(port)) {
				return Integer.parseInt(port);
			} else {
				return null;
			}
		}

		public void setPort(Integer port) {
			getAttr().put(EMAIL_PORT, port != null ? port.toString() : null);
		}

		public String getEncoding() {
			String encoding = getAttr().get(EMAIL_ENCODING);
			return StringUtils.isNotBlank(encoding) ? encoding : null;
		}

		public void setEncoding(String encoding) {
			getAttr().put(EMAIL_ENCODING, encoding);
		}

		public String getUsername() {
			return getAttr().get(EMAIL_USERNAME);
		}

		public void setUsername(String username) {
			getAttr().put(EMAIL_USERNAME, username);
		}

		public String getPassword() {
			String password = getAttr().get(EMAIL_PASSWORD);
			return StringUtils.isNotBlank(password) ? password : null;
		}

		public void setPassword(String password) {
			getAttr().put(EMAIL_PASSWORD, password);
		}

		public String getPersonal() {
			String personal = getAttr().get(EMAIL_PERSONAL);
			return StringUtils.isNotBlank(personal) ? personal : null;
		}

		public void setPersonal(String personal) {
			getAttr().put(EMAIL_PERSONAL, personal);
		}
	}

	public static class ConfigMessageTemplate implements MessageTemplate {
		public static String MESSAGE_FORGOTPASSWORD_SUBJECT = "message_forgotpassword_subject";
		public static String MESSAGE_FORGOTPASSWORD_TEXT = "message_forgotpassword_text";
		public static String MESSAGE_REGISTER_SUBJECT = "message_register_subject";
		public static String MESSAGE_REGISTER_TEXT = "message_register_text";

		private Map<String, String> attr;

		public static ConfigMessageTemplate createForgotPasswordMessageTemplate(Map<String, String> map) {
			if (map == null || StringUtils.isBlank(map.get(MESSAGE_FORGOTPASSWORD_SUBJECT))
					|| StringUtils.isBlank(map.get(MESSAGE_FORGOTPASSWORD_TEXT))) {
				// 信息不完整，返回null。
				return null;
			}
			ConfigMessageTemplate tpl = new ConfigMessageTemplate();
			tpl.setAttr(map);
			return tpl;
		}
		
		public static ConfigMessageTemplate createRegisterMessageTemplate(Map<String, String> map) {
			if (map == null || StringUtils.isBlank(map.get(MESSAGE_REGISTER_SUBJECT))
					|| StringUtils.isBlank(map.get(MESSAGE_REGISTER_TEXT))) {
				// 信息不完整，返回null。
				return null;
			}
			ConfigMessageTemplate tpl = new ConfigMessageTemplate();
			tpl.setAttr(map);
			return tpl;
		}

		public Map<String, String> getAttr() {
			if (attr == null) {
				attr = new HashMap<String, String>();
			}
			return this.attr;
		}

		public void setAttr(Map<String, String> attr) {
			this.attr = attr;
		}

		public String getForgotPasswordSubject() {
			return getAttr().get(MESSAGE_FORGOTPASSWORD_SUBJECT);
		}

		public void setForgotPasswordSubject(String subject) {
			getAttr().put(MESSAGE_FORGOTPASSWORD_SUBJECT, subject);
		}

		public String getForgotPasswordText() {
			return getAttr().get(MESSAGE_FORGOTPASSWORD_TEXT);
		}

		public void setForgotPasswordText(String text) {
			getAttr().put(MESSAGE_FORGOTPASSWORD_TEXT, text);
		}
		
		public String getRegisterSubject() {
			return getAttr().get(MESSAGE_REGISTER_SUBJECT);
		}

		public void setRegisterSubject(String subject) {
			getAttr().put(MESSAGE_REGISTER_SUBJECT, subject);
		}

		public String getRegisterText() {
			return getAttr().get(MESSAGE_REGISTER_TEXT);
		}

		public void setRegisterText(String text) {
			getAttr().put(MESSAGE_REGISTER_TEXT, text);
		}
	}

}