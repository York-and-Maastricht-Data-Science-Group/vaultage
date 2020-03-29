package org.rdbd.core.client.windows;

import java.util.ArrayList;
import java.util.List;

import org.rdbd.core.client.RDBDAction;
import org.rdbd.core.client.RDBDClient;

public class RDBDWindows {
	
	public static void main (String[] args) {
		String userId = "foo";
		
		List<RDBDAction> actions = new ArrayList<RDBDAction>();
		actions.add(new RDBDCreateAccountFileAction());
		
		RDBDClient.listenMessage(userId, actions);  
	}
}