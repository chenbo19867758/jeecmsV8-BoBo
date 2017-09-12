package com.jeecms.cms.manager.assist;

import java.util.List;

import net.sf.ehcache.Ehcache;

import com.jeecms.common.page.Pagination;
import com.jeecms.cms.entity.assist.CmsSearchWords;

public interface CmsSearchWordsMng {
	public Pagination getPage(Integer siteId,String name,Integer recommend
			,Integer orderBy,	int pageNo, int pageSize);

	public List<CmsSearchWords> getList(Integer siteId,String name,
			Integer recommend,Integer orderBy,Integer count,boolean cacheable);
	
	public CmsSearchWords findById(Integer id);

	public CmsSearchWords save(CmsSearchWords bean);

	public CmsSearchWords update(CmsSearchWords bean);

	public CmsSearchWords deleteById(Integer id);
	
	public CmsSearchWords[] deleteByIds(Integer[] ids);
	
	public int freshCacheToDB(Ehcache cache);

}