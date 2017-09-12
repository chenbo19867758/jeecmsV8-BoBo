package com.jeecms.cms.manager.main.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.common.hibernate4.Updater;
import com.jeecms.common.page.Pagination;
import com.jeecms.cms.dao.main.ContentBuyDao;
import com.jeecms.cms.entity.main.ContentBuy;
import com.jeecms.cms.entity.main.ContentCharge;
import com.jeecms.cms.manager.main.ContentBuyMng;
import com.jeecms.cms.manager.main.ContentMng;

@Service
@Transactional
public class ContentBuyMngImpl implements ContentBuyMng {
	@Transactional(readOnly = true)
	public Pagination getPage(String orderNum,Integer buyUserId,Integer authorUserId,
			Short payMode,int pageNo, int pageSize) {
		Pagination page = dao.getPage(orderNum,buyUserId,
				authorUserId,payMode,pageNo, pageSize);
		return page;
	}
	
	@Transactional(readOnly = true)
	public Pagination getPageByContent(Integer contentId,
			Short payMode,int pageNo, int pageSize){
		return dao.getPageByContent(contentId,payMode,pageNo,pageSize);
	}

	@Transactional(readOnly = true)
	public ContentBuy findById(Long id) {
		ContentBuy entity = dao.findById(id);
		return entity;
	}
	
	@Transactional(readOnly = true)
	public ContentBuy findByOrderNumber(String orderNumber){
		return dao.findByOrderNumber(orderNumber);
	}
	
	@Transactional(readOnly = true)
	public boolean hasBuyContent(Integer buyUserId,Integer contentId){
		ContentBuy buy=dao.find(buyUserId, contentId);
		//用户已经购买并且是收费订单非打赏订单
		if(buy!=null&&buy.getUserHasPaid()&&buy.getChargeReward()==ContentCharge.MODEL_CHARGE){
			return true;
		}else{
			return false;
		}
	}

	public ContentBuy save(ContentBuy bean) {
		dao.save(bean);
		return bean;
	}

	public ContentBuy update(ContentBuy bean) {
		Updater<ContentBuy> updater = new Updater<ContentBuy>(bean);
		bean = dao.updateByUpdater(updater);
		return bean;
	}

	public ContentBuy deleteById(Long id) {
		ContentBuy bean = dao.deleteById(id);
		return bean;
	}
	
	public ContentBuy[] deleteByIds(Long[] ids) {
		ContentBuy[] beans = new ContentBuy[ids.length];
		for (int i = 0,len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	private ContentBuyDao dao;
	@Autowired
	private ContentMng contentMng;

	@Autowired
	public void setDao(ContentBuyDao dao) {
		this.dao = dao;
	}
}