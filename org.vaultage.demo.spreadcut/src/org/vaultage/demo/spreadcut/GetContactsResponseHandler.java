/**** protected region GetContactsResponseHandler on begin ****/
package org.vaultage.demo.spreadcut;

import org.vaultage.core.VaultageMessage;
// import org.vaultage.demo.spreadcut.GetContactsResponseBaseHandler;

public class GetContactsResponseHandler extends GetContactsResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region GetContactsResponseHandler end ****/