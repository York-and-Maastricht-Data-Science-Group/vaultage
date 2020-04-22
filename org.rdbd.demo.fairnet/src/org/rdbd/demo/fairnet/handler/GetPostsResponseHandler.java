package org.rdbd.demo.fairnet.handler;

import java.util.List;

import org.rdbd.core.server.RDBDHandler;
import org.rdbd.demo.fairnet.Post;
import org.rdbd.demo.fairnet.User;
import org.rdbd.demo.fairnet.test.RemoteUser;

public class GetPostsResponseHandler extends RDBDHandler {

	public void run(User me, RemoteUser other, List<Post> results) {
		// Runs when user2 has responded with their list of posts to user1's request
	}

}
