package org.vaultage.demo.spreadcut.gen;

import java.util.List;
import java.util.UUID;

public class ProximityContact  {
	private String id = UUID.randomUUID().toString();
	private String publicKey;
	private String timestamp;
	
	// getter
	public String getId(){
		return this.id;
	}
	public String getPublicKey() {
		return this.publicKey;
	}
	public String getTimestamp() {
		return this.timestamp;
	}
	
	// setter
	public void setId(String id){
		this.id = id;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	// operations
	
	
	
}