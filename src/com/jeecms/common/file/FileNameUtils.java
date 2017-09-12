package com.jeecms.common.file;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;

import com.jeecms.common.image.ImageUtils;
import com.jeecms.common.ueditor.ResourceType;
import com.jeecms.common.util.Num62;

/**
 * 文件名生成帮助类
 */
public class FileNameUtils {
	/**
	 * 日期格式化对象，将当前日期格式化成yyyyMM格式，用于生成目录。
	 */
	public static final DateFormat pathDf = new SimpleDateFormat("yyyyMM");
	/**
	 * 日期格式化对象，将当前日期格式化成ddHHmmss格式，用于生成文件名。
	 */
	public static final DateFormat nameDf = new SimpleDateFormat("ddHHmmss");

	/**
	 * 生成当前年月格式的文件路径
	 * 
	 * yyyyMM 200806
	 * 
	 * @return
	 */
	public static String genPathName() {
		return pathDf.format(new Date());
	}

	/**
	 * 生产以当前日、时间开头加4位随机数的文件名
	 * 
	 * ddHHmmss 03102230
	 * 
	 * @return 10位长度文件名
	 */
	public static String genFileName() {
		return nameDf.format(new Date())
				+ RandomStringUtils.random(4, Num62.N36_CHARS);
	}

	/**
	 * 生产以当前时间开头加4位随机数的文件名
	 * 
	 * @param ext
	 *            文件名后缀，不带'.'
	 * @return 10位长度文件名+文件后缀
	 */
	public static String genFileName(String ext) {
		return genFileName() + "." + ext;
	}
	
	public static String getFileSufix(String fileName) {
		boolean normalImg=false;
		for(String imgExt:ImageUtils.IMAGE_EXT){
			if(fileName.endsWith(imgExt)){
				normalImg=true;
			}
		}
		String suffix="";
		if(normalImg){
			int splitIndex = fileName.lastIndexOf(".");
			suffix=fileName.substring(splitIndex + 1);
		}else{
			suffix=ImageUtils.IMAGE_EXT[0];
		}
		return suffix;
	}

	public static void main(String[] args) {
		System.out.println(getFileSufix("http://s16.sinaimg.cn/mw690/001sIaVVzy735eKqE232f"));
	}
}
