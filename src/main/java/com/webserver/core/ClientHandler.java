package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        System.out.println("一个客户端连接了！");
        try {
            InputStream in = socket.getInputStream();
            String line = readLine(in);
            System.out.println("line:"+line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String readLine(InputStream in) throws IOException{
        StringBuilder builder = new StringBuilder();
        int cur = -1;//本次读的字符串
        int pre = -1;//上次读的字符串
        while ((cur=in.read())!=-1){
            if (cur==10&&pre==13){
                break;
            }
            builder.append((char)cur);
            pre  = cur;
        }
        return builder.toString().trim();
    }

}
