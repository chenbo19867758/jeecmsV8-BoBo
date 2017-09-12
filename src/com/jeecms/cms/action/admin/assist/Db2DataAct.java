package com.jeecms.cms.action.admin.assist;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jeecms.cms.entity.back.CmsField;
import com.jeecms.cms.manager.assist.CmsDb2DataBackMng;
import com.jeecms.cms.manager.assist.CmsResourceMng;

import com.jeecms.cms.Constants;
import com.jeecms.common.util.DateUtils;
import com.jeecms.common.util.Zipper;
import com.jeecms.common.util.Zipper.FileEntry;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.web.WebErrors;

@Controller
public class Db2DataAct {
	private static String SUFFIX = "sql";
	private static String BR = "\r\n";
	private static String SLASH="/";
	private static String BRANCH = ";";
	private static String DROP_TABLE=" DROP TABLE  ";
	private static String ALTER_TABLE=" ALTER TABLE ";
	private static String ALTER_COLUMN=" ALTER COLUMN ";
	private static String DROP_IDENTITY=" DROP  IDENTITY ";
	private static String ENFORCED="ENFORCED";
	private static String NOT_ENFORCED="NOT ENFORCED";
	private static String SET_IDENTITY_BEGIN=" SET GENERATED Always AS IDENTITY (START WITH ";
	private static String SET_IDENTITY_END=",INCREMENT BY 1,CACHE 10) ";
	private static String backup_table="";
	private static final String INVALID_PARAM = "template.invalidParams";
	private static final Logger log = LoggerFactory.getLogger(ResourceAct.class);
	
	@RequiresPermissions("data:v_list")
	@RequestMapping("/db2/data/v_list.do")
	public String list(ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		List<String> tables=new ArrayList<String>();
		tables = dataBackMng.listTables();
		model.addAttribute("tables", tables);
		return "data/list";
	}
	
	@RequiresPermissions("data:v_listfields")
	@RequestMapping("/db2/data/v_listfields.do")
	public String listfiled(String tablename, ModelMap model,
			HttpServletRequest request, HttpServletResponse response) {
		List<CmsField> list = dataBackMng.listFields(tablename);
		model.addAttribute("list", list);
		return "data/fields";
	}
	
	@RequiresPermissions("data:v_revert")
	@RequestMapping("/db2/data/v_revert.do")
	public String listDataBases(ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		model.addAttribute("backuppath", Constants.BACKUP_PATH);
		return "data/databases";
	}
	
	@RequiresPermissions("data:o_revert")
	@RequestMapping("/db2/data/o_revert.do")
	public String revert(String filename,String db,ModelMap model, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String backFilePath = realPathResolver.get(Constants.BACKUP_PATH + SLASH +filename);
		WebErrors errors = WebErrors.create(request);
		File file=new File(backFilePath);
		if(!file.exists()){
			errors.addErrorCode("error.db.hasnotselectfile");
			return errors.showErrorPage(model);
		}
		String sql=readFile(backFilePath);
		//若db发生变化，需要处理jdbc
		try {
			dataBackMng.executeSQL(sql);
		} catch (Exception e) {
			errors.addErrorCode("db.revert.error");
			errors.addErrorString(e.getMessage());
			if (errors.hasErrors()) {
				return errors.showErrorPage(model);
			}
		}
		model.addAttribute("msg","success");
		return listDataBases(model, request, response);
	}
	
