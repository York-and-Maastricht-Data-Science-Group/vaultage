package org.vaultage.demo.monetisation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.vaultage.wallet.PaymentInformation;

// import org.vaultage.demo.monetisation.Questionnaire;
// import org.vaultage.demo.monetisation.Content;
// import org.vaultage.demo.monetisation.ContentCreatorBase;

public class ContentCreator extends ContentCreatorBase {

	public ContentCreator() throws Exception {
		super();
	}
	
	public ContentCreator(String address, int port) throws Exception {
		super(address, port);
	}
	
	// getter
	
	// setter
	
	// operations
	
	public void getContents(String requesterPublicKey, String requestToken) throws Exception {
		throw new Exception();
	}
	
	
	public void getMonetisedContent(String requesterPublicKey, String requestToken, String contentId, PaymentInformation paymentInformation) throws Exception {
		throw new Exception();
	}
	
	
}