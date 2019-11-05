package core;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {

            //测试读取客户端发送来的一行字符串
            InputStream in = socket.getInputStream();
            int d = -1;//记录每次读取到的字节
            /*
             * c1表示上次读取到的字符，
             * c2表示本次读取到的字符
             *
             *
             */
            char c1 = 'a', c2 = 'a';
            StringBuilder builder = new StringBuilder();
            while ((d = in.read()) != -1) {
                c2 = (char) d;
                //是否连续读取到了CR,LF
                if (c1 == 13 && c2 == 10) {
                    break;
                }
                builder.append(c2);
                c1 = c2;
            }
            String line = builder.toString().trim();
            System.out.println(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}