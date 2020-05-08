package org.rdbd.demo.fairnet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rdbd.core.RDBD;
import org.rdbd.core.RDBDHandler;
import org.rdbd.demo.fairnet.handler.AddFriendConfirmationHandler;
import org.rdbd.demo.fairnet.handler.AddFriendResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostConfirmationHandler;
import org.rdbd.demo.fairnet.handler.GetPostResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostsConfirmationHandler;
import org.rdbd.demo.fairnet.handler.GetPostsResponseHandler;

public abstract class FairnetVaultBase {

	protected String privateKey;
	protected String publicKey;

	protected boolean isListening;
	protected RDBD rdbd;
	protected Map<String, RDBDHandler> handlers;

	protected AddFriendResponseHandler addFriendResponseHandler;
	protected GetPostsResponseHandler getPostsResponseHandler;
	protected GetPostResponseHandler getPostResponseHandler;
	protected AddFriendConfirmationHandler addFriendConfirmedHandler;
	protected GetPostsConfirmationHandler getPostsConfirmationHandler;
	protected GetPostConfirmationHandler getPostConfirmationHandler;
	
	public FairnetVaultBase() {
		this.isListening = false;
		this.rdbd = new RDBD();
		this.handlers = new HashMap<String, RDBDHandler>();
	}

	public RDBD getRdbd() {
		return rdbd;
	}

	public void setRdbd(RDBD rdbd) {
		this.rdbd = rdbd;
	}

	public AddFriendResponseHandler getAddFriendResponseHandler() {
		return addFriendResponseHandler;
	}

	public void setAddFriendResponseHandler(AddFriendResponseHandler addFriendResponseHandler) {
		this.addFriendResponseHandler = addFriendResponseHandler;
		this.addFriendResponseHandler.setOwner(this);
		handlers.put(AddFriendResponseHandler.class.getName(), addFriendResponseHandler);
	}

	public GetPostsResponseHandler getGetPostsResponseHandler() {
		return getPostsResponseHandler;
	}

	public void setGetPostsResponseHandler(GetPostsResponseHandler getPostsResponseHandler) {
		this.getPostsResponseHandler = getPostsResponseHandler;
		this.getPostsResponseHandler.setOwner(this);
		handlers.put(GetPostsResponseHandler.class.getName(), getPostsResponseHandler);
	}

	public GetPostResponseHandler getGetPostResponseHandler() {
		return getPostResponseHandler;
	}

	public void setGetPostResponseHandler(GetPostResponseHandler getPostResponseHandler) {
		this.getPostResponseHandler = getPostResponseHandler;
		this.getPostResponseHandler.setOwner(this);
		handlers.put(GetPostResponseHandler.class.getName(), getPostResponseHandler);
	}

	public void setAddFriendConfirmationHandler(AddFriendConfirmationHandler addFriendConfirmedHandler) {
		this.addFriendConfirmedHandler = addFriendConfirmedHandler;
		this.addFriendConfirmedHandler.setOwner(this);
		handlers.put(AddFriendConfirmationHandler.class.getName(), addFriendConfirmedHandler);
	}
	
	public void setGetPostsConfirmationHandler(GetPostsConfirmationHandler getPostsConfirmationHandler) {
		this.getPostsConfirmationHandler = getPostsConfirmationHandler;
		this.getPostsConfirmationHandler.setOwner(this);
		handlers.put(GetPostsConfirmationHandler.class.getName(), getPostsConfirmationHandler);
	}
	
	public void setGetPostConfirmationHandler(GetPostConfirmationHandler getPostConfirmationHandler) {
		this.getPostConfirmationHandler = getPostConfirmationHandler;
		this.getPostConfirmationHandler.setOwner(this);
		handlers.put(GetPostConfirmationHandler.class.getName(), getPostConfirmationHandler);
	}
	
	

	public AddFriendConfirmationHandler getAddFriendConfirmedHandler() {
		return addFriendConfirmedHandler;
	}

	public GetPostsConfirmationHandler getGetPostsConfirmationHandler() {
		return getPostsConfirmationHandler;
	}
	
	public GetPostConfirmationHandler getGetPostConfirmationHandler() {
		return getPostConfirmationHandler;
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
	public boolean register(Fairnet fairnet) throws Exception {

		boolean isSuccess = rdbd.connect(fairnet.getAddress());
		if (isSuccess) {
			rdbd.listenMessage(publicKey, privateKey,  handlers);
			return true;
		}

		return false;

	}
	
	public void unregister() throws Exception {
		rdbd.disconnect();
	}

	// These are left abstract for the developer to fill in with
	// behaviour in a subclass (i.e. User.java above)
	public abstract boolean addFriend(Friend requester) throws Exception;

	public abstract List<String> getPosts(Friend requester) throws Exception;

	public abstract Post getPost(Friend requester, String id) throws Exception;

	public abstract Post createPost(String title, String body, boolean isPublic);

	public abstract Post getPost(String id) throws Exception;

	public abstract List<Friend> getFriends() throws Exception;

	public abstract List<String> getPosts() throws Exception;
}
