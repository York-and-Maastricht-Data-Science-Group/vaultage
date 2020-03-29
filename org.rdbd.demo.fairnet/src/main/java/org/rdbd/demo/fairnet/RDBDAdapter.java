package org.rdbd.demo.fairnet;

import org.rdbd.core.server.RDBDServer;
import org.rdbd.demo.fairnet.account.Account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RDBDAdapter {

	public static void createAccount(Account account) throws JsonProcessingException {
		
		ObjectMapper mapper = new ObjectMapper();

//		// Java object to JSON string, default compact-print
//		String jsonString = mapper.writeValueAsString(new Staff());
		
		// pretty-print
		String text = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(account);

		RDBDServer.sendMessage(account.getUsername(), text);
	}
}
