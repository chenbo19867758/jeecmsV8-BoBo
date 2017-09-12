package com.jeecms.cms.manager.main.impl;

import static com.jeecms.cms.entity.main.ContentCheck.DRAFT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.jeecms.cms.dao.main.ContentDao;
import com.jeecms.cms.entity.assist.CmsFile;
import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.CmsTopic;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentCheck;
import com.jeecms.cms.entity.main.ContentCount;
import com.jeecms.cms.entity.main.ContentExt;
import com.jeecms.cms.entity.main.ContentRecord.ContentOperateType;
import com.jeecms.cms.entity.main.ContentTag;
import com.jeecms.cms.entity.main.ContentTxt;
import com.jeecms.cms.entity.main.Channel.AfterCheckEnum;
import com.jeecms.cms.entity.main.Content.ContentStatus;
import com.jeecms.cms.entity.main.ContentCharge;
import com.jeecms.cms.manager.assist.CmsCommentMng;
import com.jeecms.cms.manager.assist.CmsFileMng;
import com.jeecms.cms.manager.main.ChannelCountMng;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.CmsTopicMng;
import com.jeecms.cms.manager.main.ContentChargeMng;
import com.jeecms.cms.manager.main.ContentCheckMng;
import com.jeecms.cms.manager.main.ContentCountMng;
import com.jeecms.cms.manager.main.ContentExtMng;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.cms.manager.main.ContentRecordMng;
import com.jeecms.cms.manager.main.ContentTagMng;
import com.jeecms.cms.manager.main.ContentTxtMng;
import com.jeecms.cms.manager.main.ContentTypeMng;
import com.jeecms.cms.service.ChannelDeleteChecker;
import com.jeecms.cms.service.ContentListener;
import com.jeecms.cms.staticpage.StaticPageSvc;
import com.jeecms.cms.staticpage.exception.ContentNotCheckedException;
import com.jeecms.cms.staticpage.exception.GeneratedZeroStaticPageException;
import com.jeecms.cms.staticpage.exception.StaticPageNotOpenException;
import com.jeecms.cms.staticpage.exception.TemplateNotFoundException;
import com.jeecms.cms.staticpage.exception.TemplateParseException;
import com.jeecms.common.hibernate4.Updater;
import com.jeecms.common.page.Pagination;
import com.jeecms.core.entity.CmsGroup;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.entity.CmsUserSite;
import com.jeecms.core.manager.CmsGroupMng;
import com.jeecms.core.manager.CmsUserMng;

import freemarker.template.TemplateException;

@Service
@Transactional
public class ContentMngImpl implements ContentMng, ChannelDeleteChecker {
	@Transactional(readOnly = true)
	public Pagination getPageByRight(String title, Integer typeId,Integer currUserId,
			Integer inputUserId, boolean topLevel, boolean recommend,
			ContentStatus status, Byte checkStep, Integer siteId,
			Integer channelId,Integer userId, int orderBy, int pageNo,
			int pageSize) {
		CmsUser user = cmsUserMng.findById(userId);
		CmsUserSite us = user.getUserSite(siteId);
		Pagination p;
		boolean allChannel = us.getAllChannel();
		boolean selfData = user.getSelfAdmin();
		if (allChannel && selfData) {
			// 拥有所有栏目权限，只能管理自己的数据
			p = dao.getPageBySelf(title, typeId, inputUserId, topLevel,
					recommend, status, checkStep, siteId, channelId, userId,
					orderBy, pageNo, pageSize);
		} else if (allChannel && !selfData) {
			// 拥有所有栏目权限，能够管理不属于自己的数据
			p = dao.getPage(title, typeId,currUserId, inputUserId, topLevel, recommend,
					status, checkStep, siteId,null,channelId,orderBy, pageNo,
					pageSize);
		} else {
			p = dao.getPageByRight(title, typeId, currUserId,inputUserId, topLevel,
					recommend, status, checkStep, siteId, channelId,userId,
					selfData, orderBy, pageNo, pageSize);
		}
		return p;
	}
	
