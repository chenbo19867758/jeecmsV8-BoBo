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
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.common.web.ResponseUtils;

@Controller
public class ContentJsonAct {
	/**
	 * tagIds、topicId、channelIds、siteIds 必须有一个参数有值，否则结果为空
	 * channelOption参数仅在使用 channelIds参数时有效，默认值为1
	 * @param tagIds  tagID优先级最高
	 * @param topicId 专题ID 优先级第二
	 * @param channelIds 栏目ID 优先级第三
	 * @param siteIds 站点ID 优先级第四
	 * @param typeIds 类型ID数组
	 * @param titleImg  是否有标题图
	 * @param recommend  推荐
	 * @param orderBy  排序
	 * @param title   标题检索
	 * @param channelOption  channelOption 1子栏目 2副栏目  0或者channelIds多个值
	 * @param first   查询开始下标
	 * @param count	  查询数量
	 */
	@RequestMapping(value = "/json/content_list.jspx")
	public void contentList(Integer[] tagIds,Integer topicId,
			Integer[] channelIds,Integer[] siteIds,
			Integer[] typeIds,Boolean titleImg,Boolean recommend,Integer orderBy,
			String title, Integer channelOption,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if(orderBy==null){
			orderBy=4;
		}
		if(channelOption==null){
			channelOption=1;
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=100;
		}
		List<Content>contents=null;
		if(tagIds!=null){
			contents=contentMng.getListByTagIdsForTag(tagIds, siteIds,
					channelIds, typeIds, null, titleImg, recommend,
					title,null, orderBy, first, count);
		}else if(topicId!=null){
			contents=contentMng.getListByTopicIdForTag(topicId, siteIds,
					channelIds, typeIds, titleImg, recommend, title,
					null,orderBy, first, count);
		}else if(channelIds!=null){
			contents=contentMng.getListByChannelIdsForTag(channelIds,
					typeIds, titleImg, recommend, title,
					null,orderBy, channelOption,first, count);
		}else if(siteIds!=null){
			contents=contentMng.getListBySiteIdsForTag(siteIds, typeIds,
					titleImg, recommend, title,
					null,orderBy, first, count);
		}
		JSONArray jsonArray=new JSONArray();
		if(contents!=null&&contents.size()>0){
			for(int i=0;i<contents.size();i++){
				jsonArray.put(i, convertToJson(contents.get(i)));
			}
		}
		ResponseUtils.renderJson(response, jsonArray.toString());
	}
	
	@RequestMapping(value = "/json/content_get.jspx")
	public void contentGet(Integer contentId, HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		if (contentId == null) {
			ResponseUtils.renderJson(response, "[]");
			return;
		}
		Content content=contentMng.findById(contentId);
		if (content != null) {
			JSONObject json=convertToJson(content);
			ResponseUtils.renderJson(response, json.toString());
		} else {
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	private JSONObject convertToJson(Content content) throws JSONException{
		JSONObject json=new JSONObject();
		json.put("id", content.getId());
		json.put("url", content.getUrl());
		json.put("txt", content.getTxt());
		json.put("txt1", content.getTxt1());
		json.put("txt2", content.getTxt2());
		json.put("txt3", content.getTxt3());
		json.put("tagStr", content.getTagStr());
		json.put("sortDate", content.getSortDate());
		json.put("topLevel", content.getTopLevel());
		json.put("hasTitleImg", content.getHasTitleImg());
		json.put("recommend", content.getRecommend());
		json.put("status", content.getStatus());
		json.put("viewsDay", content.getViewsDay());
		json.put("commentsDay", content.getCommentsDay());
		json.put("downloadsDay", content.getDownloadsDay());
		json.put("upsDay", content.getUpsDay());
		json.put("score", content.getScore());
		json.put("recommendLevel", content.getRecommendLevel());
		json.put("title", content.getTitle());
		json.put("shortTitle", content.getShortTitle());
		json.put("description", content.getDescription());
		json.put("author", content.getAuthor());
		json.put("origin", content.getOrigin());
		json.put("originUrl", content.getOriginUrl());
		json.put("releaseDate", content.getReleaseDate());
		json.put("mediaPath", content.getMediaPath());
		json.put("mediaType", content.getMediaType());
		json.put("titleColor", content.getTitleColor());
		json.put("bold", content.getBold());
		json.put("titleImg", content.getTitleImg());
		json.put("contentImg", content.getContentImg());
		json.put("typeImg", content.getTypeImg());
		json.put("link", content.getLink());
		json.put("views", content.getViews());
		json.put("viewsMonth", content.getViewsMonth());
		json.put("viewsWeek", content.getViewsWeek());
		json.put("viewsDay", content.getViewsDay());
		json.put("comments", content.getComments());
		json.put("commentsMonth", content.getContentCount().getCommentsMonth());
		json.put("commentsDay", content.getContentCount().getCommentsDay());
		json.put("downloads", content.getContentCount().getDownloads());
		json.put("downloadsMonth", content.getContentCount().getDownloadsMonth());
		json.put("downloadsWeek", content.getContentCount().getDownloadsWeek());
		json.put("downloadsDay", content.getContentCount().getDownloadsDay());
		json.put("ups", content.getContentCount().getUps());
		json.put("upsMonth", content.getContentCount().getUpsMonth());
		json.put("upsWeek", content.getContentCount().getUpsWeek());
		json.put("upsDay", content.getContentCount().getUpsDay());
		json.put("downs", content.getContentCount().getDowns());
		json.put("typId", content.getType().getId());
		json.put("typeName", content.getType().getName());
		json.put("siteName", content.getSiteName());
		json.put("siteId", content.getSiteId());
		json.put("siteUrl", content.getSiteUrl());
		json.put("channel", content.getChannel().getName());
		json.put("channelId", content.getChannel().getId());
		json.put("channelUrl", content.getCtgUrl());
		json.put("model", content.getModel().getName());
		json.put("modelId", content.getModel().getId());
		json.put("commentCount", content.getCommentsCount());
		json.put("attachmentPaths", content.getAttachmentPathStr());
		json.put("attachmentNames", content.getAttachmentNameStr());
		json.put("PicPaths", content.getPicPathStr());
		json.put("picDescs", content.getPicDescStr());
		return json;
	}
	@Autowired
	private ContentMng contentMng;
}

