package org.rdbd.demo.fairnet.handler;

import java.util.List;

import org.rdbd.core.RDBDHandler;
import org.rdbd.core.RDBDMessage;
import org.rdbd.demo.fairnet.FairnetVault;
import org.rdbd.demo.fairnet.Friend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetPostsResponseHandler extends RDBDHandler {

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
			 
			RDBDMessage messageBack = new RDBDMessage();
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
