package org.vaultage.demo.fairnet.gen;

import java.util.ArrayList;
import java.util.List;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageHandler;
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.app.FairnetVault;
import org.vaultage.demo.fairnet.app.GetPostsResponseHandler;

public abstract class GetPostsRequestBaseHandler extends VaultageHandler {

	@Override
	public void run() {
	
		try {
			FairnetVault me = (FairnetVault) this.vault;
			
			this.result = run(message);
			
			String value = Vaultage.Gson.toJson(result);
			
			VaultageMessage messageBack = new VaultageMessage();
			messageBack.setFrom(me.getPublicKey());
			messageBack.setTo(this.message.getFrom());
			messageBack.setOperation(GetPostsResponseHandler.class.getName());
			messageBack.putValue("result", value);
			
			me.getVaultage().sendMessage(message.getFrom(), me.getPublicKey(), me.getPrivateKey(), messageBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<String> run(VaultageMessage senderMessage) throws Exception {
	
		return (List<String>) result;
	}
}