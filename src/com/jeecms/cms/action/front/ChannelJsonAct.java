package com.jeecms.cms.action.front;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.common.web.ResponseUtils;

@Controller
public class ChannelJsonAct {
	
	/**
	 * @param parentId
	 * @param siteId
	 * @param hasContentOnly
	 * @param first   查询开始下标
	 * @param count	  查询数量
	 */
	@RequestMapping(value = "/json/channel_list.jspx")
	public void channelList(Integer parentId,Integer siteId,
			Boolean hasContentOnly,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if(hasContentOnly==null){
			hasContentOnly=false;
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=100;
		}
		List<Channel> list;
		if (parentId != null) {
			list = channelMng.getChildListForTag(parentId, hasContentOnly);
		} else {
			if (siteId == null) {
				siteId = 1;
			}
			list = channelMng.getTopListForTag(siteId, hasContentOnly);
		}
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, convertToJson(list.get(i)));
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	/**
	 * id或者path
	 * path和siteId必须一起使用
	 * @param id
	 * @param path
	 * @param siteId
	 */
	@RequestMapping(value = "/json/channel_get.jspx")
	public void channelGet(Integer id,String path,Integer siteId,
			HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		Channel channel;
		if (id != null) {
			channel = channelMng.findById(id);
		} else {
			if(siteId==null){
				siteId=1;
			}
			channel = channelMng.findByPathForTag(path, siteId);
		}
		if (channel != null) {
			JSONObject json=convertToJson(channel);
			ResponseUtils.renderJson(response, json.toString());
		} else {
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	private JSONObject convertToJson(Channel channel) throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", channel.getId());
		json.put("name", channel.getName());
		json.put("url", channel.getUrl());
		json.put("txt", channel.getTxt());
		json.put("path", channel.getPath());
		json.put("title", channel.getTitle());
		json.put("keywords", channel.getKeywords());
		json.put("description", channel.getDescription());
		json.put("deep", channel.getDeep());

		json.put("childCount", channel.getChild().size());
		
		json.put("hasContent", channel.getHasContent());
		json.put("display", channel.getDisplay());
		json.put("path", channel.getPath());
		json.put("link", channel.getLink());
		json.put("titleImg", channel.getTitleImg());
		json.put("contentImg", channel.getContentImg());
		json.put("hasTitleImg", channel.getHasTitleImg());
		json.put("hasContentImg", channel.getHasContentImg());
		json.put("views", channel.getChannelCount().getViews());
		json.put("viewsMonth", channel.getChannelCount().getViewsMonth());
		json.put("viewsWeek", channel.getChannelCount().getViewsWeek());
		json.put("viewsDay", channel.getChannelCount().getViewsDay());
		json.put("siteName", channel.getSite().getName());
		json.put("siteId", channel.getSite().getId());
		json.put("siteUrl", channel.getSite().getUrl());
		json.put("model", channel.getModel().getName());
		json.put("modelId", channel.getModel().getId());
		
		if(channel.getParent()!=null){
			json.put("parentId", channel.getParent().getId());
			json.put("parentName", channel.getParent().getName());
			json.put("parentUrl", channel.getParent().getUrl());
			json.put("parentTxt", channel.getParent().getTxt());
			json.put("parentPath", channel.getParent().getPath());
			json.put("parentTitle", channel.getParent().getTitle());
		}
		
		if(channel.getTopChannel()!=null){
			json.put("topId", channel.getTopChannel().getId());
			json.put("topName", channel.getTopChannel().getName());
			json.put("topUrl", channel.getTopChannel().getUrl());
			json.put("topTxt", channel.getTopChannel().getTxt());
			json.put("topPath", channel.getTopChannel().getPath());
			json.put("topTitle", channel.getTopChannel().getTitle());
		}
		return json;
	}
	@Autowired
	private ChannelMng channelMng;
}

