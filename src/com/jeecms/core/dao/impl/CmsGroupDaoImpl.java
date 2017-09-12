package com.jeecms.core.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.jeecms.common.hibernate4.Finder;
import com.jeecms.common.hibernate4.HibernateBaseDao;
import com.jeecms.core.dao.CmsGroupDao;
import com.jeecms.core.entity.CmsGroup;

@Repository
public class CmsGroupDaoImpl extends HibernateBaseDao<CmsGroup, Integer>
		implements CmsGroupDao {
	@SuppressWarnings("unchecked")
	public List<CmsGroup> getList() {
		String hql = "from CmsGroup bean order by bean.priority asc,bean.id asc";
		return find(hql);
	}

	public CmsGroup getRegDef() {
		String hql = "from CmsGroup bean where bean.regDef=true";
		return (CmsGroup) findUnique(hql);
	}

	public CmsGroup findById(Integer id) {
		CmsGroup entity = get(id);
		return entity;
	}
	
	public CmsGroup findByName(String name){
		String hql = "from CmsGroup bean where bean.name=:name";
		Finder f=Finder.create(hql).setParam("name", name);
		List<CmsGroup>list=find(f);
		if(list.size()>0){
			return list.get(0);
		}else{
			return null;
		}
	}
	

	public CmsGroup save(CmsGroup bean) {
		getSession().save(bean);
		return bean;
	}

	public CmsGroup deleteById(Integer id) {
		CmsGroup entity = super.get(id);
		if (entity != null) {
			getSession().delete(entity);
		}
		return entity;
	}

	@Override
	protected Class<CmsGroup> getEntityClass() {
		return CmsGroup.class;
	}
}