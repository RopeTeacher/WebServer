package com.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 响应对象
 * 该类的每一个实例用于表示发送给客户端的一个
 * 具体的响应内容。
 * 每个响应包含三部分内容:
 * 状态行，响应头，响应正文
 * @author ta
 *
 */
public class HttpResponse {
	//状态行相关信息
	private int statusCode = 200;
	private String statusReason = "OK";
	
	//响应头相关信息
	private Map<String,String> headers = new HashMap<>();
	
	
	//响应正文相关信息
	//响应正文对应的实体文件
	private File entity;
	
	//与连接相关的信息
	private Socket socket;
	private OutputStream out;
	
	public HttpResponse(Socket socket) {
		try {
			this.socket = socket;
			this.out = socket.getOutputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将当前响应对象内容以一个标准的响应
	 * 格式发送给客户端
	 */
	public void flush() {
		/*
		 * 1发送状态行
		 * 2发送响应头
		 * 3发送响应正文
		 */
		sendStatusLine();
		sendHeaders();
		sendContent();
	}
	/**
	 * 发送状态行
	 */
	private void sendStatusLine() {
		System.out.println("HttpResponse:开始发送状态行...");
		try {
			//发送状态行
			String line = "HTTP/1.1"+" "+statusCode+" "+statusReason;
			System.out.println("状态行:"+line);
			out.write(line.getBytes("ISO8859-1"));
			out.write(13);//written CR
			out.write(10);//written LF
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("HttpResponse:状态行发送完毕!");
	}
	/**
	 * 发送响应头
	 */
	private void sendHeaders() {
		System.out.println("HttpResponse:开始发送响应头...");
		try {
			/*
			 * 遍历headers，将所有响应头发送
			 */
			Set<Entry<String,String>> entrySet 
								= headers.entrySet();
			for(Entry<String,String> header : entrySet) {
				String name = header.getKey();
				String value = header.getValue();
				String line = name+": "+value;
				System.out.println("响应头:"+line);
				out.write(line.getBytes("ISO8859-1"));
				out.write(13);
				out.write(10);
			}
			
			
			//单独发送CRLF表示响应头发送完毕
			out.write(13);//written CR
			out.write(10);//written LF
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("HttpResponse:响应头发送完毕!");
	}
	/**
	 * 发送响应正文
	 */
	private void sendContent() {
		System.out.println("HttpResponse:开始发送响应正文...");
		if(entity!=null) {
			try(
				FileInputStream fis 
					= new FileInputStream(entity);
			){
				//发送响应正文
				int len = -1;
				byte[] data = new byte[1024*10];
				while((len = fis.read(data))!=-1) {
					out.write(data,0,len);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("HttpResponse:响应正文发送完毕!");
	}

	public File getEntity() {
		return entity;
	}
	/**
	 * 设置响应正文实体文件
	 * 设置的同时会自动添加两个响应头:
	 * Content-Type与Content-Length
	 * @param entity
	 */
	public void setEntity(File entity) {
		this.entity = entity;
		String fileName = entity.getName();
		System.out.println("资源名:"+fileName);
		//获取该资源后缀名
		String ext = fileName.substring(
			fileName.lastIndexOf(".")+1
		).toLowerCase();
		System.out.println("资源后缀名:"+ext);
		
		String type = HttpContext.getMimeType(ext);
		
		putHeader("Content-Type", type);
		putHeader("Content-Length", entity.length()+"");
		
		
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}
	/**
	 * 向当前响应对象中添加新的响应头
	 * @param name 响应头的名字
	 * @param value 响应头的值
	 */
	public void putHeader(String name,String value) {
		this.headers.put(name, value);
	}
	
}








