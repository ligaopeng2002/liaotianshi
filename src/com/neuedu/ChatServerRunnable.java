package com.neuedu;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ChatServerRunnable implements Runnable {

	private Socket socket;

	private DataOutputStream dos;

	private DataInputStream dis;

	private String currentUserNickName;

	public ChatServerRunnable(Socket socket) throws IOException {
		this.socket = socket;
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.dis = new DataInputStream(socket.getInputStream());
	}

	@Override
	public void run() {
		try {
			write("��ӭ���������ң�");
			login();
			System.out.println(currentUserNickName + "�û���¼�ɹ�");
			write(currentUserNickName + "�� ���ѵ�¼��\n���롾list users�����Բ鿴��ǰ��¼�û��б�\n���롾to all ��Ϣ���ݡ�����Ⱥ����Ϣ\n���롾to ĳ���û� ��Ϣ���ݡ����Ը�ָ���û�������Ϣ\n���롾exit�������˳�����");
			String input = dis.readUTF();
			while (!ChatServer.EXIT.equals(input)) {
				System.out.println(currentUserNickName + "������" + input);
				if (input.startsWith("to ")) {
					sendMessage(input);
				} else if ("list users".equals(input)) {
					showOnlineUsers();
				} else {
					write("�����������Ϸ������������룡");
				}
				input = dis.readUTF();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ChatServer.nickNameSocketMap.remove(currentUserNickName);
			try {
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void login() throws IOException {
		write("����������ǳƣ�");
		while (true) {
			String nickName = dis.readUTF();
			System.out.println("�û��������ǳƣ�" + nickName);
			synchronized (ChatServerRunnable.class) {
				if (!ChatServer.nickNameSocketMap.containsKey(nickName)) {
					currentUserNickName = nickName;
					ChatServer.nickNameSocketMap.put(nickName, socket);
					break;
				} else {
					write("��������ǳ��Ѵ��ڣ����������룺");
				}
			}
		}
	}

	private void sendMessage(String input) throws IOException {
		int receiverEndIndex = input.indexOf(" ", 3);
		String receiver = input.substring(3, receiverEndIndex);
		String message = input.substring(receiverEndIndex + 1);
		if ("all".equals(receiver)) {
			broadcast(message);
		} else {
			sendIndividualMessage(receiver, message);
		}
	}

	private void sendIndividualMessage(String receiver, String orignalMessage) throws IOException {
		Socket receiverSocket = ChatServer.nickNameSocketMap.get(receiver);
		if (receiverSocket != null) {
			SocketUtils.writeToSocket(receiverSocket, formatMessage("��", orignalMessage));
		} else {
			write("��Ҫ����������û���" + receiver + "�������ڻ����Ѿ�����");
		}
	}

	private String formatMessage(String receiver, String originalMessage) {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(currentUserNickName).append(" �� ").append(receiver).append(" ˵��\n")
				.append(originalMessage).append("\n����ʱ�䣺")
				.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		return messageBuilder.toString();
	}

	private void broadcast(String orignalMessage) throws IOException {
		for (Map.Entry<String, Socket> entry : ChatServer.nickNameSocketMap.entrySet()) {
			if (!currentUserNickName.equals(entry.getKey())) {
				SocketUtils.writeToSocket(entry.getValue(), formatMessage("������", orignalMessage));
			}
		}
	}
	
	private void showOnlineUsers() throws IOException {
		StringBuilder users = new StringBuilder();
		users.append("��ǰ���ߵ��û��У�\n");
		for (String nickName : ChatServer.nickNameSocketMap.keySet()) {
			users.append("��").append(nickName).append("��\n");
		}
		write(users.toString());
	}

	private void write(String message) throws IOException {
		SocketUtils.writeToDataOutputStream(dos, message);
	}

}
