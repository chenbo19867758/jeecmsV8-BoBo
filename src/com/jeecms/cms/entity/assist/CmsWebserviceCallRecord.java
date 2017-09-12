package com.jeecms.cms.entity.assist;

import com.jeecms.cms.entity.assist.base.BaseCmsWebserviceCallRecord;



public class CmsWebserviceCallRecord extends BaseCmsWebserviceCallRecord {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsWebserviceCallRecord () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsWebserviceCallRecord (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsWebserviceCallRecord (
		java.lang.Integer id,
		com.jeecms.cms.entity.assist.CmsWebserviceAuth auth,
		java.lang.String serviceCode,
		java.util.Date recordTime) {

		super (
			id,
			auth,
			serviceCode,
			recordTime);
	}

/*[CONSTRUCTOR MARKER END]*/


}