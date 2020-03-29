package org.rdbd.core.client.windows;

import org.rdbd.core.client.RDBDAction;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RDBDCreateAccountFileAction implements RDBDAction {

	public void execute(String message) {
		
		System.out.println(message);
	}

}