	public Pagination getPageBySite(String title, Integer typeId,Integer inputUserId,boolean topLevel,
			boolean recommend,ContentStatus status, Integer siteId,int orderBy, int pageNo,int pageSize){
		return dao.getPage(title, typeId, null, inputUserId, topLevel, recommend, status, null, siteId, null, null, orderBy, pageNo, pageSize);
	}

	public Pagination getPageForMember(String title, Integer channelId,Integer siteId,Integer modelId, Integer memberId, int pageNo, int pageSize) {
		return dao.getPage(title, null,memberId,memberId, false, false,ContentStatus.all, null, siteId,modelId,  channelId, 0, pageNo,pageSize);
	}
	
	@Transactional(readOnly = true)
	public  List<Content> getExpiredTopLevelContents(byte topLevel,Date expiredDay){
		return dao.getExpiredTopLevelContents(topLevel,expiredDay);
	}
	
	@Transactional(readOnly = true)
	public  List<Content> getPigeonholeContents(Date pigeonholeDay){
		return dao.getPigeonholeContents(pigeonholeDay);
	}
	
	@Transactional(readOnly = true)
	public Content getSide(Integer id, Integer siteId, Integer channelId,
			boolean next) {
		return dao.getSide(id, siteId, channelId, next, true);
	}

	@Transactional(readOnly = true)
	public List<Content> getListByIdsForTag(Integer[] ids, int orderBy) {
		if (ids.length == 1) {
			Content content = findById(ids[0]);
			List<Content> list;
			if (content != null) {
				list = new ArrayList<Content>(1);
				list.add(content);
			} else {
				list = new ArrayList<Content>(0);
			}
			return list;
		} else {
			return dao.getListByIdsForTag(ids, orderBy);
		}
	}

	@Transactional(readOnly = true)
	public Pagination getPageBySiteIdsForTag(Integer[] siteIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title,Map<String,String[]>attr, int orderBy, int pageNo, int pageSize) {
		return dao.getPageBySiteIdsForTag(siteIds, typeIds, titleImg,
				recommend, title,attr,orderBy, pageNo, pageSize);
	}

	@Transactional(readOnly = true)
	public List<Content> getListBySiteIdsForTag(Integer[] siteIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title,Map<String,String[]>attr,int orderBy, Integer first, Integer count) {
		return dao.getListBySiteIdsForTag(siteIds, typeIds, titleImg,
				recommend, title,attr, orderBy, first, count);
	}

	@Transactional(readOnly = true)
	public Pagination getPageByChannelIdsForTag(Integer[] channelIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title,Map<String,String[]>attr, int orderBy, int option, int pageNo, int pageSize) {
		return dao.getPageByChannelIdsForTag(channelIds, typeIds, titleImg,
				recommend, title,attr, orderBy, option, pageNo, pageSize);
	}

	@Transactional(readOnly = true)
	public List<Content> getListByChannelIdsForTag(Integer[] channelIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title,Map<String,String[]>attr,int orderBy, int option,Integer first, Integer count) {
		return dao.getListByChannelIdsForTag(channelIds, typeIds, titleImg,
				recommend, title,attr, orderBy, option,first, count);
	}

	@Transactional(readOnly = true)
	public Pagination getPageByChannelPathsForTag(String[] paths,
			Integer[] siteIds, Integer[] typeIds, Boolean titleImg,
			Boolean recommend, String title,Map<String,String[]>attr, int orderBy, int pageNo,
			int pageSize) {
		return dao.getPageByChannelPathsForTag(paths, siteIds, typeIds,
				titleImg, recommend, title,attr, orderBy, pageNo, pageSize);
	}

	@Transactional(readOnly = true)
	public List<Content> getListByChannelPathsForTag(String[] paths,
			Integer[] siteIds, Integer[] typeIds, Boolean titleImg,
			Boolean recommend, String title,
			Map<String,String[]>attr, int orderBy, Integer first,
			Integer count) {
		return dao.getListByChannelPathsForTag(paths, siteIds, typeIds,
				titleImg, recommend, title,attr, orderBy, first, count);
	}

