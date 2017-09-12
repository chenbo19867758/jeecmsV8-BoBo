package com.jeecms.cms.entity.assist;

import com.jeecms.cms.entity.assist.base.BaseCmsConfigContentCharge;

public class CmsConfigContentCharge extends BaseCmsConfigContentCharge {
	private static final long serialVersionUID = 1L;

	/* [CONSTRUCTOR MARKER BEGIN] */
	public CmsConfigContentCharge() {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsConfigContentCharge(java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */

	public CmsConfigContentCharge(Integer id,String weixinAppId, String weixinAccount,
			String weixinPassword, String alipayAppid, String alipayAccount, String alipayKey, Double chargeRatio,
			Double minDrawAmount) {
		super(id, weixinAppId, weixinAccount, weixinPassword, alipayAppid, alipayAccount, alipayKey, chargeRatio,
				minDrawAmount);
	}

	/* [CONSTRUCTOR MARKER END] */

}