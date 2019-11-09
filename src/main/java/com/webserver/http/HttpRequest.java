package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.webserver.exception.EmptyRequestException;

/**
 * 请求对象
 * 该类的每一个实例用于表示客户端发送过来的
 * 一个具体的请求内容
 * 一个请求由三部分组成:请求行，消息头，消息正文
 * @author ta
 *
 */
public class HttpRequest {
	//请求行相关信息
	//请求的方式
	private String method;
	//抽象路径
	private String uri;
	//协议版本
	private String protocol;
	//uri当中请求部分(?左侧内容)
	private String requestURI;
	//uri当中参数部分(?右侧内容)
	private String queryString;
	//记录参数部分中的具体每一个参数
	private Map<String,String> parameters = new HashMap<>();
	
	
	//消息头相关信息
	/*
	 * key:消息头的名字
	 * value:消息头对应的值
	 */
	private Map<String,String> headers = new HashMap<>();
	
	
	
	//消息正文相关信息
	
	
	//和连接相关的属性
	private Socket socket;
	private InputStream in;
	/**
	 * 构造方法，用于初始化请求对象
	 * @throws EmptyRequestException 
	 */
	public HttpRequest(Socket socket) throws EmptyRequestException {
		try {
			this.socket = socket;
			this.in = socket.getInputStream();
			/*
			 * 解析请求分为三步:
			 * 1:解析请求行
			 * 2:解析消息头
			 * 3:解析消息正文
			 */
			System.out.println("HttpRequest:开始解析请求...");
			parseRequestLine();
			parseHeaders();
			parseContent();
			System.out.println("HttpRequest:解析请求完毕!");
			
		} catch(EmptyRequestException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 解析请求行
	 * @throws EmptyRequestException 
	 */
	private void parseRequestLine() throws EmptyRequestException {
		System.out.println("HttpRequest:开始解析请求行");
		try {
			/*
			 * 通过输入流读取第一行字符串。
			 * 一个请求中的第一行字符串就是请求
			 * 行的内容。
			 * 读取到以后，按照" "(空格)拆分为三部分
			 * 然后将这三部分内容分别设置到请求行对应
			 * 的属性method,uri,protocol上
			 * 
			 * http://localhost:8088/index.html
			 */

			String line = readLine();
			//判断该请求是否为空请求
			if("".equals(line)) {
				throw new EmptyRequestException();
			}
			
			
			
			String[] data = line.split("\\s");
			method = data[0];
			uri = data[1];
			protocol = data[2];
			
			parseURI();//进一步解析uri
			
			System.out.println("method:"+method);// GET
			System.out.println("uri:"+uri);// /index.html
			System.out.println("protocol:"+protocol);// HTTP/1.1
			
		} catch(EmptyRequestException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("HttpRequest:解析请求行完毕");
	}
	/**
	 * 进一步解析uri
	 */
	private void parseURI() {
		System.out.println("HttpRequest:进一步解析uri...");
		/*
		 * 首先在解析之前，要先判别当前uri是
		 * 否含有参数，判断的依据是看uri当中
		 * 是否含有"?",有则说明含有参数，否则
		 * 就是没有参数。
		 * 如果没有参数，那么直接将uri的值赋值
		 * 给requestURI即可。
		 * 
		 * 若有参数，则应当先按照"?"将uri拆分
		 * 然后将"?"左侧内容赋值给requestURI,
		 * 将"?"右侧内容赋值给queryString
		 * 接着将参数部分再按照"&"拆分为每一组参
		 * 数，每组参数再按照"="拆分为参数名与
		 * 参数值，并将名字作为key,值作为value
		 * 保存到parameters这个Map中完成解析
		 * 
		 * /myweb/reg?username=%E8%8C%8CXXXXXX
		 */
		if(uri.indexOf("?")!=-1) {
			String[] data = uri.split("\\?");
			requestURI = data[0];
			if(data.length>1) {
				queryString = data[1];
				//对queryString中包含的%XX进行转码
				try {
					queryString = URLDecoder.decode(
						queryString, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				parseParameters(queryString);
				
			}
		}else {
			requestURI = uri;
		}
		
		
		
		System.out.println("requestURI:"+requestURI);
		System.out.println("queryString:"+queryString);
		System.out.println("parameters:"+parameters);
		System.out.println("HttpRequest:解析uri完毕!");
	}
	/**
	 * 解析参数,参数是一个字符串.
	 * 格式为:name=value&name=value...
	 * 解析出来的参数会放在parameters这个Map中
	 * @param line
	 */
	private void parseParameters(String line) {
		String[] data = line.split("&");
		for(String str : data) {
			String[] para = str.split("=");
			if(para.length>1) {
				parameters.put(para[0], para[1]);
			}else {
				parameters.put(para[0], null);
			}
		}
	}
	
	/**
	 * 解析消息头
	 */
	private void parseHeaders() {
		System.out.println("HttpRequest:开始解析消息头");
		try {
			/*
			 * 循环调用readLine方法读取每一行
			 * 字符串，每一行就是一个消息头，
			 * 如果readLine方法返回的字符串是
			 * 一个空字符串时就应当停止循环读取
			 * 操作了(因为单独读取到了CRLF)
			 * 
			 * 读取到每一个消息头后，我们可以按照
			 * ": "(即:冒号空格)来进行拆分，将
			 * 消息头的名字作为key，消息头的值作为
			 * value保存到headers这个Map中完成消息
			 * 头的解析工作
			 * 
			 */
			while(true) {
				String line = readLine();
				if("".equals(line)) {
					break;
				}
				String[] data = line.split(":\\s");
				headers.put(data[0], data[1]);
			}
			
			
			System.out.println("headers:"+headers);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("HttpRequest:解析消息头完毕");
	}
	/**
	 * 解析消息正文
	 */
	private void parseContent() {
		System.out.println("HttpRequest:开始解析消息正文");
		/*
		 * 根据消息头中的Content-Length来判定
		 * 当前请求是否含有消息正文
		 */
		if(headers.containsKey("Content-Length")) {
			//首先得到正文的长度(字节量)
			int length = Integer.parseInt(
				headers.get("Content-Length")	
			);
			try {
				//读取正文中所有的字节
				byte[] data = new byte[length];
				in.read(data);
				
				//根据Content-Type指定的正文类型来处理
				String type = headers.get("Content-Type");
				
				//是否为页面form表单提交的用户输入的信息
				if("application/x-www-form-urlencoded".equals(type)) {
					/*
					 * 正文内容就是一个字符串，原GET请求中
					 * url里"?"右侧内容
					 */
					String line = new String(data,"ISO8859-1");
					
					line = URLDecoder.decode(line,"UTF-8");
					
					parseParameters(line);
					
					
				//将来可以再添加分支判断其他类型	
				}
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		
		
		
		
		
		System.out.println("HttpRequest:解析消息正文完毕");
	}
	
	private String readLine() throws IOException {
		int d = -1;
		char c1='a',c2='a';
		StringBuilder builder = new StringBuilder();
		while((d = in.read())!=-1) {
			c2 = (char)d;
			if(c1==13&&c2==10) {
				break;
			}
			builder.append(c2);
			c1 = c2;
		}
		return builder.toString().trim();
	}
	public String getMethod() {
		return method;
	}
	public String getUri() {
		return uri;
	}
	public String getProtocol() {
		return protocol;
	}
	public String getHeader(String name) {
		return headers.get(name);
	}
	public String getRequestURI() {
		return requestURI;
	}
	public String getQueryString() {
		return queryString;
	}
	/**
	 * 根据参数名获取参数值
	 * @param name
	 * @return
	 */
	public String getParameter(String name) {
		return parameters.get(name);
	}
	
	
}