	@Transactional(readOnly = true)
	public Pagination getPageByTopicIdForTag(Integer topicId,
			Integer[] siteIds, Integer[] channelIds, Integer[] typeIds,
			Boolean titleImg, Boolean recommend, String title,Map<String,String[]>attr,int orderBy,
			int pageNo, int pageSize) {
		return dao.getPageByTopicIdForTag(topicId, siteIds, channelIds,
				typeIds, titleImg, recommend, title,attr, orderBy, pageNo, pageSize);
	}

	@Transactional(readOnly = true)
	public List<Content> getListByTopicIdForTag(Integer topicId,
			Integer[] siteIds, Integer[] channelIds, Integer[] typeIds,
			Boolean titleImg, Boolean recommend, String title,Map<String,String[]>attr,int orderBy,
			Integer first, Integer count) {
		return dao.getListByTopicIdForTag(topicId, siteIds, channelIds,
				typeIds, titleImg, recommend, title,attr, orderBy, first, count);
	}

	@Transactional(readOnly = true)
	public Pagination getPageByTagIdsForTag(Integer[] tagIds,
			Integer[] siteIds, Integer[] channelIds, Integer[] typeIds,
			Integer excludeId, Boolean titleImg, Boolean recommend,
			String title, Map<String,String[]>attr,int orderBy, int pageNo, int pageSize) {
		return dao.getPageByTagIdsForTag(tagIds, siteIds, channelIds, typeIds,
				excludeId, titleImg, recommend, title,attr,orderBy, pageNo,
				pageSize);
	}

	@Transactional(readOnly = true)
	public List<Content> getListByTagIdsForTag(Integer[] tagIds,
			Integer[] siteIds, Integer[] channelIds, Integer[] typeIds,
			Integer excludeId, Boolean titleImg, Boolean recommend,
			String title, Map<String,String[]>attr,int orderBy, Integer first, Integer count) {
		return dao.getListByTagIdsForTag(tagIds, siteIds,channelIds, typeIds,
				excludeId, titleImg, recommend, title,attr, orderBy, first, count);
	}

	@Transactional(readOnly = true)
	public Content findById(Integer id) {
		Content entity = dao.findById(id);
		return entity;
	}

	public Content save(Content bean, ContentExt ext, ContentTxt txt,
			Integer[] channelIds, Integer[] topicIds, Integer[] viewGroupIds,
			String[] tagArr, String[] attachmentPaths,
			String[] attachmentNames, String[] attachmentFilenames,
			String[] picPaths, String[] picDescs, Integer channelId,
			Integer typeId, Boolean draft,Boolean contribute, 
			Short charge,Double chargeAmount,CmsUser user, boolean forMember) {
		if(charge==null){
			charge=ContentCharge.MODEL_FREE;
		}
		saveContent(bean, ext, txt, channelId, typeId, draft,contribute,user, forMember);
		// 保存副栏目
		if (channelIds != null && channelIds.length > 0) {
			for (Integer cid : channelIds) {
				bean.addToChannels(channelMng.findById(cid));
			}
		}
		// 主栏目也作为副栏目一并保存，方便查询，提高效率。
		Channel channel=channelMng.findById(channelId);
		bean.addToChannels(channel);
		// 保存专题
		if (topicIds != null && topicIds.length > 0) {
			for (Integer tid : topicIds) {
				if(tid!=null&&tid!=0){
					bean.addToTopics(cmsTopicMng.findById(tid));
				}
			}
		}
		// 保存浏览会员组
		if (viewGroupIds != null && viewGroupIds.length > 0) {
			for (Integer gid : viewGroupIds) {
				bean.addToGroups(cmsGroupMng.findById(gid));
			}
		}
		// 保存标签
		List<ContentTag> tags = contentTagMng.saveTags(tagArr);
		bean.setTags(tags);
		// 保存附件
		if (attachmentPaths != null && attachmentPaths.length > 0) {
			for (int i = 0, len = attachmentPaths.length; i < len; i++) {
				if (!StringUtils.isBlank(attachmentPaths[i])) {
					bean.addToAttachmemts(attachmentPaths[i],
							attachmentNames[i], attachmentFilenames[i]);
				}
			}
		}
		// 保存图片集
		if (picPaths != null && picPaths.length > 0) {
			for (int i = 0, len = picPaths.length; i < len; i++) {
				if (!StringUtils.isBlank(picPaths[i])) {
					bean.addToPictures(picPaths[i], picDescs[i]);
				}
			}
		}
		//文章操作记录
		contentRecordMng.record(bean, user, ContentOperateType.add);
		//栏目内容发布数（未审核通过的也算）
		channelCountMng.afterSaveContent(channel);
		
		contentChargeMng.save(chargeAmount,charge,bean);
		// 执行监听器
		afterSave(bean);
		return bean;
	}
	
