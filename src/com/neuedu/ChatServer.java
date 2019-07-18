package com.neuedu;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
	
	public static final String EXIT = "exit";
	
	public static final int PORT = 8888;
	
	static Map<String, Socket> nickNameSocketMap = new HashMap<>();//�ǳ���ͻ��˶���Ķ�Ӧmap����

	public static void main(String[] args) {
		//try������������ʵ��Closeable��������������д�����棬���������������socket�����ȡ������п���д������䣬���Զ��ر������е���Դ
		try (ServerSocket ss = new ServerSocket(PORT)) {
			System.out.println("�����ҷ������������������ڼ���" + PORT + "�˿�");
			while (true) {
				try {
					Socket socket = ss.accept();
					System.out.println("�����û����ӵ��������ˣ���ϢΪ��" + socket);
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
