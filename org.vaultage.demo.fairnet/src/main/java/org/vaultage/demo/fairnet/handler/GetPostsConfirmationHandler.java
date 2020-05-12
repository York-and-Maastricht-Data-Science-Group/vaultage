package org.vaultage.demo.fairnet.handler;

import java.util.ArrayList;
import java.util.List;

import org.vaultage.core.VaultAgeHandler;
import org.vaultage.demo.fairnet.FairnetVault;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetPostsConfirmationHandler extends VaultAgeHandler {
	private List<String> postIds;

	@Override
	public void run() {
		System.out.println("Get Posts Confirmation");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Operation: " + this.message.getOperation());
		System.out.println("Friends:"); 
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			postIds = gson.fromJson(message.getValue(), ArrayList.class);
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
