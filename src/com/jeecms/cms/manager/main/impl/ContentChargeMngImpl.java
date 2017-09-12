package com.jeecms.cms.manager.main.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.cms.dao.main.ContentChargeDao;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentCharge;
import com.jeecms.cms.manager.main.ContentChargeMng;
import com.jeecms.common.hibernate4.Updater;
import com.jeecms.common.page.Pagination;

@Service
@Transactional
public class ContentChargeMngImpl implements ContentChargeMng {
	
	@Transactional(readOnly=true)
	public List<ContentCharge> getList(String contentTitle,Integer authorUserId,
			Date buyTimeBegin,Date buyTimeEnd,int orderBy,int count){
		return dao.getList(contentTitle,authorUserId,
				buyTimeBegin,buyTimeEnd, orderBy, count);
	}
	
	@Transactional(readOnly=true)
	public Pagination getPage(String contentTitle,Integer authorUserId,
			Date buyTimeBegin,Date buyTimeEnd,
			int orderBy,int pageNo,int pageSize){
		return dao.getPage(contentTitle,authorUserId,
				buyTimeBegin,buyTimeEnd,orderBy,pageNo,pageSize);
	}
	
	public ContentCharge save(Double chargeAmount, Short charge,Content content){
		ContentCharge contentCharge=new ContentCharge();
		contentCharge.setChargeAmount(chargeAmount);
		contentCharge.setChargeReward(charge);
		contentCharge=save(contentCharge, content);
		return contentCharge;
	}
	
	public void afterContentUpdate(Content bean,Short charge,Double chargeAmount) {
		if(charge!=null&&!charge.equals(ContentCharge.MODEL_FREE)){
			ContentCharge c=bean.getContentCharge();
			//收费金额变更
			if(c!=null){
				c.setChargeAmount(chargeAmount);
				c.setChargeReward(charge);
				update(c);
			}else{
				//从免费变更收费
				save(chargeAmount, charge,bean);
			}
		}else{
			ContentCharge c=bean.getContentCharge();
			//从收费变免费
			if(c!=null){
				c.setChargeAmount(0d);
				c.setChargeReward(ContentCharge.MODEL_FREE);
				update(c);
			}
		}
	}

	public ContentCharge update(ContentCharge bean) {
		Updater<ContentCharge> updater = new Updater<ContentCharge>(bean);
		bean = dao.updateByUpdater(updater);
		return bean;
	}
	
	public ContentCharge afterUserPay(Double payAmout, Content content){
		Calendar curr = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		ContentCharge charge=content.getContentCharge();
		if(content.getLastBuyTime()!=null){
			last.setTime(content.getLastBuyTime());
			int currDay = curr.get(Calendar.DAY_OF_YEAR);
			int lastDay = last.get(Calendar.DAY_OF_YEAR);
			int currYear=curr.get(Calendar.YEAR);
			int lastYear=last.get(Calendar.YEAR);
			int currMonth = curr.get(Calendar.MONTH);
			int lastMonth = last.get(Calendar.MONTH);
			if(charge!=null){
				if(lastYear!=currYear){
					charge.setYearAmount(0d);
					charge.setMonthAmount(0d);
					charge.setDayAmount(0d);
				}else{
					if(currMonth!=lastMonth){
						charge.setMonthAmount(0d);
						charge.setDayAmount(0d);
					}else{
						if (currDay != lastDay) {
							charge.setDayAmount(0d);
						}
					}
				}
			}
		}
		charge.setTotalAmount(charge.getTotalAmount()+payAmout);
		charge.setYearAmount(charge.getYearAmount()+payAmout);
		charge.setMonthAmount(charge.getMonthAmount()+payAmout);
		charge.setDayAmount(charge.getDayAmount()+payAmout);
		charge.setLastBuyTime(curr.getTime());
		return charge;
	}
	

	private ContentCharge save(ContentCharge charge, Content content) {
		content.setContentCharge(charge);
		charge.setContent(content);
		charge.init();
		dao.save(charge);
		content.setContentCharge(charge);
		return charge;
	}

	private ContentChargeDao dao;

	@Autowired
	public void setDao(ContentChargeDao dao) {
		this.dao = dao;
	}
}