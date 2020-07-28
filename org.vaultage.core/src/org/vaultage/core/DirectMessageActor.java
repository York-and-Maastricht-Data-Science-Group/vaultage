package org.vaultage.core;

public abstract class  DirectMessageActor {

	private String address = "localhost";
	private String port = "";

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public abstract void start();
	public abstract void stop();
}
