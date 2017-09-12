package com.jeecms.core.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.jeecms.core.entity.base.BaseCmsConfig;

public class CmsConfig extends BaseCmsConfig {
	private static final long serialVersionUID = 1L;
	public static final String VERSION = "version";

	public String getVersion() {
		return getAttr().get(VERSION);
	}
	
	public Boolean getSsoEnable(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getSsoEnable();
	}
	
	public Boolean getFlowSwitch(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getFlowSwitch();
	}
	
	public Map<String,String> getSsoAttr() {
		Map<String,String>ssoMap=new HashMap<String, String>();
		Map<String,String>attr=getAttr();
		for(String ssoKey:attr.keySet()){
			if(ssoKey.startsWith("sso_")){
				ssoMap.put(ssoKey, attr.get(ssoKey));
			}
		}
		return ssoMap;
	}
	
	public List<String> getSsoAuthenticateUrls() {
		Map<String,String>ssoMap=getSsoAttr();
		List<String>values=new ArrayList<String>();
		for(String key:ssoMap.keySet()){
			values.add(ssoMap.get(key));
		}
		return values;
	}
	
	
	public MemberConfig getMemberConfig() {
		MemberConfig memberConfig = new MemberConfig(getAttr());
		return memberConfig;
	}

	public void setMemberConfig(MemberConfig memberConfig) {
		getAttr().putAll(memberConfig.getAttr());
	}
	
	public CmsConfigAttr getConfigAttr() {
		CmsConfigAttr configAttr = new CmsConfigAttr(getAttr());
		return configAttr;
	}

	public void setConfigAttr(CmsConfigAttr configAttr) {
		getAttr().putAll(configAttr.getAttr());
	}
	
	public Boolean getQqEnable(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getQqEnable();
	}
	
	public Boolean getSinaEnable(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getSinaEnable();
	}
	
	public Boolean getQqWeboEnable(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getQqWeboEnable();
	}
	
	public String getQqID(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getQqID();
	}
	
	public String getSinaID(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getSinaID();
	}
	
	public String getQqWeboID(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getQqWeboID();
	}
	
	public Boolean getWeixinEnable(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getWeixinEnable();
	}
	
	public String getWeixinID(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getWeixinID();
	}
	
	public String getWeixinKey(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getWeixinKey();
	}
	
	public Integer getContentFreshMinute(){
		CmsConfigAttr configAttr=getConfigAttr();
		return configAttr.getContentFreshMinute();
	}

	public void blankToNull() {
		// oracle varchar2把空串当作null处理，这里为了统一这个特征，特做此处理。
		if (StringUtils.isBlank(getProcessUrl())) {
			setProcessUrl(null);
		}
		if (StringUtils.isBlank(getContextPath())) {
			setContextPath(null);
		}
		if (StringUtils.isBlank(getServletPoint())) {
			setServletPoint(null);
		}
	}

	/* [CONSTRUCTOR MARKER BEGIN] */
	public CmsConfig() {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsConfig(java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsConfig(java.lang.Integer id, java.lang.String dbFileUri,
			java.lang.Boolean uploadToDb, java.lang.String defImg,
			java.lang.String loginUrl, java.util.Date countClearTime,
			java.util.Date countCopyTime, java.lang.String downloadCode,
			java.lang.Integer downloadTime) {

		super(id, dbFileUri, uploadToDb, defImg, loginUrl, countClearTime,
				countCopyTime, downloadCode, downloadTime);
	}

	/* [CONSTRUCTOR MARKER END] */

}