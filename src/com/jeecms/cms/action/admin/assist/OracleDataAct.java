package com.jeecms.cms.action.admin.assist;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
import com.jeecms.cms.manager.assist.CmsOracleDataBackMng;
import com.jeecms.cms.manager.assist.CmsResourceMng;

import com.jeecms.cms.Constants;
import com.jeecms.common.util.DateFormatUtils;
import com.jeecms.common.util.DateUtils;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.util.Zipper;
import com.jeecms.common.util.Zipper.FileEntry;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.web.WebErrors;

@Controller
public class OracleDataAct {
	private static String SUFFIX = "sql";
	private static String BR = "\r\n";
	private static String SLASH="/";
	private static String SPACE = " ";
	private static String BRANCH = ";";
	private static String INSERT_INTO = "INSERT INTO ";
	private static String VALUES = "VALUES";
	private static String LEFTBRACE = "(";
	private static String RIGHTBRACE = ")";
	private static final String DROP_TABLE=" DROP TABLE ";
	private static final String ALTER_TABLE=" ALTER TABLE  ";
	private static final String DROP_CONSTRAINT=" DROP CONSTRAINT  ";
	private static final String TO_DATE="to_date";
	private static final String FORMAT_STRING="yyyy-mm-dd hh24:mi:ss";
	private static String QUOTES = "'";
	private static String COMMA = ",";
	private static String CLOB="CLOB";
	private static String EQUALS=":=";
	private static String DECLARE="declare";
	private static String BEGIN="begin";
	private static String END="end";
	private String backup_table="start";
	private static final String INVALID_PARAM = "template.invalidParams";
	private static final Logger log = LoggerFactory.getLogger(ResourceAct.class);
	
	@RequiresPermissions("data:v_list")
	@RequestMapping("/oracle/data/v_list.do")
	public String list(ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		List<String> tables = dataBackMng.listTabels();
		model.addAttribute("tables", tables);
		return "data/list";
	}
	
	@RequiresPermissions("data:v_listfields")
	@RequestMapping("/oracle/data/v_listfields.do")
	public String listfiled(String tablename, ModelMap model,
			HttpServletRequest request, HttpServletResponse response) {
		List<CmsField> list = dataBackMng.listFields(tablename);
		model.addAttribute("list", list);
		return "data/fields";
	}
	

	@RequiresPermissions("data:v_revert")
	@RequestMapping("/oracle/data/v_revert.do")
	public String listDataBases(ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		model.addAttribute("backuppath", Constants.BACKUP_PATH);
		return "data/databases";
	}
	
