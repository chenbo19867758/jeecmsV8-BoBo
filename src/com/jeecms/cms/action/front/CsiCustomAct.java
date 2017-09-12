package com.jeecms.cms.action.front;

import static com.jeecms.cms.Constants.TPLDIR_CSI_CUSTOM;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.common.web.RequestUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.util.FrontUtils;

/**
 * 自定义客户端包含模板
 */
@Controller
public class CsiCustomAct {
	private static final Logger log = LoggerFactory
			.getLogger(CsiCustomAct.class);

	/**
	 * 解析至自定义模板页
	 * 
	 * @param tpl
	 *            自定义模板名称
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/csi_custom.jspx")
	public String custom(String tpl, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		log.debug("visit csi custom template: {}", tpl);
		CmsSite site = CmsUtils.getSite(request);
		if(StringUtils.isNotBlank(tpl)){
			// 将request中所有参数保存至model中。
			model.putAll(RequestUtils.getQueryParams(request));
			FrontUtils.frontData(request, model, site);
			FrontUtils.frontPageData(request, model);
			return FrontUtils.getTplPath(site.getSolutionPath(), TPLDIR_CSI_CUSTOM,
					tpl);
		}else{
			return FrontUtils.pageNotFound(request, response, model);
		}
	}
}
