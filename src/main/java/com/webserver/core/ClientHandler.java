package com.webserver.core;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import com.webserver.exception.EmptyRequestException;
import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import com.webserver.servlet.HttpServlet;

/**
 * 该线程负责处理与指定客户端的交互工作
 * 处理过程分为三步:
 * 1:准备工作
 * 2:处理请求
 * 3:发送响应
 * @author ta
 *
 */
public class ClientHandler implements Runnable{
	private Socket socket;
	public ClientHandler(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		try {
			//1 准备工作
			HttpRequest request = new HttpRequest(socket);
			HttpResponse response = new HttpResponse(socket);
			/*
			 * 2 处理请求
			 * 2.1:通过request获取客户端请求的资源
			 *     对应的抽象路径requestURI的值
			 * 2.2:从webapps目录中通过对应的抽象路
			 *     径寻找该资源    
			 */
			String path = request.getRequestURI();
			System.out.println("path:"+path);
			
			
			//首先判断该请求是否请求为业务
			HttpServlet servlet 
				= ServerContext.getServlet(path);
			if(servlet!=null) {
				//处理业务
				servlet.service(request,response);
							
			}else {			
				File file = new File("./webapps"+path);
				if(file.exists()) {
					System.out.println("该资源已找到!");
					
					response.setEntity(file);
		
					System.out.println("响应客户端完毕!");
				}else {
					System.out.println("该资源不存在!");
					
					File notFound 
						= new File("./webapps/root/404.html");
					//设置状态代码与描述
					response.setStatusCode(404);
					response.setStatusReason("NOT FOUND");
					
					//设置正文文件为404页面
					response.setEntity(notFound);
									
				}
			}
			
			//3响应客户端
			response.flush();
		} catch(EmptyRequestException e) {
			//单独捕获空请求，不做任何处理。
			System.out.println("空请求...");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			/*
			 * 本次响应完毕后与客户端断开连接
			 * 这个操作是HTTP1.0的方式。
			 * 1.1允许建立连接后进行多次请求
			 * 响应，但是需要额外的消息头和
			 * 响应头的处理
			 */
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
}