	@RequiresPermissions("data:o_revert")
	@RequestMapping("/oracle/data/o_revert.do")
	public String revert(String filename,String db,ModelMap model, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String backpath = realPathResolver.get(Constants.BACKUP_PATH);
		String backFilePath = backpath + SLASH +filename;
		String sql=readFile(backFilePath);
		try {
			dataBackMng.executeSQL(sql,Constants.ONESQL_PREFIX);
		} catch (Exception e) {
			WebErrors errors = WebErrors.create(request);
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
	@RequestMapping("/oracle/data/o_backup.do")
	public String backup(String tableNames[], ModelMap model,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, InterruptedException, SQLException {
		String backpath = realPathResolver.get(Constants.BACKUP_PATH);
		File backDirectory = new File(backpath);
		if (!backDirectory.exists()) {
			backDirectory.mkdir();
		}
		DateUtils dateUtils = DateUtils.getDateInstance();
		String backFilePath = backpath + SLASH+ dateUtils.getNowString() + "."
				+ SUFFIX;
		File file=new File(backFilePath);
		List<String> tables = dataBackMng.listTabels();
		model.addAttribute("tables", tables);
		Thread thread =new DateBackupTableThread(file,tableNames);
		thread.start();
		return "data/backupProgress";
	}
	
	@RequiresPermissions("data:o_backup_progress")
	@RequestMapping("/oracle/data/o_backup_progress.do")
	public void getBackupProgress(HttpServletRequest request, HttpServletResponse response) throws JSONException{
		JSONObject json=new JSONObject();
		json.put("tablename", backup_table);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequiresPermissions("data:v_listfiles")
	@RequestMapping("/oracle/data/v_listfiles.do")
	public String listBackUpFiles(ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		model.addAttribute("list",resourceMng.listFile(Constants.BACKUP_PATH, false));
		return "data/files";
	}
	
	
	@RequiresPermissions("data:v_selectfile")
	@RequestMapping("/oracle/data/v_selectfile.do")
	public String selectBackUpFiles(ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		model.addAttribute("list",resourceMng.listFile(Constants.BACKUP_PATH, false));
		return "data/selectfile";
	}
	
	@RequiresPermissions("data:o_delete")
	@RequestMapping("/oracle/data/o_delete.do")
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
	@RequestMapping("/oracle/data/o_delete_single.do")
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
	@RequestMapping(value = "/oracle/data/v_rename.do")
	public String renameInput(HttpServletRequest request, ModelMap model) {
		String name = RequestUtils.getQueryParam(request, "name");
		String origName = name.substring(Constants.BACKUP_PATH.length());
		model.addAttribute("origName", origName);
		return "data/rename";
	}
	
	@RequiresPermissions("data:o_rename")
	@RequestMapping(value = "/oracle/data/o_rename.do", method = RequestMethod.POST)
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
	@RequestMapping(value = "/oracle/data/o_export.do")
	public String exportSubmit(String[] names,ModelMap model,HttpServletRequest request,HttpServletResponse response) 
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
	    if (r != len)
	    //  throw new IOException("<@s.m 'db.filereaderror'/>");
	    bufferedInputStream.close();
	    return new String(bytes,"utf-8");
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
		public DateBackupTableThread(File file, String[] tablenames) {
			super();
			this.file = file;
			this.tablenames = tablenames;
		}
		public void run() {
			FileOutputStream out;
			OutputStreamWriter writer=null;
			try {
				out = new FileOutputStream(file);
				writer = new OutputStreamWriter(out, "utf8");
				//删除外键sql
				for (int i=0;i<tablenames.length;i++) {
					backupDropConstraint(writer,tablenames[i]);
				}
				//备份表结构
				for (int i=0;i<tablenames.length;i++) {
					backup_table=tablenames[i];
					backupTable(writer,tablenames[i]);
					//生成建表脚本中包含约束创建，先删除方便插入数据
					backupDropConstraint(writer,tablenames[i]);
				}
				//备份数据
				for (int i=0;i<tablenames.length;i++) {
					backup_table=tablenames[i];
					backupData(writer,tablenames[i]);
				}
				//重新建立外键约束
				for (int i=0;i<tablenames.length;i++) {
					backupCreateConstraint(writer,tablenames[i]);
				}
				backup_table="finish";
				//writer.append(createSequenceSql());
				writer.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		private   String backupTable(OutputStreamWriter writer,String tablename) throws IOException {
			writer.write(createOneTableSql(tablename));
			writer.flush();
			return tablename;
		}
		
		private   String backupCreateConstraint(OutputStreamWriter writer,String tablename) throws IOException {
			writer.write(createCreateConstraintsSql(tablename));
			writer.flush();
			return tablename;
		}
		
		private   String backupDropConstraint(OutputStreamWriter writer,String tablename) throws IOException {
			writer.write(createDropConstraintSql(tablename));
			writer.flush();
			return tablename;
		}
		
		private   String backupData(OutputStreamWriter writer,String tablename) throws IOException {
			writer.write(insertDatasSql(tablename));
			writer.flush();
			return tablename;
		}
		

		private String createOneTableSql(String tablename) {
			StringBuffer buffer = new StringBuffer();
			//创建表
			buffer.append(BR);
			buffer.append(Constants.ONESQL_PREFIX+DROP_TABLE+tablename+BR);
			buffer.append(Constants.ONESQL_PREFIX+dataBackMng.createTableDDL(tablename).trim());
			/*
			//创建索引
			List<String>indexSqls=dataBackMng.createIndexDDL(tablename);
			for(String indexSql:indexSqls){
				buffer.append(Constants.ONESQL_PREFIX+indexSql);
			}
			*/
			return buffer.toString();
		}

		private String createDropConstraintSql(String tablename) {
			StringBuffer buffer = new StringBuffer();
			//删除外键约束，表创建完成后重新创建
			List<String>constraints=dataBackMng.getFkConstraints(tablename);
			for(String constraint:constraints){
				buffer.append(Constants.ONESQL_PREFIX+ALTER_TABLE+tablename+DROP_CONSTRAINT+constraint+BR);
			}
			return buffer.toString();
		}
		
		private String createCreateConstraintsSql(String tablename) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(BR);
			//表创建完成后重新创建外键约束
			List<String>constraints=dataBackMng.getFkConstraints(tablename);
			for(String constraint:constraints){
				buffer.append(Constants.ONESQL_PREFIX+dataBackMng.createFKconstraintDDL(constraint));
			}
			return buffer.toString();
		}
		
		private String insertDatasSql(String tablename) {
			StringBuffer buffer = new StringBuffer();
			Object[][] oneResult;
			buffer.append(BR);
			//插入数据
			//数组结构 [数据][是否clob类型]
			List<Object[][]> results = dataBackMng.createTableData(tablename);
			List<String>columns=dataBackMng.getColumns(tablename);
			for (int rowIndex = 0; rowIndex < results.size(); rowIndex++) {
				// one insert sql
				oneResult = results.get(rowIndex);
				buffer.append(Constants.ONESQL_PREFIX+createOneInsertSql(tablename,columns,oneResult,rowIndex));
			}
			return buffer.toString();
		}

		private String createOneInsertSql( String tablename,List<String>columns,Object[][] oneResult,int rowIndex) {
			StringBuffer buffer = new StringBuffer();
			String clobDeclareName;
			//声明clob变量
			for (int j = 0; j < oneResult.length; j++) {
				if (oneResult[j] != null&&oneResult[j][0] instanceof String&&(Boolean)oneResult[j][1]) {
					buffer.append(DECLARE+SPACE);
					clobDeclareName=CLOB+"_"+rowIndex+"_"+j;
					buffer.append(clobDeclareName+SPACE+CLOB+EQUALS+QUOTES+ StrUtils.replaceString((String) oneResult[j][0])+ QUOTES+BRANCH);
				}
			}
			buffer.append(SPACE+BEGIN);
			buffer.append(SPACE+INSERT_INTO +  tablename);
			/*列信息可以取消
			buffer.append(LEFTBRACE);
			for(int colIndex=0;colIndex<columns.size()-1;colIndex++){
				buffer.append(columns.get(colIndex)+COMMA);
			}
			buffer.append(columns.get(columns.size()-1)+RIGHTBRACE);
			*/
			buffer.append(SPACE + VALUES + LEFTBRACE);
			for (int j = 0; j < oneResult.length; j++) {
				if (oneResult[j] != null) {
					if (oneResult[j][0] instanceof Date) {
						buffer.append(TO_DATE+LEFTBRACE+QUOTES + DateFormatUtils.formatDateTime((Date)oneResult[j][0]) + QUOTES+COMMA+QUOTES+FORMAT_STRING+QUOTES+RIGHTBRACE);
					} else if (oneResult[j][0] instanceof String) {
						if((Boolean)oneResult[j][1]){
							//clob column
							clobDeclareName=CLOB+"_"+rowIndex+"_"+j;
							buffer.append(clobDeclareName);
						}else{
							buffer.append(QUOTES
									+ StrUtils.replaceKeyString((String) oneResult[j][0])
									+ QUOTES);
						}
					} else if (oneResult[j][0] instanceof Boolean) {
						if ((Boolean) oneResult[j][0]) {
							buffer.append(1);
						} else {
							buffer.append(0);
						}
					} else {
						buffer.append(oneResult[j][0]);
					}
				} else {
					buffer.append(oneResult[j][0]);
				}
				buffer.append(COMMA);
			}
			if(buffer.lastIndexOf(COMMA)!=-1)
			buffer = buffer.deleteCharAt(buffer.lastIndexOf(COMMA));
			buffer.append(RIGHTBRACE+BRANCH);
			//buffer.append(BRANCH);
			buffer.append(SPACE+"commit;");
			buffer.append(SPACE+END+BRANCH+BR);
			return buffer.toString();
		}
	}
	
	@SuppressWarnings("unused")
	private String createSequenceSql() throws SQLException{
		StringBuffer sqlBuffer=new StringBuffer();
		List<String>sequences=dataBackMng.getSequencesList(dataBackMng.getJdbcUserName());
		for(String sequence:sequences){
			sqlBuffer.append(dataBackMng.createSequenceDDL(sequence)+";");
		}
		return sqlBuffer.toString();
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
	private CmsOracleDataBackMng dataBackMng;
	@Autowired
	private CmsResourceMng resourceMng;
	@Autowired
	private CmsLogMng cmsLogMng;
}
