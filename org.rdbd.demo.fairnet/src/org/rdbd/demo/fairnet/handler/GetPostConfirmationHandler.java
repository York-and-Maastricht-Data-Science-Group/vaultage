package org.rdbd.demo.fairnet.handler;

import org.rdbd.demo.fairnet.Post;
import org.rdbd.core.RDBDHandler;
import org.rdbd.demo.fairnet.FairnetVault;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetPostConfirmationHandler extends RDBDHandler {
	private Post post;

	@Override
	public void run() {
		System.out.println("Get Post Confirmation");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Operation: " + this.message.getOperation());
		System.out.println("Post:");
		try {
			System.out.println(this.message.getValue());
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			post = gson.fromJson(message.getValue(), Post.class);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Post getPost() {
		return post;
	}

}