	//导入word执行
	public Content save(Content bean, ContentExt ext, ContentTxt txt,
			Integer channelId,Integer typeId, Boolean draft, CmsUser user,
			boolean forMember){
		saveContent(bean, ext, txt,channelId, typeId, draft,false, user, forMember);
		// 执行监听器
		afterSave(bean);
		return bean;
	}
	
	private Content saveContent(Content bean, ContentExt ext, ContentTxt txt,
			Integer channelId,Integer typeId, Boolean draft,
			Boolean contribute,CmsUser user, boolean forMember){
		Channel channel = channelMng.findById(channelId);
		bean.setChannel(channel);
		bean.setType(contentTypeMng.findById(typeId));
		bean.setUser(user);
		Byte userStep;
		if (forMember) {
			// 会员的审核级别按0处理
			userStep = 0;
		} else {
			CmsSite site = bean.getSite();
			userStep = user.getCheckStep(site.getId());
		}
		// 流程处理
		if(contribute!=null&&contribute){
			bean.setStatus(ContentCheck.CONTRIBUTE);
		}else if (draft != null && draft) {
			// 草稿
			bean.setStatus(ContentCheck.DRAFT);
		} else {
			if (userStep >= bean.getChannel().getFinalStepExtends()) {
				bean.setStatus(ContentCheck.CHECKED);
			} else {
				bean.setStatus(ContentCheck.CHECKING);
			}
		}
		// 是否有标题图
		bean.setHasTitleImg(!StringUtils.isBlank(ext.getTitleImg()));
		bean.init();
		// 执行监听器
		preSave(bean);
		dao.save(bean);
		contentExtMng.save(ext, bean);
		contentTxtMng.save(txt, bean);
		ContentCheck check = new ContentCheck();
		check.setCheckStep(userStep);
		contentCheckMng.save(check, bean);
		contentCountMng.save(new ContentCount(), bean);
		return bean;
	}

