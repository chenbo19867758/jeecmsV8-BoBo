package com.jeecms.cms.manager.assist.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.cms.dao.assist.CmsVoteItemDao;
import com.jeecms.cms.entity.assist.CmsVoteItem;
import com.jeecms.cms.entity.assist.CmsVoteSubTopic;
import com.jeecms.cms.manager.assist.CmsVoteItemMng;
import com.jeecms.common.hibernate4.Updater;
import com.jeecms.common.page.Pagination;

@Service
@Transactional
public class CmsVoteItemMngImpl implements CmsVoteItemMng {
	@Transactional(readOnly = true)
	public Pagination getPage(int pageNo, int pageSize) {
		Pagination page = dao.getPage(pageNo, pageSize);
		return page;
	}

	@Transactional(readOnly = true)
	public CmsVoteItem findById(Integer id) {
		CmsVoteItem entity = dao.findById(id);
		return entity;
	}

	public Collection<CmsVoteItem> save(Collection<CmsVoteItem> items,
			CmsVoteSubTopic topic) {
		for (CmsVoteItem item : items) {
			item.setSubTopic(topic);
			item.setTopic(topic.getVoteTopic());
			item.init();
			dao.save(item);
		}
		return items;
	}

	public Collection<CmsVoteItem> update(Collection<CmsVoteItem> items,
			CmsVoteSubTopic topic) {
		Set<CmsVoteItem> set = topic.getVoteItems();
		if(set==null){
			set=new HashSet<CmsVoteItem>();
		}
		// 先删除
		Set<CmsVoteItem> toDel = new HashSet<CmsVoteItem>();
		for (CmsVoteItem item : set) {
			if (!items.contains(item)) {
				toDel.add(item);
			}
		}
		set.removeAll(toDel);
		// 再更新和增加
		Updater<CmsVoteItem> updater;
		Set<CmsVoteItem> toAdd = new HashSet<CmsVoteItem>();
		for (CmsVoteItem item : items) {
			if (set.contains(item)) {
				// 更新
				updater = new Updater<CmsVoteItem>(item);
				dao.updateByUpdater(updater);
			} else {
				// 增加
				toAdd.add(item);
			}
		}
		save(toAdd, topic);
		set.addAll(toAdd);
		return set;
	}

	public CmsVoteItem deleteById(Integer id) {
		CmsVoteItem bean = dao.deleteById(id);
		return bean;
	}

	public CmsVoteItem[] deleteByIds(Integer[] ids) {
		CmsVoteItem[] beans = new CmsVoteItem[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	private CmsVoteItemDao dao;

	@Autowired
	public void setDao(CmsVoteItemDao dao) {
		this.dao = dao;
	}
}