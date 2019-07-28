package com.mst.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

public class Main {

	private static final int TTPport = 5000;
	private static final int ARPORT = 5001;
	private static final int DSPORT = 5002;

	public static final String AR_PREFIX = "AR_";
	public static final String DS_PREFIX = "DS_";

	private static final String INFILE = "C:\\Users\\home\\Desktop\\test.png";
	private static final String DECFILE = "C:\\Users\\home\\Desktop\\dtest.png";

	public static void main(String[] args) throws Exception {
		final String[] attr = { "army", "soldier", "disaster", "flooding" };
		System.out.println("Central Authority Master key Generator initialized and running");
		System.out.println("Public and Master key is generated based on the keys:");
		System.out.println(Arrays.toString(attr));
		System.out.println("Master Key Generator runs on PORT:" + TTPport);
		System.out.println("------------------------------------------");
		Thread ttpThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					TrustedParty ttp = new TrustedParty(attr, TTPport);
					ttp.runServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		ttpThread.start();
		Thread.sleep(1000);

		final String[] ar_attr = { "army", "soldier" };
		final String[] ds_attr = { "disaster", "flooding" };

		Thread arThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final EdgeServer arServer = new EdgeServer(ar_attr, TTPport, ARPORT, AR_PREFIX);
					arServer.runServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Thread dsThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					final EdgeServer dsServer = new EdgeServer(ds_attr, TTPport, DSPORT, DS_PREFIX);
					dsServer.runServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

//		EdgeServer arServer = new EdgeServer(ar_attr, TTPport, ARPORT, AR_PREFIX);
//		EdgeServer dsServer = new EdgeServer(ds_attr, TTPport, DSPORT, DS_PREFIX);
//		byte[] cipher = arServer.encrypt(INFILE);
//		byte[] plt = dsServer.decrypt(cipher, DECFILE);
//		File file = new File(DECFILE);
//		FileOutputStream out = new FileOutputStream(file);
//		out.write(plt);
//		out.flush();
//		out.close();

		System.out.println("Battlefield Edge Server access Master Key generator to obtain public key and "
				+ "receive private key from following subset of attrs:");
		System.out.println(Arrays.toString(ar_attr));
		System.out.println("Army Edge Server runs on PORT: " + ARPORT);
		System.out.println("------------------------------------------");
		arThread.start();

		System.out.println("Disaster Edge Server access Master Key generator to obtain public key and "
				+ "receive private key from following subset of attrs:");
		System.out.println(Arrays.toString(ds_attr));
		System.out.println("Army Edge Server runs on PORT: " + DSPORT);
		System.out.println("------------------------------------------");
		dsThread.start();
	}

}