	public Content update(Content bean, ContentExt ext, ContentTxt txt,
			String[] tagArr, Integer[] channelIds, Integer[] topicIds,
			Integer[] viewGroupIds, String[] attachmentPaths,
			String[] attachmentNames, String[] attachmentFilenames,
			String[] picPaths, String[] picDescs, Map<String, String> attr,
			Integer channelId, Integer typeId, Boolean draft,
			Short charge,Double chargeAmount,CmsUser user,boolean forMember) {
		Content entity = findById(bean.getId());
		// 执行监听器
		List<Map<String, Object>> mapList = preChange(entity);
		// 更新主表
		Updater<Content> updater = new Updater<Content>(bean);
		bean = dao.updateByUpdater(updater);
		// 审核更新处理，如果站点设置为审核退回，且当前文章审核级别大于管理员审核级别，则将文章审核级别修改成管理员的审核级别。
		Byte userStep;
		if (forMember) {
			// 会员的审核级别按0处理
			userStep = 0;
		} else {
			CmsSite site = bean.getSite();
			userStep = user.getCheckStep(site.getId());
		}
		AfterCheckEnum after = bean.getChannel().getAfterCheckEnum();
		if (after == AfterCheckEnum.BACK_UPDATE
				&& bean.getCheckStep() > userStep) {
			bean.getContentCheck().setCheckStep(userStep);
			if (bean.getCheckStep() >= bean.getChannel().getFinalStepExtends()) {
				bean.setStatus(ContentCheck.CHECKED);
			} else {
				bean.setStatus(ContentCheck.CHECKING);
			}
		}
		//修改后退回
		if (after == AfterCheckEnum.BACK_UPDATE) {
			--userStep;
			if(userStep<0){
				userStep=0;
			}
			reject(bean.getId(), user, userStep,"");
		}
		// 草稿
		if (draft != null) {
			if (draft) {
				bean.setStatus(DRAFT);
			} else {
				if (bean.getStatus() == DRAFT) {
					if (bean.getCheckStep() >= bean.getChannel()
							.getFinalStepExtends()) {
						bean.setStatus(ContentCheck.CHECKED);
					} else {
						bean.setStatus(ContentCheck.CHECKING);
					}
				}
			}
		}
		// 是否有标题图
		bean.setHasTitleImg(!StringUtils.isBlank(ext.getTitleImg()));
		// 更新栏目
		if (channelId != null) {
			bean.setChannel(channelMng.findById(channelId));
		}
		// 更新类型
		if (typeId != null) {
			bean.setType(contentTypeMng.findById(typeId));
		}
		// 更新扩展表
		contentExtMng.update(ext);
		// 更新文本表
		contentTxtMng.update(txt, bean);
		//收费变更
		contentChargeMng.afterContentUpdate(bean, charge, chargeAmount);
		// 更新属性表
		if (attr != null) {
			Map<String, String> attrOrig = bean.getAttr();
			attrOrig.clear();
			attrOrig.putAll(attr);
		}
		// 更新副栏目表
		Set<Channel> channels = bean.getChannels();
		channels.clear();
		if (channelIds != null && channelIds.length > 0) {
			for (Integer cid : channelIds) {
				channels.add(channelMng.findById(cid));
			}
		}
		channels.add(bean.getChannel());
		// 更新专题表
		Set<CmsTopic> topics = bean.getTopics();
		topics.clear();
		if (topicIds != null && topicIds.length > 0) {
			for (Integer tid : topicIds) {
				if(tid!=null&&tid!=0){
					topics.add(cmsTopicMng.findById(tid));
				}
			}
		}
		// 更新浏览会员组
		Set<CmsGroup> groups = bean.getViewGroups();
		groups.clear();
		if (viewGroupIds != null && viewGroupIds.length > 0) {
			for (Integer gid : viewGroupIds) {
				groups.add(cmsGroupMng.findById(gid));
			}
		}
		// 更新标签
		contentTagMng.updateTags(bean.getTags(), tagArr);
		// 更新附件
		bean.getAttachments().clear();
		if (attachmentPaths != null && attachmentPaths.length > 0) {
			for (int i = 0, len = attachmentPaths.length; i < len; i++) {
				if (!StringUtils.isBlank(attachmentPaths[i])) {
					bean.addToAttachmemts(attachmentPaths[i],
							attachmentNames[i], attachmentFilenames[i]);
				}
			}
		}
		// 更新图片集
		bean.getPictures().clear();
		if (picPaths != null && picPaths.length > 0) {
			for (int i = 0, len = picPaths.length; i < len; i++) {
				if (!StringUtils.isBlank(picPaths[i])) {
					bean.addToPictures(picPaths[i], picDescs[i]);
				}
			}
		}
		contentRecordMng.record(bean, user, ContentOperateType.edit);
		// 执行监听器
		afterChange(bean, mapList);
		return bean;
	}
	
	public Content update(Content bean){
		Updater<Content> updater = new Updater<Content>(bean);
		bean = dao.updateByUpdater(updater);
		return bean;
	}
	
	
	public Content update(CmsUser user,Content bean,ContentOperateType operate){
		contentRecordMng.record(bean, user, operate);
		return update(bean);
	}
	
