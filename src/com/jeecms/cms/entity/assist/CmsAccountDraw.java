package com.jeecms.cms.entity.assist;

import com.jeecms.cms.entity.assist.base.BaseCmsAccountDraw;



public class CmsAccountDraw extends BaseCmsAccountDraw {
	private static final long serialVersionUID = 1L;
	
	public static final Short CHECKING = 0;
	public static final Short CHECKED_SUCC = 1;
	public static final Short CHECKED_FAIL = 2;
	public static final Short DRAW_SUCC = 3;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsAccountDraw () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsAccountDraw (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsAccountDraw (
		java.lang.Integer id,
		com.jeecms.core.entity.CmsUser drawUser,
		java.lang.String applyAccount,
		java.lang.Double applyAmount,
		java.lang.Short applyStatus,
		java.util.Date applyTime) {

		super (
			id,
			drawUser,
			applyAccount,
			applyAmount,
			applyStatus,
			applyTime);
	}

/*[CONSTRUCTOR MARKER END]*/


}