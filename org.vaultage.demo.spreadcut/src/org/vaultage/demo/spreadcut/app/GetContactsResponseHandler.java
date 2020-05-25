/**** protected region GetContactsResponseHandler on begin ****/
package org.vaultage.demo.spreadcut.app;

import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.spreadcut.gen.GetContactsResponseBaseHandler;

public class GetContactsResponseHandler extends GetContactsResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region GetContactsResponseHandler end ****/