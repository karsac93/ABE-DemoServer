package com.mst.demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import co.junwei.cpabe.Cpabe;

public class EdgeServer {

	ServerPOJO pojo;
	ServerSocket serverSocket;
	String devicePrefix;
	int count = 1;
	String[] attr;
	List<HashObject> ownHashes;
	List<HashObject> receivedHashes;

	boolean flag = false;

	public EdgeServer(String[] attr, int TTPport, int edPort, String devicePrefix)
			throws IOException, ClassNotFoundException {
		InetAddress host = InetAddress.getLocalHost();
		Socket socket = new Socket(host.getHostName(), TTPport);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(attr);
		out.flush();

		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		pojo = (ServerPOJO) in.readObject();
		System.err.println(pojo.pub.length + " " + pojo.prv.length);
		System.out.println(pojo.ownHash.size() + " " + pojo.otherHash.size());
		out.close();
		in.close();
		socket.close();

		this.attr = attr;
		this.devicePrefix = devicePrefix;
		serverSocket = new ServerSocket(edPort);
	}

	public void runServer() throws IOException, ClassNotFoundException {
		Socket socket = serverSocket.accept();
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		String deviceId = devicePrefix + count;
		String serverType = getServerType(devicePrefix);

		System.out.println("Android device connected to " + serverType + " Edge Server");
		System.out.println("Public & private key with unique device ID " + deviceId + " is sent to the device");
		System.out.println("------------------------------------------");

		MobileServerPOJO mobileServerPOJO = new MobileServerPOJO(pojo.pub, pojo.prv, deviceId, attr, pojo.ownHash, pojo.otherHash);
		out.writeObject(mobileServerPOJO);
		out.flush();
		out.close();

		count += 1;
		runServer();
	}

	private String getServerType(String prefix) {
		// TODO Auto-generated method stub
		if (prefix.contains(Main.AR_PREFIX))
			return "Battlefield";
		else
			return "Disaster";
	}

	public byte[] encrypt(String infile) throws Exception {
		Cpabe cpabe = new Cpabe();
		String policy = "army disaster flooding 2of3";
		byte[] enc_byte = cpabe.encModified(pojo.pub, policy, infile);
		return enc_byte;
	}

	public byte[] decrypt(byte[] enc_byte, String decfile) throws Exception {
		Cpabe cpabe = new Cpabe();
		byte[] plt = cpabe.decModified(pojo.pub, pojo.prv, enc_byte);
		return plt;
	}

}
