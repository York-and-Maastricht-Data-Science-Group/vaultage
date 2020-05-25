package org.vaultage.demo.spreadcut.gen;

import java.util.ArrayList;
import java.util.List;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageHandler;
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.spreadcut.app.SpreadCutter;
import org.vaultage.demo.spreadcut.app.ConfirmContactResponseHandler;

public abstract class ConfirmContactRequestBaseHandler extends VaultageHandler {

	@Override
	public void run() {
	
		try {
			SpreadCutter me = (SpreadCutter) this.vault;
			
			this.result = run(message);
			
			String value = Vaultage.Gson.toJson(result);
			
			VaultageMessage messageBack = new VaultageMessage();
			messageBack.setFrom(me.getPublicKey());
			messageBack.setTo(this.message.getFrom());
			messageBack.setOperation(ConfirmContactResponseHandler.class.getName());
			messageBack.putValue("result", value);
			
			me.getVaultage().sendMessage(message.getFrom(), me.getPublicKey(), me.getPrivateKey(), messageBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Boolean run(VaultageMessage senderMessage) throws Exception {
		return null;
	}
}