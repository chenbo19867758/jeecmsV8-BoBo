package com.jeecms.core.entity;

import com.jeecms.core.entity.base.BaseCmsUserAccount;



public class CmsUserAccount extends BaseCmsUserAccount {
	private static final long serialVersionUID = 1L;
	
	public static final byte DRAW_WEIXIN=0;
	
	public static final byte DRAW_ALIPY=1;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsUserAccount () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsUserAccount (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsUserAccount (
		java.lang.Integer id,
		java.lang.Double contentYearAmount,
		java.lang.Double contentMonthAmount,
		java.lang.Double contentDayAmount,
		java.util.Date lastPaidTime) {

		super (
			id,
			contentYearAmount,
			contentMonthAmount,
			contentDayAmount,
			lastPaidTime);
	}

/*[CONSTRUCTOR MARKER END]*/


}