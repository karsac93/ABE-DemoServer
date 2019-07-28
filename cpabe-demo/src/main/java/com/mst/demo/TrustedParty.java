package com.mst.demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.junwei.bswabe.Bswabe;
import co.junwei.bswabe.BswabeMsk;
import co.junwei.bswabe.BswabePrv;
import co.junwei.bswabe.BswabePub;
import co.junwei.bswabe.SerializeUtils;

/**
 * This is the Master Key Generator class (MKG)
 * @author ks2ht
 *
 */
public class TrustedParty {
	
	private  int PORT;
	BswabePub pub;
	BswabeMsk msk;
	BswabePrv prv;
	ServerSocket serverSocket;
	private final static int bitLenght = 256;
	int count = 0;
	List<ArrayList<HashObject>> hashesForServer = new ArrayList<ArrayList<HashObject>>();
	
	public TrustedParty() {
		
	}
	
	
	/**
	 * This generates Master, public and overall private keys
	 * @param attr - All the attributes associated with all the edge servers
	 * @param port - Port number in which MKG runs
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public TrustedParty(String[] attr, int port) throws NoSuchAlgorithmException, IOException {
		this.PORT = port;
		pub = new BswabePub();
		msk = new BswabeMsk();
		Bswabe.setup(pub, msk);
		System.out.println(SerializeUtils.serializeBswabeMsk(msk).length);
		prv = Bswabe.keygen(pub, msk, attr);
		serverSocket = new ServerSocket(PORT);
		for(int i=0; i < 2; i++) {
			hashesForServer.add((ArrayList<HashObject>) generateKeyChain());
		}
	}
	
	/**
	 * Runs the MKG, waits for edge servers to connect
	 * receives the attributes of edge server and generated public key for it
	 * transfers key chains and all the keys
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalArgumentException
	 */
	public void runServer() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, IllegalArgumentException {
		
		Socket socket = serverSocket.accept();
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		String[] ed_attrs = (String[]) in.readObject();
		BswabePrv ed_prv = Bswabe.delegate(pub, prv, ed_attrs);
		
		byte[] pub_byte = SerializeUtils.serializeBswabePub(pub);
		byte[] prv_byte = SerializeUtils.serializeBswabePrv(ed_prv);
		List<HashObject> ownHash = new ArrayList<HashObject>();
		List<HashObject> otherHash = new ArrayList<HashObject>();
		
		for(int i=0; i<2; i++) {
			if(count != i) otherHash = hashesForServer.get(i);
			else ownHash = hashesForServer.get(i);
		}
		
		ServerPOJO serverPOJO = new ServerPOJO(pub_byte, prv_byte, ownHash, otherHash);
		count += 1;
		
		System.err.println(pub_byte.length + " " + prv_byte.length);
		
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(serverPOJO);
		out.flush();
		
		in.close();
		out.close();
		socket.close();
		runServer();
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	private String shaHash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashInBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hashInBytes) {
				sb.append(String.format("%02x", b));
			}
			//System.out.println("Hash length:" + hashInBytes.length);
			return sb.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	/**
	 * Generates a random string from which key chains are generated
	 * @return
	 */
	private String generateKey() {
		String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		int RANDOM_STRING_LENGTH = bitLenght / 8;

		StringBuffer randStr = new StringBuffer();
		for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
			int randomInt = 0;
			Random randomGenerator = new Random();
			randomInt = randomGenerator.nextInt(CHAR_LIST.length());
			char ch = CHAR_LIST.charAt(randomInt);
			randStr.append(ch);
		}

		return randStr.toString();
	}
	
	/**
	 * Generates keychains
	 * @return
	 */
	public List<HashObject> generateKeyChain() {
		List<HashObject> hashes = new ArrayList<HashObject>();
		HashObject hash = null;
		for (int i = 0; i < 5; i++) {
			String input = generateKey();
			String firstHash = shaHash(input);
			String lastHash = firstHash;
			for (int j = 1; j < 5; j++) {
				lastHash = shaHash(lastHash);
			}
			hash = new HashObject(firstHash, lastHash);
			hashes.add(hash);
		}

		return hashes;
	}
	
	/**
	 * Just for testing
	 * @param args
	 */
	public static void main(String[] args) {
		TrustedParty ttp = new TrustedParty();
		String in = "5303657f7e80ad65bfaa11feb0749a244d29cda5f0d239ef595ea44971008c33";
		for(int i=1; i<5; i++) {
			in = ttp.shaHash(in);
			System.out.println(in);
		}
	}
	
}
