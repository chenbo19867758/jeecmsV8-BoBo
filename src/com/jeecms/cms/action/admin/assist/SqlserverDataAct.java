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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import com.jeecms.cms.Constants;
import com.jeecms.cms.entity.back.CmsField;
import com.jeecms.cms.manager.assist.CmsResourceMng;
import com.jeecms.cms.manager.assist.CmsSqlserverDataBackMng;
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
public class SqlserverDataAct {
	private static String SUFFIX = "sql";
	private static String BR = "\r\n";
	private static String SLASH = "/";
	private static String dbXmlFileName = "/WEB-INF/config/jdbc.properties";
	private static String SPACE = " ";
	private static String BRANCH = ";";
	private static String INSERT_INTO = " INSERT INTO ";
	private static String VALUES = "VALUES";
	private static String LEFTBRACE = "(";
	private static String RIGHTBRACE = ")";
	private static String QUOTES = "'";
	private static String COMMA = ",";
	private static final String INVALID_PARAM = "template.invalidParams";
	private static String backup_table;
	private static final Logger log = LoggerFactory.getLogger(ResourceAct.class);

	@RequiresPermissions("data:v_list")
	@RequestMapping("/sqlserver/data/v_list.do")
	public String list(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		List<String> tables = dataBackMng.listTabels();
		model.addAttribute("tables", tables);
		return "data/list";
	}

	@RequiresPermissions("data:v_listfields")
	@RequestMapping("/sqlserver/data/v_listfields.do")
	public String listfiled(String tablename, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		List<CmsField> list = dataBackMng.listFields(tablename);
		model.addAttribute("list", list);
		return "data/fields";
	}

	@RequiresPermissions("data:v_revert")
	@RequestMapping("/sqlserver/data/v_revert.do")
	public String listDataBases(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		try {
			String defaultCatalog = dataBackMng.getDefaultCatalog();
			model.addAttribute("defaultCatalog", defaultCatalog);
		} catch (SQLException e) {
			model.addAttribute("msg", e.toString());
			return "common/error_message";
		}
		List<String> databases = dataBackMng.listDataBases();
		model.addAttribute("databases", databases);
		model.addAttribute("backuppath", Constants.BACKUP_PATH);
		return "data/databases";
	}

	@RequiresPermissions("data:o_revert")
	@RequestMapping("/sqlserver/data/o_revert.do")
	public String revert(String filename, String db, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String backpath = realPathResolver.get(Constants.BACKUP_PATH);
		String backFilePath = backpath + SLASH + filename;
		String sql = readFile(backFilePath);
		dataBackMng.executeSQL("use [" + db + "]" + BR);
		dataBackMng.executeSQL(sql);
		// 若db发生变化，需要处理jdbc
		try {
			String defaultCatalog = dataBackMng.getDefaultCatalog();
			if (!defaultCatalog.equals(db)) {
				String dbXmlPath = realPathResolver.get(dbXmlFileName);
				dbXml(dbXmlPath, defaultCatalog, db);
			}
		} catch (Exception e) {
			model.addAttribute("msg", e.toString());
			return "common/error_message";
		}
		model.addAttribute("msg", "success");
		return listDataBases(model, request, response);
	}

	@RequiresPermissions("data:o_backup")
	@RequestMapping("/sqlserver/data/o_backup.do")
	public String backup(String tableNames[], ModelMap model, HttpServletRequest request, HttpServletResponse response)
			throws IOException, InterruptedException {
		String backpath = realPathResolver.get(Constants.BACKUP_PATH);
		File backDirectory = new File(backpath);
		if (!backDirectory.exists()) {
			backDirectory.mkdir();
		}
		DateUtils dateUtils = DateUtils.getDateInstance();
		String backFilePath = backpath + SLASH + dateUtils.getNowString() + "." + SUFFIX;
		File file = new File(backFilePath);
		List<String> tables = dataBackMng.listTabels();
		model.addAttribute("tables", tables);
		Thread thread = new DateBackupTableThread(file, tableNames);
		thread.start();
		return "data/backupProgress";
	}

