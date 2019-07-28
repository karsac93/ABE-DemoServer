package com.mst.demo;

import java.io.Serializable;
import java.util.List;

/**
 * This POJO class is shared between MKG (Master Key Generator) and Edge Server
 * public, private, ownHash and otherHash for that edge server is transferred from
 * MKG to edge server
 * @author ks2ht
 *
 */
public class ServerPOJO implements Serializable{
	
	private static final long serialVersionUID = 956035975591443929L;
	
	final byte[] pub;
	final byte[] prv;
	final List<HashObject> ownHash;
	final List<HashObject> otherHash;
	
	
	public ServerPOJO(byte[] pub, byte[] prv, List<HashObject> ownHash, List<HashObject> otherHash) {
		super();
		this.pub = pub;
		this.prv = prv;
		this.ownHash = ownHash;
		this.otherHash = otherHash;
	}
	
	
	
	

}
