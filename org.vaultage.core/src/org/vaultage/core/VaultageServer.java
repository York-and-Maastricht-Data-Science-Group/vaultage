package org.vaultage.core;

public class VaultageServer {

	private String address;


	public VaultageServer(String address) {
		this.address = address;		
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