	public Content updateByChannelIds(Integer contentId,Integer[]channelIds){
		Content bean=findById(contentId);
		Set<Channel>channels=bean.getChannels();
		if (channelIds != null && channelIds.length > 0) {
		//	channels.clear();
		//	channels.add(bean.getChannel());
			for (Integer cid : channelIds) {
				channels.add(channelMng.findById(cid));
			}
		}
		return bean;
	}
	
	public Content addContentToTopics(Integer contentId,Integer[]topicIds){
		Content bean=findById(contentId);
		Set<CmsTopic>topics=bean.getTopics();
		if (topicIds != null && topicIds.length > 0) {
			for (Integer tid : topicIds) {
				topics.add(cmsTopicMng.findById(tid));
			}
		}
		return bean;
	}

	public Content check(Integer id, CmsUser user) {
		Content content = findById(id);
		List<Map<String, Object>> mapList = preChange(content);
		ContentCheck check = content.getContentCheck();
		byte userStep = user.getCheckStep(content.getSite().getId());
		byte contentStep = check.getCheckStep();
		byte finalStep = content.getChannel().getFinalStepExtends();
		// 用户审核级别小于当前审核级别，则不能审核
		if (userStep < contentStep) {
			return content;
		}
		check.setRejected(false);
		// 上级审核，清除退回意见。自我审核不清除退回意见。
		if (userStep > contentStep) {
			check.setCheckOpinion(null);
		}
		check.setCheckStep(userStep);
		// 终审
		if (userStep >= finalStep) {
			content.setStatus(ContentCheck.CHECKED);
			// 终审，清除退回意见
			check.setCheckOpinion(null);
			//终审，设置审核者
			check.setReviewer(user);
			check.setCheckDate(Calendar.getInstance().getTime());
		}
		contentRecordMng.record(content, user, ContentOperateType.check);
		// 执行监听器
		afterChange(content, mapList);
		return content;
	}

	public Content[] check(Integer[] ids, CmsUser user) {
		Content[] beans = new Content[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = check(ids[i], user);
		}
		return beans;
	}

	public Content reject(Integer id, CmsUser user, Byte step,  String opinion) {
		Content content = findById(id);
		// 执行监听器
		List<Map<String, Object>> mapList = preChange(content);
		Integer siteId = content.getSite().getId();
		byte userStep = user.getCheckStep(siteId);
		byte contentStep = content.getCheckStep();
		// 用户审核级别小于当前审核级别，则不能退回
		if (userStep < contentStep) {
			return content;
		}
		ContentCheck check = content.getContentCheck();
		if (!StringUtils.isBlank(opinion)) {
			check.setCheckOpinion(opinion);
		}
		check.setRejected(true);
		// 退回稿件一律为未终审
		content.setStatus(ContentCheck.CHECKING);

		if (step != null) {
			// 指定退回级别，不能大于自身级别
			if (step < userStep) {
				check.setCheckStep(step);
			} else {
				check.setCheckStep(userStep);
			}
		} else {
			// 未指定退回级别
			if (contentStep < userStep) {
				// 文档级别小于用户级别，为审核时退回，文档审核级别不修改。
			} else if (contentStep == userStep) {
				// 文档级别等于用户级别，为退回时退回，文档审核级别减一级。
				check.setCheckStep((byte) (check.getCheckStep() - 1));
			}
		}
		contentRecordMng.record(content, user, ContentOperateType.rejected);
		// 执行监听器
		afterChange(content, mapList);
		return content;
	}

	public Content[] reject(Integer[] ids, CmsUser user, Byte step, String opinion) {
		Content[] beans = new Content[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = reject(ids[i], user,step,opinion);
		}
		return beans;
	}
	
	public Content submit(Integer id, CmsUser user){
		Content content = check(id, user);
		return content;
	}

	public Content[] submit(Integer[] ids, CmsUser user){
		Content[] beans = new Content[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = submit(ids[i], user);
		}
		return beans;
	}

