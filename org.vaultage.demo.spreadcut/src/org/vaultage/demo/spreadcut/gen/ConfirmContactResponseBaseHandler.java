package org.vaultage.demo.spreadcut.gen;

import java.util.ArrayList;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageHandler;
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.spreadcut.app.SpreadCutter;

public class ConfirmContactResponseBaseHandler extends VaultageHandler {

	@Override
	public void run() {
		try {
			result = Vaultage.Gson.fromJson((String) message.getValue("result"), boolean.class);
			this.result = run(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}