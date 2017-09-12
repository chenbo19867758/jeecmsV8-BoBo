package com.jeecms.cms.manager.assist.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.cms.dao.assist.CmsAcquisitionDao;
import com.jeecms.cms.entity.assist.CmsAcquisition;
import com.jeecms.cms.entity.assist.CmsAcquisitionHistory;
import com.jeecms.cms.entity.assist.CmsAcquisitionTemp;
import com.jeecms.cms.entity.assist.CmsAcquisition.AcquisitionResultType;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentCharge;
import com.jeecms.cms.entity.main.ContentExt;
import com.jeecms.cms.entity.main.ContentTxt;
import com.jeecms.cms.manager.assist.CmsAcquisitionHistoryMng;
import com.jeecms.cms.manager.assist.CmsAcquisitionMng;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.CmsModelMng;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.cms.manager.main.ContentTypeMng;
import com.jeecms.cms.service.ChannelDeleteChecker;
import com.jeecms.common.hibernate4.Updater;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.manager.CmsUserMng;

@Service
@Transactional
public class CmsAcquisitionMngImpl implements CmsAcquisitionMng,
		ChannelDeleteChecker {
	@Transactional(readOnly = true)
	public List<CmsAcquisition> getList(Integer siteId) {
		return dao.getList(siteId);
	}

	@Transactional
	public CmsAcquisition findById(Integer id) {
		CmsAcquisition entity = dao.findById(id);
		return entity;
	}

	public void stop(Integer id) {
		CmsAcquisition acqu = findById(id);
		if (acqu == null) {
			return;
		}
		if (acqu.getStatus() == CmsAcquisition.START) {
			acqu.setStatus(CmsAcquisition.STOP);
		} else if (acqu.getStatus() == CmsAcquisition.PAUSE) {
			acqu.setCurrNum(0);
			acqu.setCurrItem(0);
			acqu.setTotalItem(0);
		}
	}

	public void pause(Integer id) {
		CmsAcquisition acqu = findById(id);
		if (acqu == null) {
			return;
		}
		if (acqu.getStatus() == CmsAcquisition.START) {
			acqu.setStatus(CmsAcquisition.PAUSE);
		}
	}

	public CmsAcquisition start(Integer id) {
		CmsAcquisition acqu = findById(id);
		if (acqu == null) {
			return acqu;
		}
		acqu.setStatus(CmsAcquisition.START);
		acqu.setStartTime(new Date());
		acqu.setEndTime(null);
		if (acqu.getCurrNum() <= 0) {
			acqu.setCurrNum(1);
		}
		if (acqu.getCurrItem() <= 0) {
			acqu.setCurrItem(1);
		}
		acqu.setTotalItem(0);
		return acqu;
	}

	public void end(Integer id) {
		CmsAcquisition acqu = findById(id);
		if (acqu == null) {
			return;
		}
		acqu.setStatus(CmsAcquisition.STOP);
		acqu.setEndTime(new Date());
		acqu.setCurrNum(0);
		acqu.setCurrItem(0);
		acqu.setTotalItem(0);
		acqu.setTotalItem(0);
	}

	public boolean isNeedBreak(Integer id, int currNum, int currItem,
			int totalItem) {
		CmsAcquisition acqu = findById(id);
		if (acqu == null) {
			return true;
		} else if (acqu.isPuase()) {
			acqu.setCurrNum(currNum);
			acqu.setCurrItem(currItem);
			acqu.setTotalItem(totalItem);
			acqu.setEndTime(new Date());
			return true;
		} else if (acqu.isStop()) {
			acqu.setCurrNum(0);
			acqu.setCurrItem(0);
			acqu.setTotalItem(0);
			acqu.setEndTime(new Date());
			return true;
		} else {
			acqu.setCurrNum(currNum);
			acqu.setCurrItem(currItem);
			acqu.setTotalItem(totalItem);
			return false;
		}
	}

	public CmsAcquisition save(CmsAcquisition bean, Integer channelId,
			Integer typeId, Integer userId, Integer siteId) {
		bean.setChannel(channelMng.findById(channelId));
		bean.setType(contentTypeMng.findById(typeId));
		bean.setUser(cmsUserMng.findById(userId));
		bean.setSite(cmsSiteMng.findById(siteId));
		bean.init();
		dao.save(bean);
		return bean;
	}

	public CmsAcquisition update(CmsAcquisition bean, Integer channelId,
			Integer typeId) {
		Updater<CmsAcquisition> updater = new Updater<CmsAcquisition>(bean);
		bean = dao.updateByUpdater(updater);
		bean.setChannel(channelMng.findById(channelId));
		bean.setType(contentTypeMng.findById(typeId));
		return bean;
	}

	public CmsAcquisition deleteById(Integer id) {
		//删除采集记录
		acquisitionHistoryMng.deleteByAcquisition(id);
		CmsAcquisition bean = dao.deleteById(id);
		return bean;
	}

	public CmsAcquisition[] deleteByIds(Integer[] ids) {
		CmsAcquisition[] beans = new CmsAcquisition[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	public Content saveContent(String title, String txt, String origin,
			String author,String description,Date releaseDate,Integer acquId,
			AcquisitionResultType resultType, CmsAcquisitionTemp temp,
			CmsAcquisitionHistory history) {
		CmsAcquisition acqu = findById(acquId);
		Content c = new Content();
		c.setSite(acqu.getSite());
		c.setModel(modelMng.getDefModel());
		c.setSortDate(releaseDate);
		ContentExt cext = new ContentExt();
		ContentTxt ctxt = new ContentTxt();
		cext.setAuthor(author);
		cext.setOrigin(origin);
		cext.setReleaseDate(releaseDate);
		cext.setTitle(title);
		cext.setDescription(description);
		ctxt.setTxt(txt);
		Content content = contentMng.save(c, cext, ctxt,null, null, null,
				null, null, null, null, null, null, acqu.getChannel().getId(),
				acqu.getType().getId(), false,false,
				ContentCharge.MODEL_FREE,0d,acqu.getUser(), false);
		history.setTitle(title);
		history.setContent(content);
		history.setDescription(resultType.name());
		temp.setTitle(title);
		temp.setDescription(resultType.name());
		return content;
	}

	public String checkForChannelDelete(Integer channelId) {
		if (dao.countByChannelId(channelId) > 0) {
			return "cmsAcquisition.error.cannotDeleteChannel";
		} else {
			return null;
		}
	}

	public CmsAcquisition getStarted(Integer siteId) {
		return dao.getStarted(siteId);
	}

	public Integer hasStarted(Integer siteId) {
		return getStarted(siteId) == null ? 0 : getMaxQueue(siteId) + 1;
	}

	public Integer getMaxQueue(Integer siteId) {
		return dao.getMaxQueue(siteId);
	}

	public void addToQueue(Integer[] ids, Integer queueNum) {
		for (Integer id : ids) {
			CmsAcquisition acqu = findById(id);
			if (acqu.getStatus() == CmsAcquisition.START || acqu.getQueue() > 0) {
				continue;
			}
			acqu.setQueue(queueNum++);
		}
	}

	public void cancel(Integer siteId, Integer id) {
		CmsAcquisition acqu = findById(id);
		Integer queue = acqu.getQueue();
		for (CmsAcquisition c : getLargerQueues(siteId, queue)) {
			c.setQueue(c.getQueue() - 1);
		}
		acqu.setQueue(0);
	}

	public List<CmsAcquisition> getLargerQueues(Integer siteId, Integer queueNum) {
		return dao.getLargerQueues(siteId, queueNum);
	}

	public CmsAcquisition popAcquFromQueue(Integer siteId) {
		CmsAcquisition acquisition = dao.popAcquFromQueue(siteId);
		if (acquisition != null) {
			Integer id = acquisition.getId();
			cancel(siteId, id);
		}
		return acquisition;
	}

	private ChannelMng channelMng;
	private ContentMng contentMng;
	private ContentTypeMng contentTypeMng;
	private CmsSiteMng cmsSiteMng;
	private CmsUserMng cmsUserMng;
	private CmsAcquisitionDao dao;
	@Autowired
	private CmsModelMng modelMng;
	@Autowired
	private CmsAcquisitionHistoryMng acquisitionHistoryMng;

	@Autowired
	public void setChannelMng(ChannelMng channelMng) {
		this.channelMng = channelMng;
	}

	@Autowired
	public void setContentMng(ContentMng contentMng) {
		this.contentMng = contentMng;
	}

	@Autowired
	public void setContentTypeMng(ContentTypeMng contentTypeMng) {
		this.contentTypeMng = contentTypeMng;
	}

	@Autowired
	public void setCmsSiteMng(CmsSiteMng cmsSiteMng) {
		this.cmsSiteMng = cmsSiteMng;
	}

	@Autowired
	public void setCmsUserMng(CmsUserMng cmsUserMng) {
		this.cmsUserMng = cmsUserMng;
	}

	@Autowired
	public void setDao(CmsAcquisitionDao dao) {
		this.dao = dao;
	}

}