/**** protected region User on begin ****/
package org.vaultage.demo.pollen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class User extends UserBase {
	private String name = new String();
	private Map<String, NumberPoll> polls = new HashMap<>();
	
	public User() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		super();
	}
	
	// getter
	public String getName() {
		return this.name;
	}
	
	// setter
	public void setName(String name) {
		this.name = name;
	}
	
	// operations
	
	public double sendNumberPoll(String requesterPublicKey, NumberPoll poll) throws Exception {
		throw new Exception();
	}
	
	
	public List<Integer> sendMultivaluedPoll(String requesterPublicKey, MultivaluedPoll poll) throws Exception {
		throw new Exception();
	}

	public Map<String, NumberPoll> getPolls() {
		return polls;
	}

	public void setPolls(Map<String, NumberPoll> polls) {
		this.polls = polls;
	}
	
	
}
/**** protected region User end ****/