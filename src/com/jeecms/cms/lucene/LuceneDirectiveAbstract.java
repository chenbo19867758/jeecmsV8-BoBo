package com.jeecms.cms.lucene;

import java.util.Date;
import java.util.Map;

import com.jeecms.common.web.freemarker.DirectiveUtils;

import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public abstract class LuceneDirectiveAbstract implements TemplateDirectiveModel {
	/**
	 * 输入参数，搜索关键字
	 */
	public static final String PARAM_QUERY = "q";
	/**
	 * 输入参数，站点ID
	 */
	public static final String PARAM_SITE_ID = "siteId";
	/**
	 * 输入参数，栏目ID
	 */
	public static final String PARAM_CHANNEL_ID = "channelId";
	/**
	 * 输入参数，开始日期
	 */
	public static final String PARAM_START_DATE = "startDate";
	/**
	 * 输入参数，结束日期
	 */
	public static final String PARAM_END_DATE = "endDate";
	/**
	 * 输入参数，职位类型
	 */
	public static final String PARAM_CATEGORY = "category";
	/**
	 * 输入参数，工作地点
	 */
	public static final String PARAM_WORKPLACE = "workplace";

	protected String getQuery(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getString(PARAM_QUERY, params);
	}

	protected Integer getSiteId(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getInt(PARAM_SITE_ID, params);
	}

	protected Integer getChannelId(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getInt(PARAM_CHANNEL_ID, params);
	}

	protected Date getStartDate(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getDate(PARAM_START_DATE, params);
	}

	protected Date getEndDate(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getDate(PARAM_END_DATE, params);
	}

	protected String getCategory(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getString(PARAM_CATEGORY, params);
	}

	protected String getWorkplace(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getString(PARAM_WORKPLACE, params);
	}

}
