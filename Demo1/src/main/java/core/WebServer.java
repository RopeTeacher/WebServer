package core;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
/*
WebServer服务器是模拟Tomcat的一个Web容器
 */
    private ServerSocket serverSocket;
    /**
     * 初始化服务器
     */
    public WebServer(){
        try{
            System.out.println("客户端正在启动。。。");
            serverSocket = new ServerSocket(8082);
            System.out.println("客户端启动完成！");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     *服务端工作的方法
     */
    public void start(){
        try{
            System.out.println("等待客户端连接。。。");
            Socket socket = serverSocket.accept();
            System.out.println("一个客户端连接了！");

            InputStream in = socket.getInputStream();
            int d = -1;
            while ((d=in.read())!=-1){
                System.out.print((char)d);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.start();
    }
}
