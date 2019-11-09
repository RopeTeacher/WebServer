package com.webserver.servlet;

import java.io.RandomAccessFile;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
/**
 * 登录业务
 * @author ta
 *
 */
public class LoginServlet extends HttpServlet{
	public void service(HttpRequest request,HttpResponse response) {
		//1获取登录信息
		String username 
			= request.getParameter("username");
		String password 
			= request.getParameter("password");
		
		//2验证登录
		try (
			RandomAccessFile raf
				= new RandomAccessFile("user.dat","r");
		){
			for(int i=0;i<raf.length()/100;i++) {
				raf.seek(i*100);
				//读取用户名
				byte[] data = new byte[32];
				raf.read(data);
				String name = new String(data,"UTF-8").trim();
				if(name.equals(username)) {
					//比密码
					raf.read(data);
					String pwd = new String(data,"UTF-8").trim();
					if(pwd.equals(password)) {
						//登录成功
						forward("/myweb/login_success.html", request, response);
						return;
					}
					break;
				}
			}//for循环结束
			
			//如果走到这里，统一设置登录失败
			forward("/myweb/login_fail.html",request,response);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}







