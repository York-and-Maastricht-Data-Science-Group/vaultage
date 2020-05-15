package org.vaultage.core;

public class VaultAgeServer {

	private String address;


	public VaultAgeServer(String address) {
		this.address = address;		
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
