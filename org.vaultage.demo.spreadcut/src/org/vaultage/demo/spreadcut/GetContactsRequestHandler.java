package org.vaultage.demo.spreadcut;

// import org.vaultage.demo.spreadcut.*;
import java.util.List;
import org.vaultage.core.VaultageMessage;

public class GetContactsRequestHandler extends GetContactsRequestBaseHandler {

	@Override
	public List<ProximityContact> run(VaultageMessage message, String timestamp) throws Exception {	
		return (List<ProximityContact>) result;
		
	}
}