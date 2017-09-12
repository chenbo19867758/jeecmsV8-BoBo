package com.jeecms.core.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class CmsConfigAttr {
	public CmsConfigAttr() {
	}

	public CmsConfigAttr(Map<String, String> attr) {
		this.attr = attr;
	}

	private Map<String, String> attr;

	public Map<String, String> getAttr() {
		if (attr == null) {
			attr = new HashMap<String, String>();
		}
		return attr;
	}

	public void setAttr(Map<String, String> attr) {
		this.attr = attr;
	}


	public static final String PICTURENEW = "new_picture";
	public static final String DAYNEW = "day";
	public static final String PREVIEW = "preview";
	public static final String QQ_ENABLE = "qqEnable";
	public static final String QQ_ID = "qqID";
	public static final String QQ_KEY = "qqKey";
	public static final String SINA_ENABLE = "sinaEnable";
	public static final String SINA_ID = "sinaID";
	public static final String SINA_KEY = "sinaKey";
	public static final String QQWEBO_ENABLE = "qqWeboEnable";
	public static final String QQWEBO_ID = "qqWeboID";
	public static final String QQWEBO_KEY = "qqWeboKey";
	public static final String WEIXIN_ENABLE = "weixinEnable";
	public static final String WEIXIN_ID = "weixinID";
	public static final String WEIXIN_KEY = "weixinKey";
	public static final String SSO_ENABLE = "ssoEnable";
	public static final String FLOW_SWITCH = "flowSwitch";
	public static final String CONTENT_FRESH_MINUTE = "contentFreshMinute";
	public static final String CODE_IMG_WIDTH = "codeImgWidth";
	public static final String CODE_IMG_HEIGHT = "codeImgHeight";
	
	public String getPictureNew() {
		return getAttr().get(PICTURENEW);
	}
	
	public int getDayNew() {
		String day=getAttr().get(DAYNEW);
		if(StringUtils.isNotBlank(day)){
			return Integer.parseInt(day);
		}else{
			return 0;
		}
	}
	
	public Boolean getSsoEnable() {
		String enable = getAttr().get(SSO_ENABLE);
		return !"false".equals(enable);
	}
	
	public Boolean getFlowSwitch() {
		String flowSwitch = getAttr().get(FLOW_SWITCH);
		return !"false".equals(flowSwitch);
	}
	
	
	public void setPictureNew(String path) {
		getAttr().put(PICTURENEW, path);
	}
	
	public void setDayNew(Integer day) {
		getAttr().put(DAYNEW, day.toString());
	}
	
	public Boolean getPreview() {
		String preview = getAttr().get(PREVIEW);
		return !"false".equals(preview);
	}

	/**
	 * 设置是否开启内容预览
	 * 
	 * @param preview
	 */
	public void setPreview(boolean preview) {
		getAttr().put(PREVIEW, String.valueOf(preview));
	}
	
	public Boolean getQqEnable() {
		String enable = getAttr().get(QQ_ENABLE);
		return !"false".equals(enable);
	}
	
	public String getQqID() {
		return getAttr().get(QQ_ID);
	}
	
	public String getQqKey() {
		return getAttr().get(QQ_KEY);
	}
	
	public Boolean getSinaEnable() {
		String enable = getAttr().get(SINA_ENABLE);
		return !"false".equals(enable);
	}
	
	public String getSinaID() {
		return getAttr().get(SINA_ID);
	}
	
	public String getSinaKey() {
		return getAttr().get(SINA_KEY);
	}
	
	public Boolean getQqWeboEnable() {
		String enable = getAttr().get(QQWEBO_ENABLE);
		return !"false".equals(enable);
	}
	
	public String getQqWeboID() {
		return getAttr().get(QQWEBO_ID);
	}
	
	public String getQqWeboKey() {
		return getAttr().get(QQWEBO_KEY);
	}
	
	public Boolean getWeixinEnable() {
		String enable = getAttr().get(WEIXIN_ENABLE);
		return !"false".equals(enable);
	}
	
	public String getWeixinID() {
		return getAttr().get(WEIXIN_ID);
	}
	
	public Integer getContentFreshMinute() {
		return Integer.parseInt(getAttr().get(CONTENT_FRESH_MINUTE));
	}
	
	public String getWeixinKey() {
		return getAttr().get(WEIXIN_KEY);
	}
	
	public void setQqEnable(boolean enable) {
		getAttr().put(QQ_ENABLE, String.valueOf(enable));
	}
	
	public void setQqID(String id) {
		getAttr().put(QQ_ID, id);
	}
	
	public void setQqKey(String key) {
		getAttr().put(QQ_KEY, key);
	}
	
	
	public void setSinaEnable(boolean enable) {
		getAttr().put(SINA_ENABLE, String.valueOf(enable));
	}
	
	public void setFlowSwitch(boolean flowSwitch) {
		getAttr().put(FLOW_SWITCH, String.valueOf(flowSwitch));
	}
	
	public void setContentFreshMinute(Integer minute) {
		getAttr().put(CONTENT_FRESH_MINUTE, String.valueOf(minute));
	}
	
	public void setSinaID(String id) {
		getAttr().put(SINA_ID,id);
	}
	
	public void setSinaKey(String key) {
		getAttr().put(SINA_KEY,key);
	}
	
	public void setQqWeboEnable(boolean enable) {
		getAttr().put(QQWEBO_ENABLE, String.valueOf(enable));
	}
	
	public void setQqWeboID(String id) {
		getAttr().put(QQWEBO_ID, id);
	}
	
	public void setQqWeboKey(String key) {
		getAttr().put(QQWEBO_KEY, key);
	}
	
	public void setWeixinEnable(boolean enable) {
		getAttr().put(WEIXIN_ENABLE, String.valueOf(enable));
	}
	
	public void setWeixinID(String id) {
		getAttr().put(WEIXIN_ID, id);
	}
	
	public void setWeixinKey(String key) {
		getAttr().put(WEIXIN_KEY, key);
	}
	
	public int getCodeImgWidth() {
		String width=getAttr().get(CODE_IMG_WIDTH);
		if(StringUtils.isNotBlank(width)){
			return Integer.parseInt(width);
		}else{
			return 100;
		}
	}
	
	public void setCodeImgWidth(Integer width) {
		getAttr().put(CODE_IMG_WIDTH, width.toString());
	}
	
	public int getCodeImgHeight() {
		String height=getAttr().get(CODE_IMG_HEIGHT);
		if(StringUtils.isNotBlank(height)){
			return Integer.parseInt(height);
		}else{
			return 100;
		}
	}
	
	public void setCodeImgHeight(Integer height) {
		getAttr().put(CODE_IMG_HEIGHT, height.toString());
	}

}
