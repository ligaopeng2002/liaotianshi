package com.neuedu;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
	
	public static final String EXIT = "exit";
	
	public static final int PORT = 8888;
	
	static Map<String, Socket> nickNameSocketMap = new HashMap<>();//昵称与客户端对象的对应map集合

	public static void main(String[] args) {
		//try的括号中所有实现Closeable的类声明都可以写在里面，最常见的是流操作，socket操作等。括号中可以写多行语句，会自动关闭括号中的资源
		try (ServerSocket ss = new ServerSocket(PORT)) {
			System.out.println("聊天室服务器端已启动，正在监听" + PORT + "端口");
			while (true) {
				try {
					Socket socket = ss.accept();
					System.out.println("有新用户连接到服务器端，信息为：" + socket);
					new Thread(new ChatServerRunnable(socket)).start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
