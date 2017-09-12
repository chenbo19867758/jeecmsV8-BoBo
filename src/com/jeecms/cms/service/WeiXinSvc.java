package com.jeecms.cms.service;

import java.util.Set;

import com.jeecms.cms.entity.main.Content;


/**
 * @author Tom
 */
public interface WeiXinSvc {
	/**
	 * 获取公众号的token
	 * @return  access_token
	 * access_token需要有缓存 微信限制2000次/天
	 */
	public String  getToken();
	/**
	 * 获取公众号的订阅用户
	 * @return
	 */
	public Set<String>  getUsers(String access_token);
	/**
	 * 发送纯文本消息
	 * @param title
	 * @param content
	 */
	public void  sendText(String access_token,String content);
	/**
	 * 上传多媒体视频
	 * @param access_token
	 * @param filePath
	 */
	public String  uploadFile(String access_token,String filePath,String type);
	/**
	 * 发送视频消息
	 * @param media_id
	 * @param title
	 * @param description
	 */
	public void  sendVedio(String access_token,String title,String description,String media_id);
	/**
	 * 发送图文消息
	 * @param url
	 * @param picurl
	 * @param title
	 * @param description
	 */
	public void  sendContent(String access_token,String title,String description,String url,String picurl);
	
	/**
	 * 创建自定义菜单
	 * @param menus 自定义菜单内容
	 */
	public void createMenu(String menus);
	/**
	 * 微信群发消息
	 * @param beans
	 */
	public void sendTextToAllUser(Content[] beans);
	
}
