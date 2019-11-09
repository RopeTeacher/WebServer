package com.webserver.http;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * HTTP协议规定之内容
 * @author ta
 *
 */
public class HttpContext {
	private static Map<String,String> mimeMapping = new HashMap<>();
	
	
	static {
		//初始化所有静态属性
		initMimeMapping();
	}
	/**
	 * 初始化资源后缀与Content-Type对应值
	 */
	private static void initMimeMapping() {
//		mimeMapping.put("html", "text/html");
//		mimeMapping.put("css", "text/css");
//		mimeMapping.put("js", "application/javascript");
//		mimeMapping.put("png", "image/png");
//		mimeMapping.put("jpg", "image/jpeg");
//		mimeMapping.put("gif", "image/gif");
		/*
		 * 解析conf/web.xml文件
		 * 将根标签下所有名为<mime-mapping>的
		 * 子标签获取出来，并将它下面的:
		 * <extension>标签中的文本作为key
		 * <mime-type>标签中的文本作为value
		 * 初始化mimeMapping这个Map。
		 * 
		 * 初始化完毕后，mimeMapping这个Map中
		 * 应当有1000多个元素
		 */
		try {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new File("conf/web.xml"));
			Element root = doc.getRootElement();
			List<Element> list = root.elements("mime-mapping");
			for(Element e : list) {
				String key = e.elementTextTrim("extension");
				String value = e.elementTextTrim("mime-type");
				mimeMapping.put(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(mimeMapping.size());
	}
	
	/**
	 * 根据给定的资源后缀名获取对应的Content-Type
	 * 的值
	 * @param ext
	 * @return
	 */
	public static String getMimeType(String ext) {
		return mimeMapping.get(ext);
	}
	
	
	public static void main(String[] args) {
		String type = getMimeType("js");
		System.out.println(type);
	}
}








