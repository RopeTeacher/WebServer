package com.webserver.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebServer是模拟Tomcat的一个web容器
 * web容器可以同时管理多个网络应用，并且提供了
 * 与客户端(通常是浏览器)的网络连接以及传输数据
 * 和与客户端的应用层交互(涉及到TCP协议以及HTTP
 * 协议)上面的支持。有了web容器，使得程序员更多
 * 的经历是放在具体Web应用的业务上。
 * 
 * webapp(网络应用):它包含的内容大致有网页，
 * 图片，其他静态素材以及java程序代码，就是
 * 我们上网时俗称的一个"网站"的全部内容。
 * 
 * 
 * TCP协议，处于传输层，负责两台计算机之间通过
 * 网络传输数据的协议。
 * 
 * HTTP协议，处于应用层，规定了双方发送数据的
 * 格式，以及交互规则。
 * 
 * 
 * @author ta
 *
 */
public class WebServer {
	private ServerSocket server;
	private ExecutorService threadPool;
	/**
	 * 初始化服务端
	 */
	public WebServer() {
		try {
			System.out.println("正在启动服务端...");
			server = new ServerSocket(8088);
			threadPool = Executors.newFixedThreadPool(50);
			System.out.println("服务端启动完毕!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 服务端开始工作的方法
	 */
	public void start() {
		try {
			while(true) {
				System.out.println("等待客户端连接...");
				Socket socket = server.accept();
				System.out.println("一个客户端连接了!");
				
				//启动一个线程处理该客户端交互
				ClientHandler handler 
					= new ClientHandler(socket);
				
				threadPool.execute(handler);
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		WebServer server = new WebServer();
		server.start();
	}
}









