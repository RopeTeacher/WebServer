

### 文章目录

- [项目介绍](https://blog.csdn.net/weixin_41935702/article/details/86917205#_2)

- [具体实现](https://blog.csdn.net/weixin_41935702/article/details/86917205#_8)

- - [初步搭建类和结构](https://blog.csdn.net/weixin_41935702/article/details/86917205#_9)

  - - [测试浏览器与服务端的连接](https://blog.csdn.net/weixin_41935702/article/details/86917205#_10)

  - [服务端的三步曲](https://blog.csdn.net/weixin_41935702/article/details/86917205#_102)

  - - [解析请求](https://blog.csdn.net/weixin_41935702/article/details/86917205#_103)
    - [处理请求](https://blog.csdn.net/weixin_41935702/article/details/86917205#_340)
    - [发送响应](https://blog.csdn.net/weixin_41935702/article/details/86917205#_413)

  - [状态码与对应的描述](https://blog.csdn.net/weixin_41935702/article/details/86917205#_577)

  - [DOM4J读取xml文件](https://blog.csdn.net/weixin_41935702/article/details/86917205#DOM4Jxml_1019)

  - [实现服务器功能之注册](https://blog.csdn.net/weixin_41935702/article/details/86917205#_1125)

  - [实现服务器功能之登录](https://blog.csdn.net/weixin_41935702/article/details/86917205#_1557)

  - [添加反射](https://blog.csdn.net/weixin_41935702/article/details/86917205#_1726)

- [结合服务器实战ATM项目遇到的问题](https://blog.csdn.net/weixin_41935702/article/details/86917205#ATM_1782)

- - - [用户登入之后想立即看到自己的信息，如何实现](https://blog.csdn.net/weixin_41935702/article/details/86917205#_1783)
    - [用户登入之后，可能会进行一系列的操作，比如修改个人的一些信息。当用户修改完后，点击返回主页面，如何将用户的信息立即同步到登录成功的主界面](https://blog.csdn.net/weixin_41935702/article/details/86917205#_1826)

- [心得总结](https://blog.csdn.net/weixin_41935702/article/details/86917205#_1900)



# 项目介绍

基于：Java EE、多线程、Socket网络编程、XML解析，反射

虽然要用到的东西不是很多，但是却充满了挑战。通过这个项目可以大致的了解浏览器与服务器之间的数据传递，请求是如何发送的，以及服务端又是如何处理请求，最后又是如何的发送响应。

# 具体实现

## 初步搭建类和结构

### 测试浏览器与服务端的连接

创建核心包 com.webserver.core
创建类WebServer,ClientHandler

```java
package com.webserver.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
	private ServerSocket server;

	public WebServer() {
		// 初始化server
		try {
			server = new ServerSocket(8088);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			// 启动一个线程处理该客户端交互
			Socket socket = server.accept();
			//将socket传递给ClientHander
			ClientHandler handler=new ClientHandler(socket);
			Thread t=new Thread(handler);
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		WebServer server = new WebServer();
		System.out.println("服务端开始启动");
		server.start();
	}
}
```

初始化server,启动线程处理与客户端的交互

```java
package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;

import java.net.Socket;

/**
 * 用户处理客户端请求并予以响应的处理类
 * http://localhost:8088/index.html
 */
public class ClientHandler implements Runnable{
	private Socket socket;
	public ClientHandler(Socket socket){
		this.socket=socket;
	}
	public void run() {
		System.out.println("一个客户端连接了");
		try {
			/**
			 * 获取输入流，用户读取客户端发送过来的内容，由于客户端（浏览器）发送过来的内容是Http协议规定的请求
			 * 请求的内容大部分为文本数据，且字符集为ISO8859-1，内容为英文很数字符号。还可能包含二进制数据
			 * 所以这里我们不能使用流链接字符的高级流，否则读取二进制数据部分时可能出现问题
			 */
			InputStream in=socket.getInputStream();
			int d=-1;
			while((d=in.read())!=-1){
				char c=(char)d;
				System.out.print(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
```

测试一
![测试一结果](https://img-blog.csdnimg.cn/20190210171835553.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MTkzNTcwMg==,size_16,color_FFFFFF,t_70)
打开浏览器，输入http://localhost:8088/index.html，可以看到，我们的WebServer成功的捕捉到了请求，同时我们也打印了浏览器发过来的请求。

## 服务端的三步曲

### 解析请求

**科普HTTP请求格式**
一个HTTP请求分为三部分组成:**请求行，消息头，消息正文**
1:请求行
请求行分为三部分:
请求方法 资源路径 协议(CRLF)
例如:
GET /index.html HTTP/1.1(CRLF)

请求行以CRLF结束
CR:回车符,asc编码中对应数字13
LF:换行符,asc编码中对应数字10

在ClientHander里面添加一个方法：readLine，用于测试通过输入流读取一行客户端发送过来的字符串

```java
package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;

import java.net.Socket;

/**
 * 用户处理客户端请求并予以响应的处理类
 */
public class ClientHandler implements Runnable{
	private Socket socket;
	public ClientHandler(Socket socket){
		this.socket=socket;
	}
	public void run() {
		System.out.println("一个客户端连接了");
		try {
			/**
			 * 获取输入流，用户读取客户端发送过来的内容，由于客户端（浏览器）发送过来的内容是Http协议规定的请求
			 * 请求的内容大部分为文本数据，且字符集为ISO8859-1，内容为英文很数字符号。还可能包含二进制数据
			 * 所以这里我们不能使用流链接字符的高级流，否则读取二进制数据部分时可能出现问题
			 */
			InputStream in=socket.getInputStream();
			String line=readLine(in);
			System.out.println("line:"+line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//在ClientHander里面添加一个方法：readLine，用于测试通过输入流读取一行客户端发送过来的字符串
	private String readLine(InputStream in) throws IOException{
		StringBuilder builder=new StringBuilder();
		int cur=-1;//本次读的字符串
		int pre=-1;//上次读的字符串
		while((cur=in.read())!=-1){
			if(cur==10&&pre==13){
				break;
			}
			builder.append((char)cur);
			pre=cur;
		}
		return builder.toString().trim();
	}
}
```

测试二
![添加readLine方法后的测试结果](https://img-blog.csdnimg.cn/20190210173750348.png)
通过readLine方法，我们成功的拿到请求行
接下来进入了我们解析请求行了
新建一个包：com.webserver.http，在http包中新建类：HTTPRequest，即HTTP请求对象
在HttpRequest中定义请求对应的相关属性信息的构造方法，并且定义三个私有方法parseRequestLine， parseHeaders，parseContent

```java
package com.webserver.http;
/**
 * 请求对象
 * 该类的每一个实例用于表示客户端发送过来的一个实际的HTTP请求内容
 * 每个请求由三部分组成
 * 1.请求行
 * 2.消息头
 * 3.消息正文（可以不包含）
 * @author admin
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HttpRequest {
	/*
	 * 消息头的相关信息定义
	 */
	private Map<String,String> headers=new HashMap<String,String>();
	/*
	 * 请求行相关消息定义
	 */
	//请求方式
	private String method;
	//请求路径
	private String url;
	//
	private String protocol;
	/*
	 * 定义与连接相关的属性
	 */
	private Socket socket;
	private InputStream in;
	public HttpRequest(Socket socket){
		try {
			this.socket=socket;
			this.in=socket.getInputStream();
			/*
			 *实例化一个HttpRequest要解析客户端发送过来的请求内容，并且分析其中每个部分
			 *1.解析请求行
			 *2.解析消息头
			 *3.解析消息正文 
			 */
			parseRequestLine();
			parseHeaders();
			parseContext();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 解析请求行
	 */
	private void parseRequestLine(){
		System.out.println("开始解析请求行");
		/**
		 * 读一行字符串，即请求行的内容，将字符串按空格拆分三部分
		 * 分别设置到method,url,protocol属性上即可
		 */
		try {
			String line=readLine(in);
			System.out.println("请求行内容"+line);
			/*
			 *下面代码可能抛出数组下标越界，这是由于空请求引起 
			 */
			
			String arr[]=line.split(" ");
			method=arr[0];
			url=arr[1];
			protocol=arr[2];
			System.out.println("method   "+method);
			System.out.println("url  "+url);
			System.out.println("protocol  "+protocol);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("请求行解析完毕");
	}
	public String getMethod() {
		return method;
	}
	public String getProtocol() {
		return protocol;
	}
	public String getUrl() {
		return url;
	}
	/**
	 * 解析消息头
	 */
	private void parseHeaders(){
		System.out.println("开始解析消息头");
		String line=null;
		while(true){
			try {
				line=readLine(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if("".equals(line)){
				break;
			}
			/*
			 *将消息头按照":"拆分为两项，消息头的名字当做key，消息头的值当做value保存到headers中 
			 */
			String arr[]=line.split(": ");
			headers.put(arr[0], arr[1]);
		}
		System.out.println("消息头解析完毕");
		System.out.println("headers:"+headers);
		Set<Entry<String,String>> entrySet=headers.entrySet();
		for(Entry<String,String> e:entrySet){
			String key=e.getKey();
			String value=e.getValue();
			System.out.println(key+":"+value);
		}
	}
	/**
	 * 解析消息正文
	 */
	
	private void parseContext(){
		System.out.println("开始解析消息正文");
		System.out.println("消息正文解析完毕");
	}
	/**
	 * 通过输入流读取客户端发送的一行字符串
	 * 该方法会连续的读取若干字符，当连续读到CRLF的时候停止读取，
	 * 并将之前读到的所有字符以一个字符串的形式返回
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	
	private String readLine(InputStream in) throws IOException{
		StringBuilder builder=new StringBuilder();
		int cur=-1;//本次读的字符串
		int pre=-1;//上次读的字符串
		while((cur=in.read())!=-1){
			if(cur==10&&pre==13){
				break;
			}
			builder.append((char)cur);
			pre=cur;
		}
		return builder.toString().trim();
	}
	/**
	 * 根据给定的消息头获得对应的值
	 * @param name
	 * @return
	 */
	public String getHeader(String name){
		return headers.get(name);
	}
}
```

### 处理请求

1.在项目目录下新建所有保存所有网络应用的目录：webapps
2.在webapps目录下创建一个子目录，作为我们第一个网络应用名为：myweb
3.在myweb目录中新建该应用的第一个页面:index.html

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190210180357733.png)
简单的写一个.html

```html
<html>
<head>
<meta charset="utf-8">
<title>我的首页</title>
</head>
<body>
	<center>
		<h1>百度</h1>
		<input type="text" size=32> <input type="button" value="百度一下" onclick="alert('点你妹啊')">
	</center>
</body>
</html>
```

在ClientHandler的run方法中添加处理请求的逻辑,根据request获取请求的抽象路径，然后再webapps目录中对应的抽象路径找到客户请求的资源 (添加分支，判断资源是否存在)

```java
package com.webserver.core;

import java.io.File;

import java.net.Socket;

import com.webserver.http.HttpRequest;

/**
 * 用户处理客户端请求并予以响应的处理类
 */
public class ClientHandler implements Runnable{
	private Socket socket;
	public ClientHandler(Socket socket){
		this.socket=socket;
	}
	public void run() {
		System.out.println("一个客户端连接了");
		try {
		/**
		 * 处理客户端的请求分三步
		 * 1.解析请求
		 * 2.处理请求
		 * 3.发送响应
		 */
			HttpRequest request=new HttpRequest(socket);
			//获取请求的抽象路径
			String path=request.getUrl();
			//去webapps目录下面找对应的资源
			File file=new File("webapps/myweb"+path);
			System.out.println("path:"+path);
			//判断资源是否存在
			if(file.exists()){
				System.out.println("资源找到了");
			}else{
				System.out.println("资源未找到");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
```

测试三
![测试文件是都能够找到资源](https://img-blog.csdnimg.cn/20190210181514513.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MTkzNTcwMg==,size_16,color_FFFFFF,t_70)

### 发送响应

在Http包中定义响应对象HttpResponse,用这个类的每一个实例表示一个实际发给客户端的响应内容
并在该类中定义方法：flush用于将当前响应的内容发送给客户端
flush方法中应当完成三部分的发送：转态行，响应头，响应正文
sendStatusLine();
sendHeaders();
sendContent();

```java
package com.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * 响应对象 该类的每个实例用于表示发送给客户端的一个HTTP响应内容
 * 
 * @author admin
 *
 */
public class HttpResponse {
	/*
	 * 转态行相关信息定义
	 */

	/*
	 * 响应头相关信息定义
	 */

	/*
	 * 响应正文相关信息定义
	 */
	private File entity;// 响应的实体文件

	/*
	 * 定义与连接相关的属性
	 */
	private Socket socket;
	private OutputStream out;

	public HttpResponse(Socket socket) {
		try {
			this.socket=socket;
			 out = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将当前响应对象内容以HTTP响应格式发送给客户端
	 */
	public void flush() {
		/*
		 * 发送状态行，响应头，响应正文
		 */
		sendStatusLine();
		sendHeaders();
		sendContent();
	}

	/**
	 * 发送状态行
	 */
	private void sendStatusLine() {
		// 发送状态行
		try {
			String line = "HTTP/1.1 200 OK";
			out.write(line.getBytes("ISO8859-1"));
			out.write(13);
			out.write(10);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("转态行发送完毕");
	}

	/**
	 * 发送响应头
	 */

	private void sendHeaders() {
		try {
			String line = "Content-Type: text/html";
			out.write(line.getBytes("ISO8859-1"));
			out.write(13);
			out.write(10);

			line = "Content-Length: " + entity.length();
			out.write(line.getBytes("ISO8859-1"));
			out.write(13);
			out.write(10);
			// 单独发送CRLF表示响应头发送完毕
			out.write(13);
			out.write(10);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("响应头发送完毕");
	}

	/**
	 * 发送响应正文
	 */

	private void sendContent() {
		try (FileInputStream fis = new FileInputStream(entity);) {
			int len = -1;
			byte[] data = new byte[1024 * 10];
			while ((len = fis.read(data)) != -1) {
				out.write(data, 0, len);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("响应正文发送完毕");
	}

	public File getEntity() {
		return entity;
	}

	public void setEntity(File entity) {
		this.entity = entity;
	}
}
```

HTTP响应格式也分为三部分:状态行，响应头，响应正文

状态行格式:
protorol status-code status-reason
协议版本 状态码 状态描述

修改ClientHandler创建HTTPResponse对象
![修改ClientHandler](https://img-blog.csdnimg.cn/20190210215851590.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MTkzNTcwMg==,size_16,color_FFFFFF,t_70)
测试四
运行WebServer，发现浏览器成功跳转
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190210220016102.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MTkzNTcwMg==,size_16,color_FFFFFF,t_70)

到这里，就差不多完成50%啦！！！有没有感觉很神奇。

## 状态码与对应的描述

**创建对应的404界面：**
当客户端请求的资源服务端无法找到的时候，应当响应客户端404转态码，以及一个404错误的提示页面

```html
<html>
<head>
<meta charset="utf-8">
<title>404</title>
</head>
<body>
	<h1 align="center">404,资源未找到</h1>
</body>
</html>
```

修改ClientHandler，当用户请求的资源不存在的时候，直接刷404界面

```java
package com.webserver.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Map;

import com.webserver.http.HttpContext;
import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

/**
 * 用户处理客户端请求并予以响应的处理类
 */
public class ClientHandler implements Runnable{
	private Socket socket;
	public ClientHandler(Socket socket){
		this.socket=socket;
	}
	public void run() {
		System.out.println("一个客户端连接了");
		try {
		/**
		 * 处理客户端的请求分三步
		 * 1.解析请求
		 * 2.处理请求
		 * 3.发送响应
		 */
			//解析请求
			HttpRequest request=new HttpRequest(socket);
			//创建响应对象
			HttpResponse response=new HttpResponse(socket);
			//获取请求的抽象路径
			String path=request.getUrl();
			//去webapps目录下面找对应的资源
			File file=new File("webapps"+path);
			//判断资源是否存在
			if(file.exists()){
				System.out.println("资源找到了");
				/*
				 * 发送一个标准的Http响应给客户端
				 */
				//将要响应给客户端的资源设置到响应对象中
				response.setEntity(file);
				
			}else{
				System.out.println("资源未找到");
				//设置转态代码404
				response.setStatusCode(404);
//				response.setStatusReason("Not Found");
				response.setEntity(new File("webapps/root/404.html"));
			}
			//响应该内容给客户端
			response.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			//最后要与客户端断开连接
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	

}
```

**创建HttpContext类，来管辖这些与http协议的相关内容**
1.修改要发送的转态代码，不再是写死的200，这个值要改变成可以设置的
2.不用的转态代码有不同的描述，我们定义一个map来保存转态代码和描述，然后设置转态代码后自动找到对应的描述
3.改进响应中的响应头操作，根据用户客户端请求的不同实际静态资源来响应其类型值

```java
package com.webserver.http;
/**
 * http协议规定的相关内容
 * @author admin
 *
 */

import java.util.HashMap;
import java.util.Map;

public class HttpContext {
	/**
	 * 状态代码与对应的描述
	 * key:状态代码
	 * value:状态代码的描述
	 */
	private static Map<Integer, String> STATUS_MAPPING=new HashMap<Integer,String>();
	/**
	 * 介质类型映射
	 * Key:文件名的后缀
	 * value:Content-Type对应的值
	 */
	private static Map<String, String>  MIME_TYPE_MAPPING=new HashMap<String,String>();
	public static Map<Integer, String> getSTATUS_MAPPING() {
		return STATUS_MAPPING;
	}
	static{
		//初始化
		initStatusMapping();
		initMineTypeMapping();
	}
	/**
	 * 初始化状态代码和对应的描述
	 */
	private static void initStatusMapping() {
		STATUS_MAPPING.put(200, "ok");
		STATUS_MAPPING.put(404, "Not Found");
		STATUS_MAPPING.put(500, "Internal Server Error");
	}
	private static void initMineTypeMapping() {
		MIME_TYPE_MAPPING.put("html", "text/html");
		MIME_TYPE_MAPPING.put("css", "text/css");
		MIME_TYPE_MAPPING.put("png", "image/png");
		MIME_TYPE_MAPPING.put("gif", "image/gif");
		MIME_TYPE_MAPPING.put("jpg", "image/jepg");
		MIME_TYPE_MAPPING.put("js", "application/javascript");
		
	}
	/**
	 * 根据给定的状态代码获得对应的状态描述
	 */
	public static String getStatusReason(int code){
		return STATUS_MAPPING.get(code);
	}
	/**
	 * 根据资源后缀获取相对应的Content-type值
	 * @param ext
	 * @return
	 */
	public static String getMimeType(String ext){
		return MIME_TYPE_MAPPING.get(ext);
	}
	public static void main(String[] args) {
		String fileName="jquery-1.8.3.min.js";
		String ext=fileName.substring(fileName.lastIndexOf(".")+1);
		String line=getMimeType(ext);
		System.out.println(line);
	}
}
```

通过map，我们可以修改sendHeaders方法，改成遍历headers来发送所有的响应头
修改以后的HttpResponse

```java
package com.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * 响应对象 该类的每个实例用于表示发送给客户端的一个HTTP响应内容
 * 
 * @author admin
 *
 */
public class HttpResponse {

	/*
	 * 状态行相关信息定义
	 */
	// 状态代码
	private int StatusCode = 200;
	// 状态描述
	private String statusReason = "OK";

	/*
	 * 响应头相关信息定义
	 */
	private Map<String, String> headers = new HashMap<String, String>();

	/*
	 * 响应正文相关信息定义
	 */
	private File entity;// 响应的实体文件

	/*
	 * 定义与连接相关的属性
	 */
	private Socket socket;
	private OutputStream out;

	public HttpResponse(Socket socket) {
		try {
			this.socket = socket;
			out = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将当前响应对象内容以HTTP响应格式发送给客户端
	 */
	public void flush() {
		/*
		 * 发送状态行，响应头，响应正文
		 */
		sendStatusLine();
		sendHeaders();
		sendContent();
	}

	/**
	 * 发送状态行
	 */
	private void sendStatusLine() {
		// 发送状态行
		try {
			String line = "HTTP/1.1" + " " + StatusCode + " " + statusReason;
			out.write(line.getBytes("ISO8859-1"));
			out.write(13);
			out.write(10);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("转态行发送完毕");
	}

	/**
	 * 发送响应头
	 */

	private void sendHeaders() {
		try {
			//遍历headers将所有的响应头发送给客户端
			Set<Entry<String, String>> header=headers.entrySet();
			for(Entry<String, String> e:header){
				String name=e.getKey();
				String value=e.getValue();
				String line=name+": "+value;
				out.write(line.getBytes("ISO8859-1"));
				out.write(13);
				out.write(10);
			}
			// 单独发送CRLF表示响应头发送完毕
			out.write(13);
			out.write(10);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("响应头发送完毕");
	}

	/**
	 * 发送响应正文
	 */

	private void sendContent() {
		try (FileInputStream fis = new FileInputStream(entity);) {
			int len = -1;
			byte[] data = new byte[1024 * 10];
			while ((len = fis.read(data)) != -1) {
				out.write(data, 0, len);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("响应正文发送完毕");
	}

	public File getEntity() {
		return entity;
	}
/**
 * 设置响应正文的实体文件
 * 设置该文件，则意味着这个响应是包含正文的，而一个响应只要包含正文，一定会包含两个响应头
 * Content-Type和Content-Length，用于告知客户端正文的数据类型以及字节量
 * 
 * @param entity
 */
	public void setEntity(File entity) {
		this.entity = entity;
		//Content-Length
		headers.put("Content-Length", entity.length()+"");
		
		//Content-Type
		//1获取该资源文件的后缀名
		String fileName=entity.getName();
		int index=fileName.lastIndexOf(".")+1;
		String ext=fileName.substring(index);
		//2根据后缀获得对应的Content-Type的值
		String line=HttpContext.getMimeType(ext);
		headers.put("Content-Type", line);
	}

	/**
	 * 设置指定的状态代码，同时会制动设置对应的状态描述
	 * 
	 * @param statusCode
	 */

	public void setStatusCode(int statusCode) {
		StatusCode = statusCode;
		this.statusReason = HttpContext.getStatusReason(statusCode);
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public int getStatusCode() {
		return StatusCode;
	}

	public String getStatusReason() {
		return statusReason;
	}

	/**
	 * 添加指定响应头
	 * 
	 * @param name
	 * @param value
	 */
	public void putHeader(String name, String value) {
		this.headers.put(name, value);
	}

	/**
	 * 获取指定的响应头的值
	 * 
	 * @param name
	 * @return
	 */
	public String getHeader(String name) {
		return this.headers.get(name);
	}
}
```

最后将我们的WebServer进行修改，套上一个while，让服务器一直处于启动的转态，可以不断的接收浏览器发送来的请求
同时每次服务器请求完了以后我们就可以断开socket

```java
package com.webserver.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
	private ServerSocket server;

	public WebServer() {
		// 初始化server
		try {
			server = new ServerSocket(8088);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			while (true) {
				// 启动一个线程处理该客户端交互
				Socket socket = server.accept();
				// 将socket传递给ClientHander
				ClientHandler handler = new ClientHandler(socket);
				Thread t = new Thread(handler);
				t.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		WebServer server = new WebServer();
		System.out.println("服务端开始启动");
		server.start();
	}
}
```

测试五
导入别人写好的界面，然后直接启动WebServer服务器，然后用自己的浏览器访问
在我的资源里面有
