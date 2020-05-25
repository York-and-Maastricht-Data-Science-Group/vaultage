package org.vaultage.demo.spreadcut.gen;

import java.util.ArrayList;
import java.util.List;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageHandler;
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.spreadcut.app.SpreadCutter;
import org.vaultage.demo.spreadcut.app.GetContactsResponseHandler;

public abstract class GetContactsRequestBaseHandler extends VaultageHandler {

	@Override
	public void run() {
	
		try {
			SpreadCutter me = (SpreadCutter) this.vault;
			
			this.result = run(message);
			
			String value = Vaultage.Gson.toJson(result);
			
			VaultageMessage messageBack = new VaultageMessage();
			messageBack.setFrom(me.getPublicKey());
			messageBack.setTo(this.message.getFrom());
			messageBack.setOperation(GetContactsResponseHandler.class.getName());
			messageBack.putValue("result", value);
			
			me.getVaultage().sendMessage(message.getFrom(), me.getPublicKey(), me.getPrivateKey(), messageBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<ProximityContact> run(VaultageMessage senderMessage) throws Exception {
		return null;
	}
}