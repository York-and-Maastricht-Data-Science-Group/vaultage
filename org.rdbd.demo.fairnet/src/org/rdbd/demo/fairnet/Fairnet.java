package org.rdbd.demo.fairnet;

import javax.jms.Connection;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.rdbd.core.server.RDBD;

public class Fairnet {

	private String address;


	public Fairnet(String address) {
		this.address = address;		
	}
	


	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
