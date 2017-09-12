package com.jeecms.cms.staticpage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.service.ContentListenerAbstract;

import freemarker.template.TemplateException;

@Component
public class ContentStaticPageListener extends ContentListenerAbstract {
	private static final Logger log = LoggerFactory
			.getLogger(ContentStaticPageListener.class);
	/**
	 * 是否已审核
	 */
	private static final String IS_CHECKED = "isChecked";

	@Override
	public void afterSave(Content content) {
		if (content.isChecked()) {
			try {
				staticPageSvc.contentRelated(content.getId());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TemplateException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Map<String, Object> preChange(Content content) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(IS_CHECKED, content.isChecked());
		return map;
	}

	@Override
	public void afterChange(Content content, Map<String, Object> map) {
		boolean pre = (Boolean) map.get(IS_CHECKED);
		boolean curr = content.isChecked();
		try {
			if (pre && !curr) {
				staticPageSvc.deleteContent(content);
			} else if (!pre && curr) {
				staticPageSvc.contentRelated(content);
			} else if (pre && curr) {
				staticPageSvc.contentRelated(content);
			}
		} catch (IOException e) {
			log.error("", e);
		} catch (TemplateException e) {
			log.error("", e);
		}
	}

	@Override
	public void afterDelete(Content content) {
		try {
			staticPageSvc.deleteContent(content);
		} catch (IOException e) {
			log.error("", e);
		} catch (TemplateException e) {
			log.error("", e);
		}
	}

	private StaticPageSvc staticPageSvc;

	@Autowired
	public void setStaticPageSvc(StaticPageSvc staticPageSvc) {
		this.staticPageSvc = staticPageSvc;
	}

}