	public Content cycle(CmsUser user,Integer id) {
		Content content = findById(id);
		// 执行监听器
		List<Map<String, Object>> mapList = preChange(content);
		content.setStatus(ContentCheck.RECYCLE);
		// 执行监听器
		afterChange(content, mapList);
		contentRecordMng.record(content, user, ContentOperateType.cycle);
		return content;
	}

	public Content[] cycle(CmsUser user,Integer[] ids) {
		Content[] beans = new Content[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = cycle(user,ids[i]);
		}
		return beans;
	}

	public Content recycle(Integer id) {
		Content content = findById(id);
		// 执行监听器
		List<Map<String, Object>> mapList = preChange(content);
		byte contentStep = content.getCheckStep();
		byte finalStep = content.getChannel().getFinalStepExtends();
		if (contentStep >= finalStep && !content.getRejected()) {
			content.setStatus(ContentCheck.CHECKED);
		} else {
			content.setStatus(ContentCheck.CHECKING);
		}
		// 执行监听器
		afterChange(content, mapList);
		return content;
	}

	public Content[] recycle(Integer[] ids) {
		Content[] beans = new Content[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = recycle(ids[i]);
		}
		return beans;
	}

	public Content deleteById(Integer id) {
		Content bean = findById(id);
		// 执行监听器
		preDelete(bean);
		// 移除tag
		contentTagMng.removeTags(bean.getTags());
		bean.getTags().clear();
		// 删除评论
		cmsCommentMng.deleteByContentId(id);
		//删除附件记录
		fileMng.deleteByContentId(id);
		bean.clear();
		bean = dao.deleteById(id);
		// 执行监听器
		afterDelete(bean);
		return bean;
	}

