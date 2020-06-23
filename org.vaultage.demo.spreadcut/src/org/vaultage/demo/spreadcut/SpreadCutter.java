
package org.vaultage.demo.spreadcut;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// import org.vaultage.demo.spreadcut.ProximityContact;
// import org.vaultage.demo.spreadcut.SpreadCutterBase;

public class SpreadCutter extends SpreadCutterBase {
	private List<ProximityContact> contacts = new ArrayList<ProximityContact>();
	
	public SpreadCutter() throws Exception {
		super();
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
	
	public List<ProximityContact> getContacts(SpreadCutter requesterSpreadCutter, String timestamp) throws Exception {
		throw new Exception();
	}
	
	
	public boolean confirmContact(SpreadCutter requesterSpreadCutter, String timestamp) throws Exception {
		throw new Exception();
	}
	
	
}