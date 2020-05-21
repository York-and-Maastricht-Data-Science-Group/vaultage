package org.vaultage.demo.fairnet.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.vaultage.util.VaultageEncryption;
import org.vaultage.demo.fairnet.gen.Friend;
import org.vaultage.demo.fairnet.gen.Post;
import org.vaultage.demo.fairnet.gen.FairnetVaultBase;

public class FairnetVault extends FairnetVaultBase {
	private String name = new String();
	private List<Friend> friends = new ArrayList<>();
	private List<Post> posts = new ArrayList<>();

	public FairnetVault() throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		KeyPair keyPair = VaultageEncryption.generateKeys();
		publicKey = VaultageEncryption.getPublicKey(keyPair);
		privateKey = VaultageEncryption.getPrivateKey(keyPair);
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
	
	/* protected region createPost on begin */
	public Post createPost(String content, Boolean isPublic) throws Exception {
		Post post = new Post();
		post.setContent(content);
		post.setIsPublic(isPublic);
		this.posts.add(post);
		return post;
	}
    /* protected region createPost end */
    
	
	/* protected region isFriend on begin */
	public Boolean isFriend(String friendPublicKey) throws Exception {
		return friends.stream().anyMatch(f -> f.getPublicKey().equals(friendPublicKey));
	}
    /* protected region isFriend end */
    
	
	/* protected region getPostById on begin */
	public Post getPostById(String postId) throws Exception {
		return this.posts.stream().filter(p -> p.getId().equals(postId)).findFirst().orElse(null);
	}
    /* protected region getPostById end */
    
	@Override
	/* protected region getPost on begin */
	public Post getPost(String friendPublicKey, String postId) throws Exception {
		if (isFriend(friendPublicKey)) {
			return this.getPostById(postId);
		} else {
			return null;
		}
	}
    /* protected region getPost end */
    
	@Override
	/* protected region getPosts on begin */
	public List<String> getPosts(String friendPublicKey) throws Exception {
		if (isFriend(friendPublicKey)) {
			return this.posts.stream().filter(p -> p.getIsPublic()).map(p -> p.getId()).collect(Collectors.toCollection(ArrayList::new));
		}else {
			return new ArrayList<String>();
		}
	}
    /* protected region getPosts end */
    
		
}