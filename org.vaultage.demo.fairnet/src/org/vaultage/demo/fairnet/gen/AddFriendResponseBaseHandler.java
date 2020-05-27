package org.vaultage.demo.fairnet.gen;

import java.util.ArrayList;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageHandler;
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.app.FairnetVault;

public class AddFriendResponseBaseHandler extends VaultageHandler {

	@Override
	public void run() {
		try {
			result = Vaultage.Gson.fromJson((String) message.getValue("result"), java.lang.Boolean.class);
			this.result = run(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}