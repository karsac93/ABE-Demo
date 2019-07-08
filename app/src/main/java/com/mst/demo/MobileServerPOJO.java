package com.mst.demo;

import java.io.Serializable;
import java.util.List;

public class MobileServerPOJO implements Serializable{
	
	private static final long serialVersionUID = -4593526765417959655L;
	
	final byte[] pub;
	final byte[] prv;
	final String deviceID;
	final String[] attr;
	final List<HashObject> ownHash;
	final List<HashObject> otherHash;
	
	public MobileServerPOJO(byte[] pub, byte[] prv, String deviceID, String[] attr, List<HashObject> ownHash,
			List<HashObject> otherHash) {
		super();
		this.pub = pub;
		this.prv = prv;
		this.deviceID = deviceID;
		this.attr = attr;
		this.ownHash = ownHash;
		this.otherHash = otherHash;
	}

	public byte[] getPub() {
		return pub;
	}

	public byte[] getPrv() {
		return prv;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public String[] getAttr() {
		return attr;
	}

	public List<HashObject> getOwnHash() {
		return ownHash;
	}

	public List<HashObject> getOtherHash() {
		return otherHash;
	}
	
	
	
}
