package com.jeecms.cms.manager.main;

import com.jeecms.common.page.Pagination;


import com.jeecms.cms.entity.main.ContentBuy;

public interface ContentBuyMng {
	public Pagination getPage(String orderNum,Integer buyUserId,Integer authorUserId
			,Short payMode,int pageNo, int pageSize);
	
	public Pagination getPageByContent(Integer contentId,
			Short payMode,int pageNo, int pageSize);

	public ContentBuy findById(Long id);
	
	public ContentBuy findByOrderNumber(String orderNumber);
	
	public boolean hasBuyContent(Integer buyUserId,Integer contentId);

	public ContentBuy save(ContentBuy bean);

	public ContentBuy update(ContentBuy bean);

	public ContentBuy deleteById(Long id);
	
	public ContentBuy[] deleteByIds(Long[] ids);
}