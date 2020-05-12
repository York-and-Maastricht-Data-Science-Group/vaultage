package org.vaultage.demo.fairnet.handler;

import java.util.List;

import org.vaultage.core.VaultAgeHandler;
import org.vaultage.core.VaultAgeMessage;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetPostsResponseHandler extends VaultAgeHandler {

	@Override
	public void run() {
		System.out.println("Get Posts Request");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Operation: " + this.message.getOperation());

		try {
			FairnetVault me = (FairnetVault) this.owner;
			Friend myFriend = new Friend("", message.getFrom());
			List<String> posts = me.getPosts(myFriend);
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String value = gson.toJson(posts);
			 
			VaultAgeMessage messageBack = new VaultAgeMessage();
			messageBack.setFrom(me.getPublicKey());
			messageBack.setTo(this.message.getFrom());
			messageBack.setOperation(GetPostsConfirmationHandler.class.getName());
			messageBack.setValue(value);
			
			me.getRdbd().sendMessage(myFriend.getPublicKey(), me.getPublicKey(), me.getPrivateKey(), messageBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
