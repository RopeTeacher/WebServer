package com.webserver.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.webserver.servlet.HttpServlet;

/**
 * 这里保存所有服务端相关的配置信息
 * @author ta
 *
 */
public class ServerContext {
	private static Map<String,HttpServlet> servletMapping = new HashMap<>();
	
	static {
		initServletMapping();
	}
	/**
	 * 初始化请求路径与对应Servlet的关系
	 */
	private static void initServletMapping(){
//		servletMapping.put("/myweb/reg", new RegServlet());
//		servletMapping.put("/myweb/login", new LoginServlet());
		/*
		 * 解析conf/servlets.xml文件
		 * 将根标签下所有的<servlet>标签获取到
		 * 并且将每个<servlet>标签中的属性:
		 * path的值作为key
		 * className的值利用反射加载对应的类
		 * 并实例化，将实例化的对象造型为
		 * HttpServlet并作为value存入到
		 * servletMapping完成初始化。
		 */
		try {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(
					new File("./conf/servlets.xml"));
			Element root = doc.getRootElement();
			List<Element> list = root.elements();
			for(Element servletEle : list) {
				String path 
					= servletEle.attributeValue("path");
				
				String className 
					= servletEle.attributeValue("className");
				Class cls = Class.forName(className);
				HttpServlet servlet 
					= (HttpServlet)cls.newInstance();
				
				servletMapping.put(path, servlet);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(servletMapping.size());
	} 
	/**
	 * 根据给定的请求路径获取对应的Servlet
	 * @param path
	 * @return
	 */
	public static HttpServlet getServlet(String path) {
		return servletMapping.get(path);
	}
}




