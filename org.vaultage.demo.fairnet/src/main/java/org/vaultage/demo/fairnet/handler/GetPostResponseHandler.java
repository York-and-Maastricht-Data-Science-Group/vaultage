package org.vaultage.demo.fairnet.handler;

import org.vaultage.core.RDBDHandler;
import org.vaultage.core.RDBDMessage;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;
import org.vaultage.demo.fairnet.Post;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetPostResponseHandler extends RDBDHandler {

	
	@Override
	public void run() {
		System.out.println("Get Post Request");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Operation: " + this.message.getOperation());

		try {
			FairnetVault me = (FairnetVault) this.owner;
			Friend myFriend = new Friend("", message.getFrom());
			Post post = me.getPost(message.getValue());
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String value = gson.toJson(post);
			 
			RDBDMessage messageBack = new RDBDMessage();
			messageBack.setFrom(me.getPublicKey());
			messageBack.setTo(this.message.getFrom());
			messageBack.setOperation(GetPostConfirmationHandler.class.getName());
			messageBack.setValue(value);
			
			me.getRdbd().sendMessage(myFriend.getPublicKey(), me.getPublicKey(), me.getPrivateKey(), messageBack);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
