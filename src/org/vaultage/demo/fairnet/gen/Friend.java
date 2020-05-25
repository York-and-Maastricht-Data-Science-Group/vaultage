package org.vaultage.demo.fairnet.gen;

import java.util.List;
import java.util.UUID;

public class Friend  {
	private String id = UUID.randomUUID().toString();
	private String name;
	private String publicKey;

	// getter
	public String getId(){
		return this.id;
	}
	public String getName() {
		return this.name;
	}
	public String getPublicKey() {
		return this.publicKey;
	}

	// setter
	public void setId(String id){
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	// operations
	
	
			
}