	@RequiresPermissions("data:o_backup")
	@RequestMapping("/db2/data/o_backup.do")
	public String backup(String tableNames[],String encoding, ModelMap model,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, InterruptedException {
		String backpath = realPathResolver.get(Constants.BACKUP_PATH);
		File backDirectory = new File(backpath);
		if (!backDirectory.exists()) {
			backDirectory.mkdir();
		}
		DateUtils dateUtils = DateUtils.getDateInstance();
		String backFilePath = backpath + SLASH+ dateUtils.getNowString() + "."
				+ SUFFIX;
		File file=new File(backFilePath);
		Thread thread =new DateBackupTableThread(file,tableNames,encoding);
		thread.start();
		return "data/backupProgress";
	}
	
	@RequiresPermissions("data:v_listfiles")
	@RequestMapping("/db2/data/v_listfiles.do")
	public String listBackUpFiles(ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		model.addAttribute("list",resourceMng.listFile(Constants.BACKUP_PATH, false));
		return "data/files";
	}
	
	@RequiresPermissions("data:v_selectfile")
	@RequestMapping("/db2/data/v_selectfile.do")
	public String selectBackUpFiles(ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		model.addAttribute("list",resourceMng.listFile(Constants.BACKUP_PATH, false));
		return "data/selectfile";
	}
	
	@RequiresPermissions("data:o_delete")
	@RequestMapping("/db2/data/o_delete.do")
	public String delete(String root, String[] names,
			HttpServletRequest request, ModelMap model,HttpServletResponse response) {
		WebErrors errors = validateDelete(names, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		int count = resourceMng.delete(names);
		log.info("delete Resource count: {}", count);
		for (String name : names) {
			log.info("delete Resource name={}", name);
			cmsLogMng.operating(request, "resource.log.delete", "filename="
					+ name);
		}
		model.addAttribute("root", root);
		return listBackUpFiles( model,request,response);
	}
	
	@RequiresPermissions("data:o_delete_single")
	@RequestMapping("/db2/data/o_delete_single.do")
	public String deleteSingle(HttpServletRequest request, ModelMap model,HttpServletResponse response) {
		// TODO 输入验证
		String name = RequestUtils.getQueryParam(request, "name");
		WebErrors errors = validateDelete(new String[]{name}, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		int count = resourceMng.delete(new String[] { name });
		log.info("delete Resource {}, count {}", name, count);
		cmsLogMng.operating(request, "resource.log.delete", "filename=" + name);
		return listBackUpFiles( model,request,response);
	}

	@RequiresPermissions("data:v_rename")
	@RequestMapping(value = "/db2/data/v_rename.do")
	public String renameInput(HttpServletRequest request, ModelMap model) {
		String name = RequestUtils.getQueryParam(request, "name");
		String origName = name.substring(Constants.BACKUP_PATH.length());
		model.addAttribute("origName", origName);
		return "data/rename";
	}
	
	@RequiresPermissions("data:o_rename")
	@RequestMapping(value = "/db2/data/o_rename.do", method = RequestMethod.POST)
	public String renameSubmit(String root, String origName, String distName,
			HttpServletRequest request, ModelMap model,HttpServletResponse response) {
		String orig = Constants.BACKUP_PATH + origName;
		String dist = Constants.BACKUP_PATH + distName;
		resourceMng.rename(orig, dist);
		log.info("name Resource from {} to {}", orig, dist);
		model.addAttribute("root", root);
		return listBackUpFiles( model,request,response);
	}
	
	@RequiresPermissions("data:o_export")
	@RequestMapping(value = "/db2/data/o_export.do")
	public String  exportSubmit(String[] names,ModelMap model,HttpServletRequest request,HttpServletResponse response) 
	throws UnsupportedEncodingException {
		if(validate(names, request)){
			WebErrors errors = WebErrors.create(request);
			errors.addErrorCode(INVALID_PARAM);
			return errors.showErrorPage(model);
		}
		String backName="back";
		if(names!=null&&names.length>0&&names[0]!=null){
			backName=names[0].substring(names[0].indexOf(Constants.BACKUP_PATH)+Constants.BACKUP_PATH.length()+1);
		}
		List<FileEntry> fileEntrys = new ArrayList<FileEntry>();
		response.setContentType("application/x-download;charset=UTF-8");
		response.addHeader("Content-disposition", "filename="
				+ backName+".zip");
		for(String filename:names){
			File file=new File(realPathResolver.get(filename));
			fileEntrys.add(new FileEntry("", "", file));
		}
		try {
			// 模板一般都在windows下编辑，所以默认编码为GBK
			Zipper.zip(response.getOutputStream(), fileEntrys, "GBK");
		} catch (IOException e) {
			log.error("export db error!", e);
		}
		return null;
	}

	
	@RequiresPermissions("data:o_backup_progress")
	@RequestMapping("/db2/data/o_backup_progress.do")
	public void getBackupProgress(HttpServletRequest request, HttpServletResponse response) throws JSONException{
		JSONObject json=new JSONObject();
		json.put("tablename", backup_table);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	
	public  void dbXml(String fileName, String oldDbHost,String dbHost) throws Exception {
		String s = FileUtils.readFileToString(new File(fileName));
		s = StringUtils.replace(s, oldDbHost, dbHost);
		FileUtils.writeStringToFile(new File(fileName), s);
	}
	
	private  String readFile(String filename) throws IOException {
	    File file =new File(filename);
	    if(filename==null || filename.equals(""))
	    {
	      throw new NullPointerException("<@s.m 'db.fileerror'/>");
	    }
	    long len = file.length();
	    byte[] bytes = new byte[(int)len];
	    BufferedInputStream bufferedInputStream=new BufferedInputStream(new FileInputStream(file));
	    int r = bufferedInputStream.read( bytes );
	    if (r != len){
	    	// throw new IOException("<@s.m 'db.filereaderror'/>");
	    }
	    bufferedInputStream.close();
	    return new String(bytes,"utf-8").replace(BR, "");
	}
	
	private WebErrors validateDelete(String[] names,
			HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		errors.ifEmpty(names, "names");
		if(names!=null&&names.length>0){
			for(String name:names){
				//导出阻止非法获取其他目录文件
				if (!name.contains("/WEB-INF/backup/")||name.contains("../")||name.contains("..\\")) {
					errors.addErrorCode(INVALID_PARAM);
				}
			}
		}else{
			errors.addErrorCode(INVALID_PARAM);
		}
		for (String id : names) {
			vldExist(id, errors);
		}
		return errors;
	}
	
	private boolean vldExist(String name, WebErrors errors) {
		if (errors.ifNull(name, "name")) {
			return true;
		}
		return false;
	}
	
	private class DateBackupTableThread extends Thread{
		private File file;
		private String[] tablenames;
		private String encoding;
		public DateBackupTableThread(File file, String[] tablenames,String encoding) {
			super();
			this.file = file;
			this.tablenames = tablenames;
			this.encoding=encoding;
		}
		public void run() {
			FileOutputStream out;
			OutputStreamWriter writer=null;
			try {
				out = new FileOutputStream(file);
				writer = new OutputStreamWriter(out, "utf8");
				writer.append(dataBackMng.disableORenbaleFK(false));
				writer.append(BR);
				try {
					for (int i=0;i<tablenames.length;i++) {
						backup_table=tablenames[i];
						backupTable(writer,tablenames[i],encoding);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				backup_table="";
				writer.append(dataBackMng.disableORenbaleFK(true));
				writer.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private   String backupTable(OutputStreamWriter writer,String tablename,String encoding) throws IOException, SQLException {
			writer.write(createOneTableSql(tablename,encoding));
			writer.flush();
			return tablename;
		}

		private String createOneTableSql(String tablename,String encoding)throws SQLException {
			StringBuffer buffer = new StringBuffer();
			String references=dataBackMng.getTableReferences(tablename);
			//删除表语句
			buffer.append(Constants.ONESQL_PREFIX +DROP_TABLE+ tablename + BRANCH + BR);
			boolean generatedByIdentity=false;
			String identityColumn=dataBackMng.getIdentityColumn(tablename);
			String tableDDL="";
			try {
				tableDDL=dataBackMng.createTableDDL(tablename,encoding);
				if(tableDDL.contains(ENFORCED)&&!tableDDL.contains(NOT_ENFORCED)){
					tableDDL=tableDDL.replace(ENFORCED, NOT_ENFORCED);
				}
				buffer.append(tableDDL + BR);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//存在自动增长列并且存在数据需要插入
			String dataInsertSqls=dataBackMng.createTableDataSQL(tablename);
			if(StringUtils.isNotBlank(tableDDL)&&StringUtils.isNotBlank(identityColumn)&&StringUtils.isNotBlank(dataInsertSqls)){
				generatedByIdentity=true;
				//自增长字段不能插入值，取消自动增长属性
				buffer.append(Constants.ONESQL_PREFIX +ALTER_TABLE+tablename+ALTER_COLUMN+identityColumn+DROP_IDENTITY+BRANCH+BR);
			}
			buffer.append(dataInsertSqls);
			//插入数据后恢复自动增长属性
			if(generatedByIdentity){
				//获取自动增长列最大值
				Integer max=dataBackMng.getMaxValueOfIdentityColumn(tablename);
				Integer identityStart=1;
				if(max!=null){
					identityStart+=max;
				}
				buffer.append(Constants.ONESQL_PREFIX +ALTER_TABLE+tablename+ALTER_COLUMN+identityColumn+SET_IDENTITY_BEGIN+identityStart+SET_IDENTITY_END+BRANCH+BR);
			}
			//从新创建约束
			buffer.append(references);
			return buffer.toString();
		}
	}
	
	private boolean validate(String[] names,HttpServletRequest request) {
		if(names!=null&&names.length>0){
			for(String name:names){
				//导出阻止非法获取其他目录文件
				if (!name.contains(Constants.BACKUP_PATH)||name.contains("../")||name.contains("..\\")) {
					return true;
				}
			}
		}else{
			return true;
		}
		return false;
	}
	
	
	@Autowired
	private RealPathResolver realPathResolver;
	@Autowired
	private CmsDb2DataBackMng dataBackMng;
	@Autowired
	private CmsResourceMng resourceMng;
	@Autowired
	private CmsLogMng cmsLogMng;
}
