package org.vaultage.demo.fairnet.test;

import org.vaultage.core.Vault;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.GetPostResponseHandler;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;

public class UnitTestGetPostResponseHandler extends GetPostResponseHandler {

	private Post post;

	

	@Override
	public void run(FairnetVault me, RemoteFairnetVault other, String responseToken, Post result) throws Exception {
		this.post = result;
//		System.out.println("Post with id " + result.getId() + " has been received");
		
		synchronized (this) {
			this.notify();
		}
	}

	public Post getResult() {
		return post;
	}

	@Override
	public void run(Vault localVault, RemoteFairnetVault remoteVault, String responseToken, Post result)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	
}
