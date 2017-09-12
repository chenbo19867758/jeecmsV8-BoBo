package com.jeecms.plug.weixin.action.admin;

import static com.jeecms.common.page.SimplePage.cpn;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.service.WeiXinSvc;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.plug.weixin.entity.WeixinMenu;
import com.jeecms.plug.weixin.manager.WeixinMenuMng;

@Controller
public class WeixinMenuAct {
	
	private static final Logger log = LoggerFactory.getLogger(WeixinMenuAct.class);
	
	@RequiresPermissions("weixinMenu:v_list")
	@RequestMapping("/weixinMenu/v_list.do")
	public String list(Integer parentId,Integer pageNo,HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		Pagination p = manager.getPage(site.getId(),parentId, cpn(pageNo), CookieUtils.getPageSize(request));
		if(parentId!=null){
			WeixinMenu entity = manager.findById(parentId);
			if(entity.getParent()!=null){
				model.addAttribute("parentListId", entity.getParent().getId());
			}
		}
		
		model.addAttribute("pagination", p);
		model.addAttribute("parentId", parentId);
		model.addAttribute("pageNo", pageNo);
		return "weixinMenu/list";
	}
	
	@RequiresPermissions("weixinMenu:v_add")
	@RequestMapping("/weixinMenu/v_add.do")
	public String add(Integer parentId,Integer pageNo,HttpServletRequest request, ModelMap model) {
		model.addAttribute("parentId", parentId);
		model.addAttribute("pageNo", pageNo);
		return "weixinMenu/add";
	}

	@RequiresPermissions("weixinMenu:v_edit")
	@RequestMapping("/weixinMenu/v_edit.do")
	public String edit(Integer id,Integer parentId,Integer pageNo,HttpServletRequest request, ModelMap model) {
		WeixinMenu entity = manager.findById(id);
		model.addAttribute("parentId", parentId);
		model.addAttribute("pageNo", pageNo);
		model.addAttribute("menu",entity);
		return "weixinMenu/edit";
	}
	
	@RequiresPermissions("weixinMenu:o_save")
	@RequestMapping("/weixinMenu/o_save.do")
	public String save(WeixinMenu bean,Integer parentId,Integer pageNo,HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		bean.setSite(site);
		if(parentId!=null){
			bean.setParent(manager.findById(parentId));
		}
		manager.save(bean);
		return list(parentId, pageNo, request, model);
	}
	
	@RequiresPermissions("weixinMenu:o_update")
	@RequestMapping("/weixinMenu/o_update.do")
	public String update(WeixinMenu bean,Integer parentId,Integer pageNo,HttpServletRequest request, ModelMap model) {
		manager.update(bean);
		return list(parentId, pageNo, request, model);
	}
	
	@RequiresPermissions("weixinMenu:o_menu")
	@RequestMapping("/weixinMenu/o_menu.do")
	public String menu(WeixinMenu bean,Integer parentId,
			Integer pageNo,HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		List<WeixinMenu> menus = manager.getList(site.getId(),100);
		weixinSvcMng.createMenu(getMenuJsonString(menus));
		return list(parentId, pageNo, request, model);
	}
	
	@RequiresPermissions("weixinMenu:o_delete")
	@RequestMapping("/weixinMenu/o_delete.do")
	public String delete(Integer[] ids,Integer parentId,Integer pageNo,HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		WeixinMenu[] beans = manager.deleteByIds(ids);
		for (WeixinMenu bean : beans) {
			log.info("delete Brief id={}", bean.getId());
		}
		return list(parentId, pageNo, request, model);
	}
	
	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Integer id : ids) {
			vldExist(id, errors);
		}
		return errors;
	}
	
	private boolean vldExist(Integer id, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		WeixinMenu entity = manager.findById(id);
		if (errors.ifNotExist(entity, WeixinMenu.class, id)) {
			return true;
		}
		return false;
	}
	
	public String getMenuJsonString(List<WeixinMenu> menus) {
		
		String strJson = "{" +
				"\"button\":[";
				
		for (int i = 0; i < menus.size(); i++) {
			strJson = strJson + "{	";
			WeixinMenu menu = menus.get(i);
			if(menu.getChild().size()>0){
				strJson = strJson +
						"\"name\":\""+menu.getName()+"\","+
				        "\"sub_button\":[";
						Set<WeixinMenu> sets = menu.getChild();
						Iterator<WeixinMenu> iter = sets.iterator();
						int no = 1;
						while(iter.hasNext()){
							if(no>5){
								break;
							}else{
								if(no==1){
									strJson = strJson + "{";
								}else{
									strJson = strJson + ",{";
								}
								WeixinMenu child = iter.next();
								if(child.getType().equals("click")){
									strJson = strJson + 
											"\"type\":\"click\","+
											"\"name\":\""+child.getName()+"\","+
											"\"key\":\""+child.getKey()+"\"}";
								}else{
									strJson = strJson + 
											"\"type\":\"view\","+
											"\"name\":\""+child.getName()+"\","+
											"\"url\":\""+child.getUrl()+"\"}";
								}
								no++;
							}
						}
				strJson = strJson+"]";
			}else if(menu.getType().equals("click")){
				strJson = strJson + 
						"\"type\":\"click\","+
						"\"name\":\""+menu.getName()+"\","+
						"\"key\":\""+menu.getKey()+"\"";
			}else{
				strJson = strJson + 
						"\"type\":\"view\","+
						"\"name\":\""+menu.getName()+"\","+
						"\"url\":\""+menu.getUrl()+"\"";
			}
			if(i==menus.size()-1){
				strJson = strJson + "}";
			}else{
				strJson = strJson + "},";
			}
		}
		strJson = strJson + "]}";
        return strJson;
		    
	}
	
	@Autowired
	private WeixinMenuMng manager;
	@Autowired
	private WeiXinSvc weixinSvcMng;
}