	public Content[] deleteByIds(Integer[] ids) {
		Content[] beans = new Content[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	public Content[] contentStatic(CmsUser user,Integer[] ids)
			throws TemplateNotFoundException, TemplateParseException,
			GeneratedZeroStaticPageException, StaticPageNotOpenException, ContentNotCheckedException {
		int count = 0;
		List<Content> list = new ArrayList<Content>();
		for (int i = 0, len = ids.length; i < len; i++) {
			Content content = findById(ids[i]);
			try {
				if (!content.getChannel().getStaticContent()) {
					throw new StaticPageNotOpenException(
							"content.staticNotOpen", count, content.getTitle());
				}
				if(!content.isChecked()){
					throw new ContentNotCheckedException("content.notChecked", count, content.getTitle());
				}
				if (staticPageSvc.content(content)) {
					list.add(content);
					count++;
				}
			} catch (IOException e) {
				throw new TemplateNotFoundException(
						"content.tplContentNotFound", count, content.getTitle());
			} catch (TemplateException e) {
				throw new TemplateParseException("content.tplContentException",
						count, content.getTitle());
			}
			contentRecordMng.record(content, user, ContentOperateType.createPage);
		}
		if (count == 0) {
			throw new GeneratedZeroStaticPageException(
					"content.staticGenerated");
		}
		Content[] beans = new Content[count];
		return list.toArray(beans);
	}
	
	public Pagination getPageForCollection(Integer siteId, Integer memberId, int pageNo, int pageSize){
		return dao.getPageForCollection(siteId,memberId,pageNo,pageSize);
	}
	
	public void updateFileByContent(Content bean,Boolean valid){
		Set<CmsFile>files;
		Iterator<CmsFile>it;
		CmsFile tempFile;
		//处理附件
		files=bean.getFiles();
		it=files.iterator();
		while(it.hasNext()){
			tempFile=it.next();
			tempFile.setFileIsvalid(valid);
			fileMng.update(tempFile);
		}
	}
	
	
	

	public String checkForChannelDelete(Integer channelId) {
		int count = dao.countByChannelId(channelId);
		if (count > 0) {
			return "content.error.cannotDeleteChannel";
		} else {
			return null;
		}
	}
	

	private void preSave(Content content) {
		if (listenerList != null) {
			for (ContentListener listener : listenerList) {
				listener.preSave(content);
			}
		}
	}

	private void afterSave(Content content) {
		if (listenerList != null) {
			for (ContentListener listener : listenerList) {
				listener.afterSave(content);
			}
		}
	}

	private List<Map<String, Object>> preChange(Content content) {
		if (listenerList != null) {
			int len = listenerList.size();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(
					len);
			for (ContentListener listener : listenerList) {
				list.add(listener.preChange(content));
			}
			return list;
		} else {
			return null;
		}
	}

	private void afterChange(Content content, List<Map<String, Object>> mapList) {
		if (listenerList != null) {
			Assert.notNull(mapList);
			Assert.isTrue(mapList.size() == listenerList.size());
			int len = listenerList.size();
			ContentListener listener;
			for (int i = 0; i < len; i++) {
				listener = listenerList.get(i);
				listener.afterChange(content, mapList.get(i));
			}
		}
	}

	private void preDelete(Content content) {
		if (listenerList != null) {
			for (ContentListener listener : listenerList) {
				listener.preDelete(content);
			}
		}
	}

	private void afterDelete(Content content) {
		if (listenerList != null) {
			for (ContentListener listener : listenerList) {
				listener.afterDelete(content);
			}
		}
	}

	private List<ContentListener> listenerList;

	public void setListenerList(List<ContentListener> listenerList) {
		this.listenerList = listenerList;
	}

	private ChannelMng channelMng;
	private ContentExtMng contentExtMng;
	private ContentTxtMng contentTxtMng;
	private ContentTypeMng contentTypeMng;
	private ContentCountMng contentCountMng;
	private ContentCheckMng contentCheckMng;
	private ContentTagMng contentTagMng;
	private CmsGroupMng cmsGroupMng;
	private CmsUserMng cmsUserMng;
	private CmsTopicMng cmsTopicMng;
	private CmsCommentMng cmsCommentMng;
	private ContentDao dao;
	private StaticPageSvc staticPageSvc;
	private CmsFileMng fileMng;
	@Autowired
	private ContentRecordMng contentRecordMng;
	@Autowired
	private ChannelCountMng channelCountMng;
	@Autowired
	private ContentChargeMng contentChargeMng;

	@Autowired
	public void setChannelMng(ChannelMng channelMng) {
		this.channelMng = channelMng;
	}

	@Autowired
	public void setContentTypeMng(ContentTypeMng contentTypeMng) {
		this.contentTypeMng = contentTypeMng;
	}

	@Autowired
	public void setContentCountMng(ContentCountMng contentCountMng) {
		this.contentCountMng = contentCountMng;
	}

	@Autowired
	public void setContentExtMng(ContentExtMng contentExtMng) {
		this.contentExtMng = contentExtMng;
	}

	@Autowired
	public void setContentTxtMng(ContentTxtMng contentTxtMng) {
		this.contentTxtMng = contentTxtMng;
	}

	@Autowired
	public void setContentCheckMng(ContentCheckMng contentCheckMng) {
		this.contentCheckMng = contentCheckMng;
	}

	@Autowired
	public void setCmsTopicMng(CmsTopicMng cmsTopicMng) {
		this.cmsTopicMng = cmsTopicMng;
	}

	@Autowired
	public void setContentTagMng(ContentTagMng contentTagMng) {
		this.contentTagMng = contentTagMng;
	}

	@Autowired
	public void setCmsGroupMng(CmsGroupMng cmsGroupMng) {
		this.cmsGroupMng = cmsGroupMng;
	}

	@Autowired
	public void setCmsUserMng(CmsUserMng cmsUserMng) {
		this.cmsUserMng = cmsUserMng;
	}

	@Autowired
	public void setCmsCommentMng(CmsCommentMng cmsCommentMng) {
		this.cmsCommentMng = cmsCommentMng;
	}
	
	@Autowired
	public void setFileMng(CmsFileMng fileMng) {
		this.fileMng = fileMng;
	}

	@Autowired
	public void setDao(ContentDao dao) {
		this.dao = dao;
	}

	@Autowired
	public void setStaticPageSvc(StaticPageSvc staticPageSvc) {
		this.staticPageSvc = staticPageSvc;
	}
}