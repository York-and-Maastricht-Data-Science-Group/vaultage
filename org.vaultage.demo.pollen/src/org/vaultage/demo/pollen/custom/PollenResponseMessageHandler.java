package org.vaultage.demo.pollen.custom;

import org.vaultage.core.ResponseMessageHandler;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.pollen.RemoteUser;
import org.vaultage.demo.pollen.User;

public class PollenResponseMessageHandler extends ResponseMessageHandler {

	@Override
	public void process(VaultageMessage message, String senderPublicKey, Object vault) throws Exception {
		User user = (User) vault;
		RemoteUser sender = new RemoteUser(user, senderPublicKey);

		String operation = message.getOperation();

		switch (operation) {
		case "sendNumberPoll":
			double result = Vaultage.deserialise(message.getValue("result"), Double.class);
			user.getSendNumberPollResponseHandler().run(user, sender, result);
		}
	}

}
