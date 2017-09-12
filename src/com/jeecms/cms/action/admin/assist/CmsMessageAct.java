package com.jeecms.cms.action.admin.assist;

import static com.jeecms.common.page.SimplePage.cpn;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsMessage;
import com.jeecms.cms.entity.assist.CmsReceiverMessage;
import com.jeecms.cms.manager.assist.CmsMessageMng;
import com.jeecms.cms.manager.assist.CmsReceiverMessageMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsGroup;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.manager.CmsGroupMng;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.manager.CmsUserMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsMessageAct {
	private static final Logger log = LoggerFactory
			.getLogger(CmsMessageAct.class);

	@RequiresPermissions("message:v_list")
	@RequestMapping("/message/v_list.do")
	public String list(Integer pageNo, String title, Date sendBeginTime,
			Date sendEndTime, Boolean status, Integer box,
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model) {
		CmsUser user = CmsUtils.getUser(request);
		Pagination pagination = null;
		String returnPage = "message/inbox";
		if (box == null) {
			box = 0;
		}
		if (box.equals(0)) {
			// 收件箱
			pagination = receiverMessageMng.getPage(null, null, user
					.getId(), title, sendBeginTime, sendEndTime, status, box,
					false, cpn(pageNo), CookieUtils.getPageSize(request));
			returnPage = "message/inbox";
		} else if (box.equals(1)) {
			// 发件箱
			pagination = messageMng.getPage(null, user.getId(), null,
					title, sendBeginTime, sendEndTime, status, box, false,
					cpn(pageNo), CookieUtils.getPageSize(request));
			returnPage = "message/sendbox";
		} else if (box.equals(2)) {
			// 草稿箱
			pagination = messageMng.getPage(null, user.getId(), null,
					title, sendBeginTime, sendEndTime, status, box, false,
					cpn(pageNo), CookieUtils.getPageSize(request));
			returnPage = "message/draftbox";
		} else if (box.equals(3)) {
			// 垃圾箱(可能从收件箱或者从发件箱转过来)
			pagination = receiverMessageMng.getPage(null, user.getId(),
					user.getId(), title, sendBeginTime, sendEndTime, status,
					box, false, cpn(pageNo), CookieUtils.getPageSize(request));
			returnPage = "message/trashbox";
		}
		model.addAttribute("msg", request.getAttribute("msg"));
		model.addAttribute("pagination", pagination);
		model.addAttribute("pageNo", pageNo);
		model.addAttribute("title", title);
		model.addAttribute("sendBeginTime", sendBeginTime);
		model.addAttribute("sendEndTime", sendEndTime);
		model.addAttribute("status", status);
		model.addAttribute("box", box);
		return returnPage;
	}

	@RequiresPermissions("message:v_add")
	@RequestMapping("/message/v_add.do")
	public String add(ModelMap model) {
		List<CmsGroup> groups = groupMng.getList();
		model.addAttribute("groupList", groups);
		return "message/add";
	}

	// 直接发送
	@RequiresPermissions("message:v_send")
	@RequestMapping("/message/v_send.do")
	public String send(CmsMessage message, String username, Integer groupId,
			Integer pageNo, String title, Date sendBeginTime, Date sendEndTime,
			Boolean status, Integer box, ModelMap model,
			HttpServletRequest request, HttpServletResponse response) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		Date now = new Date();
		CmsReceiverMessage receiverMessage = new CmsReceiverMessage();
		CmsUser msgReceiverUser = userMng.findByUsername(username);
		if (msgReceiverUser != null) {
			messageInfoSet(message, receiverMessage, user, msgReceiverUser,
					now, site, request);
		}
		// 按会员组推送站内信
		if (groupId != null && !groupId.equals(-1)) {
			List<CmsUser> users;
			CmsUser tempUser;
			CmsMessage tempMsg;
			CmsReceiverMessage tempReceiverMsg;
			if (groupId.equals(0)) {
				// 所有未禁用会员
				users = userMng.getList(null, null, null, null, false, false,
						null);
				if (users != null && users.size() > 0) {
					for (int i = 0; i < users.size(); i++) {
						tempUser = users.get(i);
						tempMsg = new CmsMessage();
						tempMsg.setMsgTitle(message.getMsgTitle());
						tempMsg.setMsgContent(message.getMsgContent());
						tempReceiverMsg = new CmsReceiverMessage();
						if (msgReceiverUser != null) {
							if (!tempUser.equals(msgReceiverUser)) {
								messageInfoSet(tempMsg, tempReceiverMsg, user,
										tempUser, now, site, request);
							}
						} else {
							messageInfoSet(tempMsg, tempReceiverMsg, user,
									tempUser, now, site, request);
						}
					}
				}
			} else {
				// 非禁用的会员
				users = userMng.getList(null, null, null, groupId, false,
						false, null);
				if (users != null && users.size() > 0) {
					for (int i = 0; i < users.size(); i++) {
						tempUser = users.get(i);
						tempMsg = new CmsMessage();
						tempMsg.setMsgTitle(message.getMsgTitle());
						tempMsg.setMsgContent(message.getMsgContent());
						tempReceiverMsg = new CmsReceiverMessage();
						if (msgReceiverUser != null) {
							if (!tempUser.equals(msgReceiverUser)) {
								messageInfoSet(tempMsg, tempReceiverMsg, user,
										tempUser, now, site, request);
							}
						} else {
							messageInfoSet(tempMsg, tempReceiverMsg, user,
									tempUser, now, site, request);
						}
					}
				}
			}
		}

		return list(pageNo, title, sendBeginTime, sendEndTime, status, 1,
				request, response, model);
	}

	private void messageInfoSet(CmsMessage message,
			CmsReceiverMessage receiverMessage, CmsUser sendUser,
			CmsUser receiverUser, Date sendTime, CmsSite site,
			HttpServletRequest request) {
		message.setMsgBox(1);
		message.setMsgSendUser(sendUser);
		message.setMsgReceiverUser(receiverUser);
		message.setMsgStatus(false);
		message.setSendTime(sendTime);
		message.setSite(site);
		messageMng.save(message);
		receiverMessage.setMsgBox(0);
		receiverMessage.setMsgContent(message.getMsgContent());
		receiverMessage.setMsgSendUser(sendUser);
		receiverMessage.setMsgReceiverUser(receiverUser);
		receiverMessage.setMsgStatus(false);
		receiverMessage.setMsgTitle(message.getMsgTitle());
		receiverMessage.setSendTime(sendTime);
		receiverMessage.setSite(site);
		receiverMessage.setMessage(message);
		// 接收端（有一定冗余）
		receiverMessageMng.save(receiverMessage);
		log.info("member CmsMessage send CmsMessage success. id={}", message
				.getId());
		cmsLogMng.operating(request, "cmsMessage.log.send", "id="
				+ message.getId() + ";title=" + message.getMsgTitle());
	}

	// 存草稿
	@RequiresPermissions("message:v_save")
	@RequestMapping("/message/v_save.do")
	public String save(CmsMessage message, String username, Integer pageNo,
			String title, Date sendBeginTime, Date sendEndTime, Boolean status,
			Integer box, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		message.setMsgBox(2);
		message.setMsgSendUser(user);
		CmsUser msgReceiverUser = userMng.findByUsername(username);
		message.setMsgReceiverUser(msgReceiverUser);
		message.setMsgStatus(false);
		// 作为草稿和发件箱的区别
		message.setSendTime(null);
		// message.setSendTime(new Date());
		message.setSite(site);
		messageMng.save(message);
		CmsReceiverMessage receiverMessage = new CmsReceiverMessage(message);
		receiverMessage.setMsgBox(2);
		receiverMessage.setMessage(message);
		// 接收端（有一定冗余）
		receiverMessageMng.save(receiverMessage);
		cmsLogMng.operating(request, "cmsMessage.log.save", "id="
				+ message.getId() + ";title=" + message.getMsgTitle());
		return list(pageNo, title, sendBeginTime, sendEndTime, status, 2,
				request, response, model);
	}

	// 发送
	@RequiresPermissions("message:v_tosend")
	@RequestMapping("/message/v_tosend.do")
	public String message_tosend(Integer id, Integer pageNo, String title,
			Date sendBeginTime, Date sendEndTime, Boolean status, Integer box,
			ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		CmsMessage message = messageMng.findById(id);
		message.setMsgBox(1);
		message.setSendTime(new Date());
		messageMng.update(message);
		Set<CmsReceiverMessage> receiverMessageSet = message.getReceiverMsgs();
		Iterator<CmsReceiverMessage> it = receiverMessageSet.iterator();
		CmsReceiverMessage receiverMessage;
		while (it.hasNext()) {
			receiverMessage = it.next();
			receiverMessage.setMsgBox(0);
			receiverMessage.setSendTime(new Date());
			receiverMessage.setMessage(message);
			// 接收端（有一定冗余）
			receiverMessageMng.update(receiverMessage);
		}
		log.info("member CmsMessage send CmsMessage success. id={}", message
				.getId());
		cmsLogMng.operating(request, "cmsMessage.log.send", "id="
				+ message.getId() + ";title=" + message.getMsgTitle());
		return list(pageNo, title, sendBeginTime, sendEndTime, status, 1,
				request, response, model);
	}

	@RequiresPermissions("message:v_edit")
	@RequestMapping("/message/v_edit.do")
	public String edit(Integer id, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsMessage message = messageMng.findById(id);
		model.addAttribute("message", message);
		return "message/edit";
	}
	
	@RequiresPermissions("message:o_update")
	@RequestMapping("/message/o_update.do")
	public String update(CmsMessage message, Integer pageNo, String title,
			Date sendBeginTime, Date sendEndTime, Boolean status, Integer box,
			ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		message = messageMng.update(message);
		// 更新发送表的信息，收件表的信息同步更新
		Set<CmsReceiverMessage> receiverMessageSet = message.getReceiverMsgs();
		Iterator<CmsReceiverMessage> it = receiverMessageSet.iterator();
		CmsReceiverMessage receiverMessage;
		while (it.hasNext()) {
			receiverMessage = it.next();
			receiverMessage.setMsgContent(message.getContentHtml());
			receiverMessage.setMsgReceiverUser(message.getMsgReceiverUser());
			receiverMessage.setMsgTitle(message.getMsgTitle());
			receiverMessage.setMessage(message);
			// 接收端（有一定冗余）
			receiverMessageMng.update(receiverMessage);
		}
		log.info("member CmsMessage update CmsMessage success. id={}", message
				.getId());
		cmsLogMng.operating(request, "cmsMessage.log.update", "id="
				+ message.getId() + ";title=" + message.getMsgTitle());
		return list(pageNo, title, sendBeginTime, sendEndTime, status, box,
				request, response, model);
	}

	@RequiresPermissions("message:v_read")
	@RequestMapping("/message/v_read.do")
	public String read(Integer id, Integer box, HttpServletRequest request,
			ModelMap model) {

		CmsUser user = CmsUtils.getUser(request);
		if(box!=null){
			//发件箱
			if(box.equals(1)){
				// 阅读已发信
				CmsMessage msg = messageMng.findById(id);
				model.addAttribute("message", msg);
			}else{
				CmsReceiverMessage message = receiverMessageMng.findById(id);
				// 阅读收信
				// 收件人查看更新已读状态
				if(message!=null){
					if (message.getMsgReceiverUser().equals(user)) {
						message.setMsgStatus(true);
						receiverMessageMng.update(message);
					}
					model.addAttribute("message", message);
				}
			}
		}
		model.addAttribute("box", box);
		return "message/read";
	}

	@RequiresPermissions("message:v_forward")
	@RequestMapping("/message/v_forward.do")
	public String forward(Integer id, HttpServletRequest request, ModelMap model) {
		CmsReceiverMessage receiverMessage = receiverMessageMng.findById(id);
		CmsMessage message;
		if (receiverMessage != null) {
			model.addAttribute("message", receiverMessage);
		} else {
			message = messageMng.findById(id);
			model.addAttribute("message", message);
		}
		List<CmsGroup> groups = groupMng.getList();
		model.addAttribute("groupList", groups);
		return "message/add";
	}
	
	@RequiresPermissions("message:v_reply")
	@RequestMapping("/message/v_reply.do")
	public String reply(Integer id, HttpServletRequest request, ModelMap model) {
		CmsReceiverMessage receiverMessage = receiverMessageMng.findById(id);
		model.addAttribute("message", receiverMessage);
		List<CmsGroup> groups = groupMng.getList();
		model.addAttribute("groupList", groups);
		return "message/reply";
	}

	@RequiresPermissions("message:v_trash")
	@RequestMapping("/message/v_trash.do")
	public void trash(Integer[] ids, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws JSONException {
		CmsUser user = CmsUtils.getUser(request);
		JSONObject object = new JSONObject();
		CmsMessage message;
		CmsReceiverMessage receiverMessage;
		if (user == null) {
			object.put("result", false);
		} else {
			for (Integer i = 0; i < ids.length; i++) {
				message = messageMng.findById(ids[i]);
				receiverMessage = receiverMessageMng.findById(ids[i]);
				if (message != null && message.getMsgSendUser().equals(user)) {
					// 清空发信表的同时复制该数据到收信表（收信人未空）
					receiverMessage=new CmsReceiverMessage();
					receiverMessage.setMsgBox(3);
					receiverMessage.setMsgContent(message.getMsgContent());
					receiverMessage.setMsgSendUser(message.getMsgSendUser());
					receiverMessage.setMsgReceiverUser(message.getMsgReceiverUser());
					receiverMessage.setMsgStatus(message.getMsgStatus());
					receiverMessage.setMsgTitle(message.getMsgTitle());
					receiverMessage.setSendTime(message.getSendTime());
					receiverMessage.setSite(message.getSite());
					receiverMessage.setMessage(message);
					// 接收端（有一定冗余）
					receiverMessageMng.save(receiverMessage);
					/*
					// 清空该发件对应的收件关联关系
					Set<CmsReceiverMessage> receiverMessages = message
							.getReceiverMsgs();
					CmsReceiverMessage tempReceiverMessage;
					if (receiverMessages != null && receiverMessages.size() > 0) {
						it = receiverMessages.iterator();
						while (it.hasNext()) {
							tempReceiverMessage= it.next();
							tempReceiverMessage.setMessage(null);
							receiverMessageMng.update(tempReceiverMessage);
						}
					}
					*/
					message.setMsgBox(3);
					messageMng.update(message);
					//messageMng.deleteById(ids[i]);
					cmsLogMng.operating(request, "cmsMessage.log.trash", "id="
							+ message.getId() + ";title="
							+ message.getMsgTitle());
				}
				if (receiverMessage != null
						&& receiverMessage.getMsgReceiverUser().equals(user)) {
					receiverMessage.setMsgBox(3);
					receiverMessageMng.update(receiverMessage);
					cmsLogMng.operating(request, "cmsMessage.log.trash", "id="
							+ receiverMessage.getId() + ";title="
							+ receiverMessage.getMsgTitle());
				}
				log.info("member CmsMessage trash CmsMessage success. id={}",
						ids[i]);
			}
			object.put("result", true);
		}
		ResponseUtils.renderJson(response, object.toString());
	}

	@RequiresPermissions("message:v_revert")
	@RequestMapping("/message/v_revert.do")
	public void revert(Integer ids[], HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws JSONException {
		CmsUser user = CmsUtils.getUser(request);
		JSONObject object = new JSONObject();
		CmsReceiverMessage receiverMessage;
		CmsMessage message;
		if (user == null) {
			object.put("result", false);
		} else {
			for (Integer i = 0; i < ids.length; i++) {
				receiverMessage = receiverMessageMng.findById(ids[i]);
				// 收件箱
				if (receiverMessage != null
						&& receiverMessage.getMsgReceiverUser().equals(user)) {
					receiverMessage.setMsgBox(0);
					receiverMessageMng.update(receiverMessage);
					cmsLogMng.operating(request, "cmsMessage.log.revert", "id="
							+ receiverMessage.getId() + ";title=" + receiverMessage.getMsgTitle());
				}
				//发件箱移除过来
				if (receiverMessage != null
						&& receiverMessage.getMsgSendUser().equals(user)){
					message=receiverMessage.getMessage();
					message.setMsgBox(1);
					messageMng.update(message);
					receiverMessageMng.deleteById(receiverMessage.getId());
				}
				log.info("member CmsMessage revert CmsMessage success. id={}",
						ids[i]);
			}
			object.put("result", true);
		}
		ResponseUtils.renderJson(response, object.toString());
	}

	@RequiresPermissions("message:v_empty")
	@RequestMapping("/message/v_empty.do")
	public void empty(Integer ids[], HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws JSONException {
		CmsUser user = CmsUtils.getUser(request);
		JSONObject object = new JSONObject();
		CmsReceiverMessage receiverMessage;
		if (user == null) {
			object.put("result", false);
		} else {
			for (Integer i = 0; i < ids.length; i++) {
				// 清空收到的站内信
				receiverMessage = receiverMessageMng.findById(ids[i]);
				if (receiverMessage != null) {
					receiverMessageMng.deleteById(ids[i]);
				}
				CmsMessage message=receiverMessage.getMessage();
				if(message!=null&&message.getReceiverMsgs().size()<=0){
					messageMng.deleteById(message.getId());
				}
			}
			object.put("result", true);
		}
		ResponseUtils.renderJson(response, object.toString());
	}

	@RequiresPermissions("message:v_findUser")
	@RequestMapping("/message/v_findUser.do")
	public void findUserByUserName(String username, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws JSONException {
		CmsUser user = CmsUtils.getUser(request);
		JSONObject object = new JSONObject();
		if (user == null) {
			object.put("result", false);
		} else {
			Boolean exist = userMng.usernameNotExist(username);
			object.put("result", true);
			object.put("exist", exist);
		}
		ResponseUtils.renderJson(response, object.toString());
	}

	// 查找未读信息条数
	@RequiresPermissions("message:v_countUnreadMsg")
	@RequestMapping(value = "/message/v_countUnreadMsg.do")
	public void findUnreadMessagesByUser(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws JSONException {
		CmsUser user = CmsUtils.getUser(request);
		CmsSite site = CmsUtils.getSite(request);
		JSONObject object = new JSONObject();
		if (user == null) {
			object.put("result", false);
		} else {
			List<CmsReceiverMessage> receiverMessages = receiverMessageMng
					.getList(site.getId(), null, user.getId(), null, null,
							null, false, 0, false);
			object.put("result", true);
			if (receiverMessages != null && receiverMessages.size() > 0) {
				object.put("count", receiverMessages.size());
			} else {
				object.put("count", 0);
			}
			object.put("result", true);
		}
		ResponseUtils.renderJson(response, object.toString());
	}

	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		return errors;
	}

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		CmsMessage entity = messageMng.findById(id);
		if (errors.ifNotExist(entity, CmsMessage.class, id)) {
			return true;
		}
		if (!entity.getSite().getId().equals(siteId)) {
			errors.notInSite(CmsMessage.class, id);
			return true;
		}
		return false;
	}

	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private CmsMessageMng messageMng;
	@Autowired
	private CmsReceiverMessageMng receiverMessageMng;
	@Autowired
	private CmsUserMng userMng;
	@Autowired
	private CmsGroupMng groupMng;
}