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
	String devicePrefix; //Army server has AR as prefix, Disaster Server as DS as prefix
	int count = 1; //Keeps track of number of devices connected, so that unique can be assigned based on it
	String[] attr; // Attributes related to this ES, user input from Main.class
	List<HashObject> ownHashes; 
	List<HashObject> receivedHashes;
	
	/**
	 * 
	 * @param attr - Attributes associated with this edge server
	 * @param TTPport - MKG port number
	 * @param edPort - Port on which this Edge Server (ES) will be running
	 * @param devicePrefix - For each ES, unique prefix for DTN nodes which connect to them
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public EdgeServer(String[] attr, int TTPport, int edPort, String devicePrefix)
			throws IOException, ClassNotFoundException {
		
		// Pass on the attrs to MKG
		InetAddress host = InetAddress.getLocalHost();
		Socket socket = new Socket(host.getHostName(), TTPport);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(attr);
		out.flush();
		
		//Receive public/private keys and keychains from MKG
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		pojo = (ServerPOJO) in.readObject();
		System.err.println(pojo.pub.length + " " + pojo.prv.length);
		System.out.println(pojo.ownHash.size() + " " + pojo.otherHash.size());
		out.close();
		in.close();
		socket.close();

		this.attr = attr;
		this.devicePrefix = devicePrefix;
		
		// The edge server is initialized
		serverSocket = new ServerSocket(edPort);
	}
	
	/**
	 * On calling this method, the edge server starts running
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void runServer() throws IOException, ClassNotFoundException {
		Socket socket = serverSocket.accept();
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		String deviceId = devicePrefix + count;
		String serverType = getServerType(devicePrefix);

		System.out.println("Android device connected to " + serverType + " Edge Server");
		System.out.println("Public & private key with unique device ID " + deviceId + " is sent to the device");
		System.out.println("------------------------------------------");
		
		// POJO class for sending public, private, device ID, attrs, ownhash and otherhash are sent to mobile nodes
		MobileServerPOJO mobileServerPOJO = new MobileServerPOJO(pojo.pub, pojo.prv, deviceId, attr, pojo.ownHash, pojo.otherHash);
		out.writeObject(mobileServerPOJO);
		out.flush();
		out.close();

		count += 1;
		runServer();
	}
	
	
	/**
	 * Based on the prefix this gives the type of server
	 * @param prefix - AR or DS value
	 * @return
	 */
	private String getServerType(String prefix) {
		// TODO Auto-generated method stub
		if (prefix.contains(Main.AR_PREFIX))
			return "Battlefield";
		else
			return "Disaster";
	}
	
	
	/**
	 * This is just for testing
	 * @param infile
	 * @return
	 * @throws Exception
	 */
	public byte[] encrypt(String infile) throws Exception {
		Cpabe cpabe = new Cpabe();
		String policy = "army disaster flooding 2of3";
		byte[] enc_byte = cpabe.encModified(pojo.pub, policy, infile);
		return enc_byte;
	}
	
	/**
	 * This is just for testing
	 * @param enc_byte
	 * @param decfile
	 * @return
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] enc_byte, String decfile) throws Exception {
		Cpabe cpabe = new Cpabe();
		byte[] plt = cpabe.decModified(pojo.pub, pojo.prv, enc_byte);
		return plt;
	}
}
