/**** protected region ConfirmContactResponseHandler on begin ****/
package org.vaultage.demo.spreadcut;

import org.vaultage.core.VaultageMessage;
// import org.vaultage.demo.spreadcut.ConfirmContactResponseBaseHandler;

public class ConfirmContactResponseHandler extends ConfirmContactResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region ConfirmContactResponseHandler end ****/