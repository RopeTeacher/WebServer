package com.webserver.servlet;

import java.io.RandomAccessFile;
import java.util.Arrays;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

/**
 * 用于处理注册业务
 * @author ta
 *
 */
public class RegServlet extends HttpServlet{
	public void service(HttpRequest request,HttpResponse response) {
		System.out.println("RegServlet:开始处理注册...");
		/*
		 * 1:通过request获取用户提交的注册信息
		 * 2:将信息写入user.dat文件保存
		 * 3:设置response响应注册结果页面
		 */
		String username 
			= request.getParameter("username");
		String password
			= request.getParameter("password");
		String nickname
			= request.getParameter("nickname");
		int age = Integer.parseInt(
			request.getParameter("age")	
		);
		System.out.println("username:"+username);
		System.out.println("password:"+password);
		System.out.println("nickname:"+nickname);
		System.out.println("age:"+age);
		
		/*
		 * 每个用户占用100字节
		 * 其中用户名，密码，昵称为字符串各32字节
		 * 年龄是int值固定的4字节。
		 */
		try(
			RandomAccessFile raf 
				= new RandomAccessFile(
						"user.dat","rw");	
		) {
			//要先检查该用户是否已经存在了
			for(int i=0;i<raf.length()/100;i++) {
				raf.seek(i*100);
				byte[] data = new byte[32];
				raf.read(data);
				String name = new String(data,"UTF-8").trim();
				if(name.equals(username)) {
					//重复用户
					forward("/myweb/have_user.html", request, response);
					return;
				}
			}
			
			
			raf.seek(raf.length());
			
			//写入用户名
			byte[] data = username.getBytes("UTF-8");
			//扩容数组到32字节
			data = Arrays.copyOf(data, 32);
			raf.write(data);
			
			//写密码
			data = password.getBytes("UTF-8");
			data = Arrays.copyOf(data, 32);
			raf.write(data);
			
			//写昵称
			data = nickname.getBytes("UTF-8");
			data = Arrays.copyOf(data, 32);
			raf.write(data);
			
			//写年龄
			raf.writeInt(age);
			
			
			//响应注册成功的页面给客户端
			forward("/myweb/reg_success.html", request, response);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
		System.out.println("RegServlet:处理注册完毕!");
	}
}




