package org.rdbd.demo.fairnet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rdbd.core.server.RDBD;
import org.rdbd.core.server.RDBDHandler;
import org.rdbd.demo.fairnet.handler.AddFriendConfirmationHandler;
import org.rdbd.demo.fairnet.handler.AddFriendResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostsConfirmationHandler;
import org.rdbd.demo.fairnet.handler.GetPostsResponseHandler;

public abstract class UserBase {

	protected String privateKey;
	protected String publicKey;

	protected AddFriendResponseHandler addFriendResponseHandler;
	protected GetPostsResponseHandler getPostsResponseHandler;
	protected GetPostResponseHandler getPostResponseHandler;
	protected AddFriendConfirmationHandler addFriendConfirmedHandler;
	protected boolean isListening;
	protected RDBD rdbd;
	protected Map<String, RDBDHandler> handlers;

	public UserBase() {
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
			rdbd.listenMessage(publicKey, handlers);
			return true;
		}

		return false;

	}
	
	public void unregister() throws Exception {
		rdbd.disconnect();
	}

	// These are left abstract for the developer to fill in with
	// behaviour in a subclass (i.e. User.java above)
	public abstract boolean addFriend(User requester) throws Exception;

	public abstract List<String> getPosts(User requester) throws Exception;

	public abstract Post getPost(User requester, String id) throws Exception;

	public abstract Post createPost(String text, boolean isPublic);

	public abstract Post getPost(String id) throws Exception;

	public abstract List<String> getFriends() throws Exception;

	public abstract List<String> getPosts() throws Exception;

	public abstract void setGetPostsConfirmationHandler(GetPostsConfirmationHandler getPostsConfirmationHandler) throws Exception;
	
}
