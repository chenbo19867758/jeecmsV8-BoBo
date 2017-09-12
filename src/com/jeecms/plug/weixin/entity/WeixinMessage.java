package com.jeecms.plug.weixin.entity;

import com.jeecms.plug.weixin.entity.base.BaseWeixinMessage;



public class WeixinMessage extends BaseWeixinMessage {
	private static final long serialVersionUID = 1L;
	public static final int CONTENT_ONLY=2;
	public static final int CONTENT_WITH_KEY=1;
	public static final int CONTENT_WITH_IMG=0;
	public java.lang.Boolean getWelcome (){
		return super.isWelcome();
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public WeixinMessage () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public WeixinMessage (java.lang.Integer id) {
		super(id);
	}

/*[CONSTRUCTOR MARKER END]*/


}