package org.vaultage.demo.fairnet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaultage.core.VaultAge;
import org.vaultage.core.VaultAgeHandler;
import org.vaultage.core.VaultAgeServer;
import org.vaultage.demo.fairnet.handler.AddFriendResponseHandler;
import org.vaultage.demo.fairnet.handler.AddFriendRequestHandler;
import org.vaultage.demo.fairnet.handler.GetPostResponseHandler;
import org.vaultage.demo.fairnet.handler.GetPostRequestHandler;
import org.vaultage.demo.fairnet.handler.GetPostsResponseHandler;
import org.vaultage.demo.fairnet.handler.GetPostsRequestHandler;

public abstract class FairnetVaultBase {

	protected String privateKey;
	protected String publicKey;

	protected boolean isListening;
	protected VaultAge vaultage;
	protected Map<String, VaultAgeHandler> handlers;

	protected AddFriendRequestHandler addFriendRequestHandler;
	protected AddFriendResponseHandler addFriendResponseHandler;
	protected GetPostsRequestHandler getPostsRequestHandler;
	protected GetPostsResponseHandler getPostsResponseHandler;
	protected GetPostRequestHandler getPostRequestHandler;
	protected GetPostResponseHandler getPostResponseHandler;

	public FairnetVaultBase() {
		this.isListening = false;
		this.vaultage = new VaultAge();
		this.handlers = new HashMap<String, VaultAgeHandler>();
	}

	public VaultAge getVaultAge() {
		return vaultage;
	}

	public void setVaultAge(VaultAge vaultage) {
		this.vaultage = vaultage;
	}

	//
	public AddFriendRequestHandler getAddFriendRequestHandler() {
		return addFriendRequestHandler;
	}

	public void setAddFriendRequestHandler(AddFriendRequestHandler addFriendRequestHandler) {
		this.addFriendRequestHandler = addFriendRequestHandler;
		this.addFriendRequestHandler.setOwner(this);
		handlers.put(AddFriendRequestHandler.class.getName(), addFriendRequestHandler);
	}

	// 
	public GetPostsRequestHandler getGetPostsRequestHandler() {
		return getPostsRequestHandler;
	}
	
	public void setGetPostsRequestHandler(GetPostsRequestHandler getPostsRequestHandler) {
		this.getPostsRequestHandler = getPostsRequestHandler;
		this.getPostsRequestHandler.setOwner(this);
		handlers.put(GetPostsRequestHandler.class.getName(), getPostsRequestHandler);
	}

	// 
	public void setGetPostRequestHandler(GetPostRequestHandler getPostRequestHandler) {
		this.getPostRequestHandler = getPostRequestHandler;
		this.getPostRequestHandler.setOwner(this);
		handlers.put(GetPostRequestHandler.class.getName(), getPostRequestHandler);
	}
	public GetPostRequestHandler getGetPostRequestHandler() {
		return getPostRequestHandler;
	}


	//
	public void setAddFriendResponseHandler(AddFriendResponseHandler addFriendResponseHandler) {
		this.addFriendResponseHandler = addFriendResponseHandler;
		this.addFriendResponseHandler.setOwner(this);
		handlers.put(AddFriendResponseHandler.class.getName(), addFriendResponseHandler);
	}
	
	public AddFriendResponseHandler getAddFriendResponseHandler() {
		return addFriendResponseHandler;
	}

	//
	public void setGetPostsResponsenHandler(GetPostsResponseHandler getPostsResponseHandler) {
		this.getPostsResponseHandler = getPostsResponseHandler;
		this.getPostsResponseHandler.setOwner(this);
		handlers.put(GetPostsResponseHandler.class.getName(), getPostsResponseHandler);
	}
	
	public GetPostsResponseHandler getGetPostsResponseHandler() {
		return getPostsResponseHandler;
	}
	
	//
	public void setGetPostResponseHandler(GetPostResponseHandler getPostResponseHandler) {
		this.getPostResponseHandler = getPostResponseHandler;
		this.getPostResponseHandler.setOwner(this);
		handlers.put(GetPostResponseHandler.class.getName(), getPostResponseHandler);
	}

	public GetPostResponseHandler getGetPostResponseHandler() {
		return getPostResponseHandler;
	}

	/***
	 * This method makes the user listens to a message broker. When a request e.g.
	 * addFriend message is received, call the addFriend method, get its result,
	 * encrypt it using the public key of the requester and send a response message
	 * to the message broker. When a response message is received, decrypt it using
	 * the user's own private key and call the respective response handler (e.g.
	 * addFriendResponseHandler).
	 * 
	 * @param fairnet
	 * @return
	 * @throws Exception
	 */
	public boolean register(VaultAgeServer fairnet) throws Exception {

		boolean isSuccess = vaultage.connect(fairnet.getAddress());
		if (isSuccess) {
			vaultage.listenMessage(publicKey, privateKey, handlers);
			return true;
		}

		return false;

	}

	public void unregister() throws Exception {
		vaultage.disconnect();
	}

	// These are left abstract for the developer to fill in with
	// behaviour in a subclass (i.e. User.java above)
	public abstract boolean addFriend(Friend requester) throws Exception;

	public abstract List<String> getPosts(Friend requester) throws Exception;

	public abstract Post getPost(Friend requester, String id) throws Exception;

	public abstract Post createPost(String title, String body, boolean isPublic) throws Exception;

	public abstract Post getPost(String id) throws Exception;

	public abstract List<Friend> getFriends() throws Exception;

	public abstract List<String> getPosts() throws Exception;
}
