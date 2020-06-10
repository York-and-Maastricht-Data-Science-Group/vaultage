package org.vaultage.demo.spreadcut;

import java.util.List;
import org.vaultage.core.VaultageMessage;
// import org.vaultage.demo.spreadcut.GetContactsResponseBaseHandler;

public class GetContactsResponseHandler extends GetContactsResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage, List<ProximityContact> result) throws Exception {
		return result;
	}
}