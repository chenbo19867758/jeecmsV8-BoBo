package com.jeecms.cms.entity.assist;

import java.util.ArrayList;
import java.util.List;

import com.jeecms.cms.entity.assist.base.BaseCmsWebservice;



public class CmsWebservice extends BaseCmsWebservice {
	private static final long serialVersionUID = 1L;
	
	public static final String SERVICE_TYPE_ADD_USER = "addUser";
	public static final String SERVICE_TYPE_UPDATE_USER = "updateUser";
	public static final String SERVICE_TYPE_DELETE_USER = "deleteUser";
	
	public void addToParams(String name, String defaultValue) {
		List<CmsWebserviceParam> list = getParams();
		if (list == null) {
			list = new ArrayList<CmsWebserviceParam>();
			setParams(list);
		}
		CmsWebserviceParam param = new CmsWebserviceParam();
		param.setParamName(name);
		param.setDefaultValue(defaultValue);
		list.add(param);
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsWebservice () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsWebservice (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsWebservice (
		java.lang.Integer id,
		java.lang.String address) {

		super (
			id,
			address);
	}

/*[CONSTRUCTOR MARKER END]*/


}