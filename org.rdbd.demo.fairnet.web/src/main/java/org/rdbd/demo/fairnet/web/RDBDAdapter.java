package org.rdbd.demo.fairnet.web;

import org.rdbd.core.RDBD;
import org.rdbd.demo.fairnet.web.account.Account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RDBDAdapter {

	public static void createAccount(Account account) throws JsonProcessingException {
		
		ObjectMapper mapper = new ObjectMapper();

//		// Java object to JSON string, default compact-print
//		String jsonString = mapper.writeValueAsString(new Staff());
		
		// pretty-print
		String text = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(account);

		RDBD.sendMessage(account.getUsername(), text);
	}
}
