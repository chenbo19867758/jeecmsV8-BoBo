package com.jeecms.cms.manager.main.impl;

import java.util.Calendar;

import net.sf.ehcache.Ehcache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.cms.dao.main.ChannelCountDao;
import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.ChannelCount;
import com.jeecms.cms.manager.main.ChannelCountMng;
import com.jeecms.common.hibernate4.Updater;
import com.jeecms.core.entity.CmsConfig;
import com.jeecms.core.manager.CmsConfigMng;

@Service
@Transactional
public class ChannelCountMngImpl implements ChannelCountMng {
	public int freshCacheToDB(Ehcache cache) {
		CmsConfig config = cmsConfigMng.get();
		clearCount(config);
		int count = dao.freshCacheToDB(cache);
		return count;
	}

	private int clearCount(CmsConfig config) {
		Calendar curr = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		last.setTime(config.getCountClearTime());
		int currDay = curr.get(Calendar.DAY_OF_YEAR);
		int lastDay = last.get(Calendar.DAY_OF_YEAR);
		if (currDay != lastDay) {
			int currWeek = curr.get(Calendar.WEEK_OF_YEAR);
			int lastWeek = last.get(Calendar.WEEK_OF_YEAR);
			int currMonth = curr.get(Calendar.MONTH);
			int lastMonth = last.get(Calendar.MONTH);
			//清除时间不更新也可以，在内容计数器中已经更新过
			cmsConfigMng.updateCountClearTime(curr.getTime());
			return dao.clearCount(currWeek != lastWeek, currMonth != lastMonth);
		} else {
			return 0;
		}
	}

	@Transactional(readOnly = true)
	public ChannelCount findById(Integer id) {
		ChannelCount entity = dao.findById(id);
		return entity;
	}

	public ChannelCount save(ChannelCount count, Channel channel) {
		count.setChannel(channel);
		count.init();
		dao.save(count);
		channel.setChannelCount(count);
		return count;
	}

	public void afterSaveContent(Channel channel) {
		ChannelCount c=channel.getChannelCount();
		CmsConfig config = cmsConfigMng.get();
		clearContentCount(config);
		c.setContentDay(c.getContentDay()+1);
		c.setContentMonth(c.getContentMonth()+1);
		c.setContentWeek(c.getContentWeek()+1);
		c.setContentYear(c.getContentYear()+1);
		c.setContentTotal(c.getContentTotal()+1);
		update(c);
		if(channel.getParent()!=null){
			afterSaveContent(channel.getParent());
		}
	}
	
	public void afterDelContent(Channel channel) {
		ChannelCount c=channel.getChannelCount();
		CmsConfig config = cmsConfigMng.get();
		clearContentCount(config);
		c.setContentDay(c.getContentDay()-1);
		c.setContentMonth(c.getContentMonth()-1);
		c.setContentWeek(c.getContentWeek()-1);
		c.setContentYear(c.getContentYear()-1);
		c.setContentTotal(c.getContentTotal()-1);
		if(c.getContentDay()<0){
			c.setContentDay(0);
		}
		if(c.getContentWeek()<0){
			c.setContentWeek(0);
		}
		if(c.getContentMonth()<0){
			c.setContentMonth(0);
		}
		if(c.getContentYear()<0){
			c.setContentYear(0);
		}
		if(c.getContentTotal()<0){
			c.setContentTotal(0);
		}
		update(c);
		if(channel.getParent()!=null){
			afterDelContent(channel.getParent());
		}
	}
	
	private int clearContentCount(CmsConfig config) {
		Calendar curr = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		last.setTime(config.getChannelCountClearTime());
		int currDay = curr.get(Calendar.DAY_OF_YEAR);
		int lastDay = last.get(Calendar.DAY_OF_YEAR);
		if (currDay != lastDay) {
			int currWeek = curr.get(Calendar.WEEK_OF_YEAR);
			int lastWeek = last.get(Calendar.WEEK_OF_YEAR);
			int currMonth = curr.get(Calendar.MONTH);
			int lastMonth = last.get(Calendar.MONTH);
			int currYear = curr.get(Calendar.YEAR);
			int lastYear = last.get(Calendar.YEAR);
			cmsConfigMng.updateChannelCountClearTime(curr.getTime());
			return dao.clearContentCount(currDay!=lastDay,currWeek != lastWeek,currMonth != lastMonth,currYear != lastYear);
		} else {
			return 0;
		}
	}
	
	public ChannelCount update(ChannelCount bean) {
		Updater<ChannelCount> updater = new Updater<ChannelCount>(bean);
		ChannelCount entity = dao.updateByUpdater(updater);
		return entity;
	}

	private CmsConfigMng cmsConfigMng;
	private ChannelCountDao dao;

	@Autowired
	public void setCmsConfigMng(CmsConfigMng cmsConfigMng) {
		this.cmsConfigMng = cmsConfigMng;
	}

	@Autowired
	public void setDao(ChannelCountDao dao) {
		this.dao = dao;
	}

}