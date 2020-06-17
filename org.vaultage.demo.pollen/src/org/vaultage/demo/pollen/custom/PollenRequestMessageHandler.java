package org.vaultage.demo.pollen.custom;

import org.vaultage.core.RequestMessageHandler;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageMessage.MessageType;
import org.vaultage.demo.pollen.NumberPoll;
import org.vaultage.demo.pollen.User;

public class PollenRequestMessageHandler extends RequestMessageHandler {

	@Override
	public void process(VaultageMessage message, String senderPublicKey, Object vault) throws Exception {
		User user = (User) vault;

		String operation = message.getOperation();

		User requester = new User();
		requester.setPublicKey(senderPublicKey);

		switch (operation) {
		case "sendNumberPoll":
			System.out.println("Received sendNumberPoll requestz");
			NumberPoll poll = Vaultage.deserialise(message.getValue("poll"), NumberPoll.class);
			double result = user.sendNumberPoll(requester, poll);

			// sending the response back
			// will be handled by 
			//   1st - a PollenResponseMessageHandler to determine the operation handler to use
			//   2nd - the specific handler (SendNumberPollResponseHandler in this case)
			VaultageMessage response = new VaultageMessage();
			response.setSenderId(user.getId());
			response.setFrom(user.getPublicKey());
			response.setTo(senderPublicKey);
			response.setOperation("sendNumberPoll");
			response.setMessageType(MessageType.RESPONSE);

			response.putValue("result", Vaultage.serialise(result));

			user.getVaultage().sendMessage(response.getTo(), user.getPublicKey(),
					user.getPrivateKey(), response);
		}
	}

}
