package org.rdbd.demo.fairnet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rdbd.demo.fairnet.exception.NotFoundException;
import org.rdbd.demo.fairnet.exception.PermissionDeniedException;
import org.rdbd.demo.fairnet.handler.AddFriendConfirmedHandler;
import org.rdbd.demo.fairnet.util.FairnetUtil;

public class User extends UserBase {

	protected String name;

	private String VAULT_PATH = "files";
	private String STRING_PUBLIC_KEY = "public_key";
	private String STRING_PRIVATE_KEY = "private_key";
	private String STRING_NAME = "name";
	private String STRING_FRIENDS = "friends";
	private String STRING_POSTS = "posts";

	private Map<String, User> friends = new HashMap<>();
	private Map<String, Post> posts = new HashMap<>();

	public User(String id, String privateKey) {
		this.privateKey = privateKey;
		this.publicKey = id;
		File file = new File(VAULT_PATH + File.separator + this.publicKey);
		if (!(file).exists() || !file.isDirectory()) {
			file.mkdir();
		}
		file = new File(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_FRIENDS);
		if (!(file).exists() || !file.isDirectory()) {
			file.mkdir();
		}
		file = new File(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_POSTS);
		if (!(file).exists() || !file.isDirectory()) {
			file.mkdir();
		}

		this.setPublicKey(id);
		this.setPrivateKey(privateKey);

	}

	public User(String id) {
		this(id, null);
	}

	public String getPublicKey() {
		privateKey = FairnetUtil
				.readFile(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_PRIVATE_KEY);
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		FairnetUtil.saveFile(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_PUBLIC_KEY,
				publicKey);
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		privateKey = FairnetUtil
				.readFile(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_PRIVATE_KEY);
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		if (privateKey != null) {
			FairnetUtil.saveFile(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_PRIVATE_KEY,
					privateKey);
		}
		this.privateKey = privateKey;
	}

	public String getName() {
		name = FairnetUtil.readFile(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_NAME);
		return name;
	}

	public void setName(String name) {
		FairnetUtil.saveFile(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_NAME, name);
		this.name = name;
	}

	public Post createPost(String id, String text) {
		Post post = new Post(id, text);
		FairnetUtil.saveFile(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_POSTS
				+ File.separator + post.getId(), post.getBody());
		posts.put(post.getId(), post);
		return post;
	}

	@Override
	public Post createPost(String text, boolean isPublic) {
		Post post = new Post(text, isPublic);
		FairnetUtil.saveFile(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_POSTS
				+ File.separator + post.getId(), post.getBody());
		posts.put(post.getId(), post);
		return post;
	}

	@Override
	public boolean addFriend(User requester) throws Exception {
		FairnetUtil.saveFile(VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_FRIENDS
				+ File.separator + requester.publicKey, requester.getPublicKey());
		friends.put(requester.getPublicKey(), requester);
		return true;
	}

	@Override
	public List<String> getPosts(User requester) throws Exception {
		if (isFriend(requester)) {
			return (List<String>) new ArrayList<String>(posts.keySet());
		} else {
			throw new PermissionDeniedException();
		}
	}

	@Override
	public Post getPost(String id) throws Exception {
		String body = FairnetUtil.readFile(
				VAULT_PATH + File.separator + this.publicKey + File.separator + STRING_POSTS + File.separator + id);
		Post post = new Post(id, body);
		posts.put(post.getId(), post);
		return post;
	}

	@Override
	public Post getPost(User requester, String id) throws Exception {
//		Post post = posts.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);

		Post post = posts.get(id);

		if (isFriend(requester)) {
			if (post == null) {
				throw new NotFoundException();
			} else {
				return post;
			}
		} else {
			if (post != null && post.isPublic())
				return post;
			else
				throw new PermissionDeniedException();
		}
	}

	public boolean isFriend(User other) {
		return true;
	}
}
