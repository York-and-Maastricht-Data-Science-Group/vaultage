/**** protected region GetContactsRequestHandler on begin ****/
package org.vaultage.demo.spreadcut.app;

import org.vaultage.demo.spreadcut.gen.*;
import java.util.List;
import org.vaultage.core.VaultageMessage;

public class GetContactsRequestHandler extends GetContactsRequestBaseHandler {

	@Override
	public List<ProximityContact> run(VaultageMessage senderMessage) throws Exception {
		return (List<ProximityContact>) result;
	}
}
/**** protected region GetContactsRequestHandler end ****/