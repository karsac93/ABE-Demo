package com.mst.demo;

import java.io.Serializable;

public class HashObject  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1739358668033838114L;
	final String k5;
	final String k0;
	
	public HashObject(String k5, String k0) {
		super();
		this.k5 = k5;
		this.k0 = k0;
	}

	public String getK5() {
		return k5;
	}

	public String getK0() {
		return k0;
	}
	
	
	
	
}
