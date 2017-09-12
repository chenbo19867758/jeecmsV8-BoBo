package com.jeecms.cms.entity.main;

import com.jeecms.cms.entity.main.base.BaseContentRecord;



public class ContentRecord extends BaseContentRecord {
	private static final long serialVersionUID = 1L;
	public enum ContentOperateType {
		/**
		 * 新增
		 */
		add,
		/**
		 * 修改
		 */
		edit,
		/**
		 * 审核
		 */
		check,
		/**
		 * 退回
		 */
		rejected,
		/**
		 * 移动
		 */
		move,
		/**
		 * 生成静态页
		 */
		createPage,
		/**
		 * 回收
		 */
		cycle,
		/**
		 * 归档
		 */
		pigeonhole,
		/**
		 * 出档
		 */
		reuse,
		/**
		 * 共享
		 */
		shared
	};
	
	public static final byte add = 0;
	public static final byte edit = 1;
	public static final byte check = 2;
	public static final byte rejected = 3;
	public static final byte move = 4;
	public static final byte createPage = 5;
	public static final byte cycle = 6;
	public static final byte pigeonhole = 7;
	public static final byte reuse = 8;
	public static final byte shared = 9;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ContentRecord () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ContentRecord (java.lang.Long id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public ContentRecord (
		java.lang.Long id,
		com.jeecms.cms.entity.main.Content content,
		com.jeecms.core.entity.CmsUser user,
		java.util.Date operateTime,
		java.lang.Byte operateType) {

		super (
			id,
			content,
			user,
			operateTime,
			operateType);
	}

/*[CONSTRUCTOR MARKER END]*/


}