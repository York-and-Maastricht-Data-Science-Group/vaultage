package org.vaultage.demo.spreadcut;

import java.util.List;
import org.vaultage.core.VaultageMessage;
// import org.vaultage.demo.spreadcut.ConfirmContactResponseBaseHandler;

public class ConfirmContactResponseHandler extends ConfirmContactResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage, boolean result) throws Exception {
		return result;
	}
}