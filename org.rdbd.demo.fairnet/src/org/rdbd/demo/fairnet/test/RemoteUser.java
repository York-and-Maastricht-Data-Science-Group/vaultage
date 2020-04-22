package org.rdbd.demo.fairnet.test;

import org.rdbd.demo.fairnet.User;
import org.rdbd.demo.fairnet.handler.AddFriendResponseHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.rdbd.core.server.RDBDMessage;
import org.rdbd.demo.fairnet.Fairnet;

public class RemoteUser {

	protected Fairnet fairnet;
	protected String publicKey;
	protected User user;

	public RemoteUser(Fairnet fairnet, String publicKey, User user) throws Exception {
		this.fairnet = fairnet;
		this.publicKey = publicKey;
		this.user = user;
	}

	public void addFriend(String friendPublicKey) {
		    
		RDBDMessage message = new RDBDMessage();
		message.setFrom("alice[at]publickey.com");
		message.setTo(friendPublicKey);
		message.setOperation(AddFriendResponseHandler.class.getName());
		message.setValue("Hi, Bob! I'm Alice!");
		
		this.user.getRdbd().sendMessage(message.getTo(), message);
		
	}

	public void getPosts() {
		// ... as above
	}

	public void getPost() {
		// ... as above
	}

}