/**** protected region SpreadCutter on begin ****/
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
	
	public SpreadCutter() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
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
	
	public List<ProximityContact> getContacts(String requesterPublicKey, String timestamp) throws Exception {
		throw new Exception();
	}
	
	
	public boolean confirmContact(String requesterPublicKey, String timestamp) throws Exception {
		throw new Exception();
	}
	
	
}
/**** protected region SpreadCutter end ****/