package com.jeecms.cms.entity.main;

import org.apache.commons.lang.StringUtils;

import com.jeecms.cms.entity.main.base.BaseContentBuy;



public class ContentBuy extends BaseContentBuy {
	private static final long serialVersionUID = 1L;
	
	public boolean getUserHasPaid(){
		if(StringUtils.isNotBlank(getOrderNumWeiXin())
				||StringUtils.isNotBlank(getOrderNumAliPay())){
			return true;
		}else{
			return false;
		}
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ContentBuy () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ContentBuy (java.lang.Long id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public ContentBuy (
		java.lang.Long id,
		com.jeecms.cms.entity.main.Content content,
		com.jeecms.core.entity.CmsUser buyUser,
		com.jeecms.core.entity.CmsUser authorUser,
		java.lang.Double chargeAmount,
		java.lang.Double authorAmount,
		java.lang.Double platAmount,
		java.util.Date buyTime,
		boolean hasPaidAuthor) {

		super (
			id,
			content,
			buyUser,
			authorUser,
			chargeAmount,
			authorAmount,
			platAmount,
			buyTime,
			hasPaidAuthor);
	}

/*[CONSTRUCTOR MARKER END]*/


}