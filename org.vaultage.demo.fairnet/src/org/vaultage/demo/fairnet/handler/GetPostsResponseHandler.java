package org.vaultage.demo.fairnet.handler;

import java.util.ArrayList;
import java.util.List;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageHandler;
import org.vaultage.demo.fairnet.FairnetVault;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetPostsResponseHandler extends VaultageHandler {
	private List<String> postIds;

	@Override
	public void run() {
		System.out.println("Get Posts Confirmation");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Operation: " + this.message.getOperation());
		System.out.println("Friends:"); 
		try {
			postIds = Vaultage.Gson.fromJson(message.getValue("value"), ArrayList.class);
			for(String postId:postIds) {
				System.out.println(((FairnetVault)this.owner).getName() +": "+ postId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> getPostIds() {
		return postIds;
	}
}
