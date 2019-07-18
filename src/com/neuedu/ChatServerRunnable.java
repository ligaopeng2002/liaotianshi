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
			write("欢迎来到聊天室！");
			login();
			System.out.println(currentUserNickName + "用户登录成功");
			write(currentUserNickName + "， 您已登录。\n输入【list users】可以查看当前登录用户列表\n输入【to all 消息内容】可以群发消息\n输入【to 某个用户 消息内容】可以给指定用户发送消息\n输入【exit】可以退出聊天");
			String input = dis.readUTF();
			while (!ChatServer.EXIT.equals(input)) {
				System.out.println(currentUserNickName + "输入了" + input);
				if (input.startsWith("to ")) {
					sendMessage(input);
				} else if ("list users".equals(input)) {
					showOnlineUsers();
				} else {
					write("您输入的命令不合法，请重新输入！");
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
		write("请输入你的昵称：");
		while (true) {
			String nickName = dis.readUTF();
			System.out.println("用户输入了昵称：" + nickName);
			synchronized (ChatServerRunnable.class) {
				if (!ChatServer.nickNameSocketMap.containsKey(nickName)) {
					currentUserNickName = nickName;
					ChatServer.nickNameSocketMap.put(nickName, socket);
					break;
				} else {
					write("您输入的昵称已存在，请重新输入：");
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
			SocketUtils.writeToSocket(receiverSocket, formatMessage("你", orignalMessage));
		} else {
			write("您要单独聊天的用户【" + receiver + "】不存在或者已经下线");
		}
	}

	private String formatMessage(String receiver, String originalMessage) {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(currentUserNickName).append(" 对 ").append(receiver).append(" 说：\n")
				.append(originalMessage).append("\n发送时间：")
				.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		return messageBuilder.toString();
	}

	private void broadcast(String orignalMessage) throws IOException {
		for (Map.Entry<String, Socket> entry : ChatServer.nickNameSocketMap.entrySet()) {
			if (!currentUserNickName.equals(entry.getKey())) {
				SocketUtils.writeToSocket(entry.getValue(), formatMessage("所有人", orignalMessage));
			}
		}
	}
	
	private void showOnlineUsers() throws IOException {
		StringBuilder users = new StringBuilder();
		users.append("当前在线的用户有：\n");
		for (String nickName : ChatServer.nickNameSocketMap.keySet()) {
			users.append("【").append(nickName).append("】\n");
		}
		write(users.toString());
	}

	private void write(String message) throws IOException {
		SocketUtils.writeToDataOutputStream(dos, message);
	}

}
