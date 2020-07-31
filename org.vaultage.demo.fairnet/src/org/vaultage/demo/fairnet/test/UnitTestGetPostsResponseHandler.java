package org.vaultage.demo.fairnet.test;

import java.util.List;

import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.GetPostsResponseHandler;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;

public class UnitTestGetPostsResponseHandler implements GetPostsResponseHandler {

	private List<String> posts;

	
	
	@Override
	public void run(FairnetVault me, RemoteFairnetVault other, String responseToken, List<String> result)
			throws Exception {
		this.posts = result;
		System.out.println("All post ids from " + other.getRemotePublicKey() + " have been received");
		
		synchronized (this) {
			this.notify();
		}
	}

	public List<String> getResult() {
		return posts;
	}


}
