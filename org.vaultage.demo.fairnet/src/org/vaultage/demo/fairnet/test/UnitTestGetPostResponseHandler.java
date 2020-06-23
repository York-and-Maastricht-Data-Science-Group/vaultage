package org.vaultage.demo.fairnet.test;

import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.GetPostResponseHandler;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;

public class UnitTestGetPostResponseHandler implements GetPostResponseHandler {

	private Post post;

	

	@Override
	public void run(FairnetVault me, RemoteFairnetVault other, String responseToken, Post result) throws Exception {
		this.post = result;
		System.out.println("Post with id " + result.getId() + " has been received");
		
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public Post getResult() {
		return post;
	}

	
}
