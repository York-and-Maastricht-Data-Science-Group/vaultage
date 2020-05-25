/**** protected region SpreadCutter on begin ****/
package org.vaultage.demo.spreadcut.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.vaultage.util.VaultageEncryption;
import org.vaultage.demo.spreadcut.gen.ProximityContact;
import org.vaultage.demo.spreadcut.gen.SpreadCutterBase;

public class SpreadCutter extends SpreadCutterBase {
	private List<ProximityContact> contacts = new ArrayList<>();
	
	public SpreadCutter() throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		KeyPair keyPair = VaultageEncryption.generateKeys();
		publicKey = VaultageEncryption.getPublicKey(keyPair);
		privateKey = VaultageEncryption.getPrivateKey(keyPair);
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
	
	
	public Boolean confirmContact(String requesterPublicKey, String timestamp) throws Exception {
		throw new Exception();
	}
	
	
}
/**** protected region SpreadCutter end ****/