	@RequiresPermissions("data:o_backup_progress")
	@RequestMapping({ "/sqlserver/data/o_backup_progress.do" })
	public void getBackupProgress(HttpServletRequest request, HttpServletResponse response) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("tablename", backup_table);
		ResponseUtils.renderJson(response, json.toString());
	}

	@RequiresPermissions("data:v_listfiles")
	@RequestMapping("/sqlserver/data/v_listfiles.do")
	public String listBackUpFiles(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		model.addAttribute("list", resourceMng.listFile(Constants.BACKUP_PATH, false));
		return "data/files";
	}

	@RequiresPermissions("data:v_selectfile")
	@RequestMapping("/sqlserver/data/v_selectfile.do")
	public String selectBackUpFiles(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		model.addAttribute("list", resourceMng.listFile(Constants.BACKUP_PATH, false));
		return "data/selectfile";
	}

	@RequiresPermissions("data:o_delete")
	@RequestMapping("/sqlserver/data/o_delete.do")
	public String delete(String root, String[] names, HttpServletRequest request, ModelMap model,
			HttpServletResponse response) {
		WebErrors errors = validateDelete(names, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		int count = resourceMng.delete(names);
		log.info("delete Resource count: {}", count);
		for (String name : names) {
			log.info("delete Resource name={}", name);
			cmsLogMng.operating(request, "resource.log.delete", "filename=" + name);
		}
		model.addAttribute("root", root);
		return listBackUpFiles(model, request, response);
	}

	@RequiresPermissions("data:o_delete_single")
	@RequestMapping("/sqlserver/data/o_delete_single.do")
	public String deleteSingle(HttpServletRequest request, ModelMap model, HttpServletResponse response) {
		// TODO 输入验证
		String name = RequestUtils.getQueryParam(request, "name");
		WebErrors errors = validateDelete(new String[] { name }, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		int count = resourceMng.delete(new String[] { name });
		log.info("delete Resource {}, count {}", name, count);
		cmsLogMng.operating(request, "resource.log.delete", "filename=" + name);
		return listBackUpFiles(model, request, response);
	}

	@RequiresPermissions("data:v_rename")
	@RequestMapping(value = "/sqlserver/data/v_rename.do")
	public String renameInput(HttpServletRequest request, ModelMap model) {
		String name = RequestUtils.getQueryParam(request, "name");
		String origName = name.substring(Constants.BACKUP_PATH.length());
		model.addAttribute("origName", origName);
		return "data/rename";
	}

	@RequiresPermissions("data:o_rename")
	@RequestMapping(value = "/sqlserver/data/o_rename.do", method = RequestMethod.POST)
	public String renameSubmit(String root, String origName, String distName, HttpServletRequest request,
			ModelMap model, HttpServletResponse response) {
		String orig = Constants.BACKUP_PATH + origName;
		String dist = Constants.BACKUP_PATH + distName;
		resourceMng.rename(orig, dist);
		log.info("name Resource from {} to {}", orig, dist);
		model.addAttribute("root", root);
		return listBackUpFiles(model, request, response);
	}

	@RequiresPermissions("data:o_export")
	@RequestMapping(value = "/sqlserver/data/o_export.do")
	public String exportSubmit(String[] names, ModelMap model, HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		if (validate(names, request)) {
			WebErrors errors = WebErrors.create(request);
			errors.addErrorCode(INVALID_PARAM);
			return errors.showErrorPage(model);
		}
		String backName = "back";
		if (names != null && names.length > 0 && names[0] != null) {
			backName = names[0].substring(names[0].indexOf(Constants.BACKUP_PATH) + Constants.BACKUP_PATH.length() + 1);
		}
		List<FileEntry> fileEntrys = new ArrayList<FileEntry>();
		response.setContentType("application/x-download;charset=UTF-8");
		response.addHeader("Content-disposition", "filename=" + backName + ".zip");
		for (String filename : names) {
			File file = new File(realPathResolver.get(filename));
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

	public void dbXml(String fileName, String oldDbHost, String dbHost) throws Exception {
		String s = FileUtils.readFileToString(new File(fileName));
		s = StringUtils.replace(s, oldDbHost, dbHost);
		FileUtils.writeStringToFile(new File(fileName), s);
	}

	private String readFile(String filename) throws IOException {
		File file = new File(filename);
		if (filename == null || filename.equals("")) {
			throw new NullPointerException("<@s.m 'db.fileerror'/>");
		}
		long len = file.length();
		byte[] bytes = new byte[(int) len];
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
		int r = bufferedInputStream.read(bytes);
		if (r != len)
			// throw new IOException("<@s.m 'db.filereaderror'/>");
			bufferedInputStream.close();
		return new String(bytes, "utf-8");
	}

	private WebErrors validateDelete(String[] names, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		errors.ifEmpty(names, "names");
		if (names != null && names.length > 0) {
			for (String name : names) {
				// 导出阻止非法获取其他目录文件
				if (!name.contains("/WEB-INF/backup/") || name.contains("../") || name.contains("..\\")) {
					errors.addErrorCode(INVALID_PARAM);
				}
			}
		} else {
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

	private boolean validate(String[] names, HttpServletRequest request) {
		if (names != null && names.length > 0) {
			for (String name : names) {
				// 导出阻止非法获取其他目录文件
				if (!name.contains(Constants.BACKUP_PATH) || name.contains("../") || name.contains("..\\")) {
					return true;
				}
			}
		} else {
			return true;
		}
		return false;
	}

	private class DateBackupTableThread extends Thread {
		private File file;
		private String[] tablenames;

		public DateBackupTableThread(File file, String[] tablenames) {
			this.file = file;
			this.tablenames = tablenames;
		}

		public void run() {
			OutputStreamWriter writer = null;
			try {
				FileOutputStream out = new FileOutputStream(file);
				writer = new OutputStreamWriter(out, "utf8");

				for (int i = 0; i < this.tablenames.length; i++) {
					backup_table = tablenames[i];
					nocheckConstraint(writer, this.tablenames[i]);
				}

				for (int i = 0; i < this.tablenames.length; i++) {
					backup_table = tablenames[i];
					backupTable(writer, this.tablenames[i]);
				}

				for (int i = 0; i < this.tablenames.length; i++) {
					backup_table = tablenames[i];
					checkConstraint(writer, this.tablenames[i]);
				}

				backup_table = "";
				writer.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private String nocheckConstraint(OutputStreamWriter writer, String tablename) throws IOException {
			StringBuffer buffer = new StringBuffer();
			buffer.append("ALTER TABLE " + tablename + " NOCHECK CONSTRAINT ALL; " + SqlserverDataAct.BR);
			writer.write(buffer.toString());
			writer.flush();
			return tablename;
		}

		private String checkConstraint(OutputStreamWriter writer, String tablename) throws IOException {
			StringBuffer buffer = new StringBuffer();
			buffer.append("ALTER TABLE " + tablename + " CHECK CONSTRAINT ALL; " + SqlserverDataAct.BR);
			writer.write(buffer.toString());
			writer.flush();
			return tablename;
		}

		private String backupTable(OutputStreamWriter writer, String tablename) throws IOException {
			String sql = createOneTableSql(tablename);
			writer.write(sql);

			sql = createOneTableConstraintSql(sql, tablename);
			writer.write(sql);
			writer.flush();
			return tablename;
		}

		private String createOneTableSql(String tablename) {
			StringBuffer buffer = new StringBuffer();

			buffer.append(getNoCheckReference(tablename));

			buffer.append(dataBackMng.createTableDDL(tablename));

			List<Object[]> results = dataBackMng.createTableData(tablename);
			List<String> columns = dataBackMng.getColumns(tablename);
			if ((buffer.toString().contains(" IDENTITY")) && (results.size() > 0)) {
				buffer.append("SET IDENTITY_INSERT   " + tablename + " ON" + SqlserverDataAct.BR);
			}
			for (int i = 0; i < results.size(); i++) {
				Object[] oneResult = (Object[]) results.get(i);
				buffer.append(createOneInsertSql(tablename, columns, oneResult));
			}
			if ((buffer.toString().contains(" IDENTITY")) && (results.size() > 0)) {
				buffer.append("SET IDENTITY_INSERT  " + tablename + " OFF" + SqlserverDataAct.BR);
			}
			return buffer.toString();
		}

		private String createOneTableConstraintSql(String sql, String tablename) {
			StringBuffer buffer = new StringBuffer();

			buffer.append(dataBackMng.createConstraintDDL(sql, tablename));
			return buffer.toString();
		}

		private String createOneInsertSql(String tablename, List<String> columns, Object[] oneResult) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(Constants.ONESQL_PREFIX + SqlserverDataAct.INSERT_INTO + tablename);
			buffer.append(SqlserverDataAct.LEFTBRACE);
			for (int colIndex = 0; colIndex < columns.size() - 1; colIndex++) {
				buffer.append("[" + (String) columns.get(colIndex) + "]" + SqlserverDataAct.COMMA);
			}
			buffer.append("[" + (String) columns.get(columns.size() - 1) + "]" + SqlserverDataAct.RIGHTBRACE);
			buffer.append(SqlserverDataAct.SPACE + SqlserverDataAct.VALUES + SqlserverDataAct.LEFTBRACE);
			for (int j = 0; j < oneResult.length; j++) {
				if (oneResult[j] != null) {
					if ((oneResult[j] instanceof Date))
						buffer.append(SqlserverDataAct.QUOTES + oneResult[j] + SqlserverDataAct.QUOTES);
					else if ((oneResult[j] instanceof String))
						buffer.append(SqlserverDataAct.QUOTES +
								// 整合版
								StrUtils.replaceKeyString(oneResult[j].toString().replace("'", ""))
								+ SqlserverDataAct.QUOTES);
					else if ((oneResult[j] instanceof Boolean)) {
						if (((Boolean) oneResult[j]).booleanValue())
							buffer.append(1);
						else
							buffer.append(0);
					} else
						buffer.append(oneResult[j]);
				} else {
					buffer.append(oneResult[j]);
				}
				buffer.append(SqlserverDataAct.COMMA);
			}
			if (buffer.lastIndexOf(SqlserverDataAct.COMMA) != -1)
				buffer = buffer.deleteCharAt(buffer.lastIndexOf(SqlserverDataAct.COMMA));
			buffer.append(RIGHTBRACE + BRANCH + BR);
			return buffer.toString();
		}

		private String getNoCheckReference(String tablename) {
			Map<String, String> refers = dataBackMng.getBeReferForeignKeyFromTable(tablename);
			StringBuffer sqlBuffer = new StringBuffer();
			Iterator<String> keyIt = refers.keySet().iterator();

			if ((refers != null) && (!refers.isEmpty())) {
				while (keyIt.hasNext()) {
					String key = (String) keyIt.next();
					sqlBuffer.append(
							"ALTER TABLE [" + (String) refers.get(key) + "]" + " DROP   CONSTRAINT " + key + BR);
				}
			}

			return sqlBuffer.toString();
		}

	}

	@Autowired
	private RealPathResolver realPathResolver;
	@Autowired
	private CmsSqlserverDataBackMng dataBackMng;
	@Autowired
	private CmsResourceMng resourceMng;
	@Autowired
	private CmsLogMng cmsLogMng;
}
