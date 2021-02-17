package org.vaultage.demo.spreadcut;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.vaultage.wallet.PaymentInformation;

// import org.vaultage.demo.spreadcut.ProximityContact;
// import org.vaultage.demo.spreadcut.SpreadCutterBase;

public class SpreadCutter extends SpreadCutterBase {
	private List<ProximityContact> contacts = new ArrayList<ProximityContact>();
	
	public SpreadCutter() throws Exception {
		super();
	}
	
	public SpreadCutter(String address, int port) throws Exception {
		super(address, port);
	}
	
	// getter
	public List<ProximityContact> getContacts() {
		return this.contacts;
	}
	
	// setter
	public void setContacts(List<ProximityContact> contacts) {
		this.contacts = contacts;
	}
	
	// operations
	
	public void getContacts(String requesterPublicKey, String requestToken, String timestamp) throws Exception {
		throw new Exception();
	}
	
	
	public void confirmContact(String requesterPublicKey, String requestToken, String timestamp) throws Exception {
		throw new Exception();
	}
	
	
}