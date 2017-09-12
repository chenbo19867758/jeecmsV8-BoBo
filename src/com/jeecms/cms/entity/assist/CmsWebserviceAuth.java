package com.jeecms.cms.entity.assist;

import com.jeecms.cms.entity.assist.base.BaseCmsWebserviceAuth;



public class CmsWebserviceAuth extends BaseCmsWebserviceAuth {
	private static final long serialVersionUID = 1L;


	public boolean getEnable() {
		return super.isEnable();
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsWebserviceAuth () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsWebserviceAuth (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsWebserviceAuth (
		java.lang.Integer id,
		java.lang.String username,
		java.lang.String password,
		java.lang.String system,
		boolean enable) {

		super (
			id,
			username,
			password,
			system,
			enable);
	}

/*[CONSTRUCTOR MARKER END]*/


}