
package org.vaultage.demo.fairnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// import org.vaultage.demo.fairnet.Friend;
// import org.vaultage.demo.fairnet.Post;
// import org.vaultage.demo.fairnet.FairnetVaultBase;

public class FairnetVault extends FairnetVaultBase {
	private String name = new String();
	private List<Friend> friends = new ArrayList<Friend>();
	private List<Post> posts = new ArrayList<Post>();

	public FairnetVault() throws Exception {
		super();
	}

	// getter
	public String getName() {
		return this.name;
	}

	public List<Friend> getFriends() {
		return this.friends;
	}

	public List<Post> getPosts() {
		return this.posts;
	}

	// setter
	public void setName(String name) {
		this.name = name;
	}

	public void setFriends(List<Friend> friends) {
		this.friends = friends;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	// operations
	public Post createPost(String content, Boolean isPublic) throws Exception {
		Post post = new Post();
		post.setContent(content);
		post.setIsPublic(isPublic);
		this.posts.add(post);
		return post;
	}

	public Boolean isFriend(String friendPublicKey) throws Exception {
		return friends.stream().anyMatch(f -> f.getPublicKey().equals(friendPublicKey));
	}

	public Post getPostById(String postId) throws Exception {
		return this.posts.stream().filter(p -> p.getId().equals(postId)).findFirst().orElse(null);
	}

	@Override
	public void addFriend(FairnetVault requesterFairnetVault, String requestToken) throws Exception {
		Friend friend = new Friend();
		friend.setPublicKey(requesterFairnetVault.getPublicKey());
		friends.add(friend);
		(new RemoteFairnetVault(this, requesterFairnetVault.getPublicKey())).respondToAddFriend(true, requestToken);
	}

	@Override
	public void getPost(FairnetVault requesterFairnetVault, String requestToken, String postId) throws Exception {
		if (isFriend(requesterFairnetVault.getPublicKey())) {
			Post post = this.getPostById(postId);
			(new RemoteFairnetVault(this, requesterFairnetVault.getPublicKey())).respondToGetPost(post, requestToken);
		}
	}

	@Override
	public void getPosts(FairnetVault requesterFairnetVault, String requestToken) throws Exception {
		List<String> posts = null;
		if (isFriend(requesterFairnetVault.getPublicKey())) {
			posts = this.posts.stream().filter(p -> p.getIsPublic()).map(p -> p.getId())
					.collect(Collectors.toCollection(ArrayList::new));
			(new RemoteFairnetVault(this, requesterFairnetVault.getPublicKey())).respondToGetPosts(posts, requestToken);
		}
	}

}