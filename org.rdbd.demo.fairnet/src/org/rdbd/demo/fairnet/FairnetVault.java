package org.rdbd.demo.fairnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.rdbd.demo.fairnet.exception.NotFoundException;
import org.rdbd.demo.fairnet.exception.PermissionDeniedException;
import org.rdbd.demo.fairnet.util.FairnetUtil;
import org.rdbd.util.RDBDEncryptionUtil;

public class FairnetVault extends FairnetVaultBase {

	protected String id;
	protected String name;

	private String HOME = System.getProperty("user.home");
	private String STRING_ID = "id";
	private String VAULT_PATH = "fairnet";
	private String STRING_PUBLIC_KEY = "public_key";
	private String STRING_PRIVATE_KEY = "private_key";
	private String STRING_NAME = "name";
	private String STRING_FRIENDS = "friends";
	private String STRING_POSTS = "posts";

	private Map<String, Friend> friends = new HashMap<>();
	private Map<String, Post> posts = new HashMap<>();

	public FairnetVault(String id) throws NoSuchAlgorithmException, FileNotFoundException, IOException {

		Properties appProps = new Properties();
		appProps.load(new FileInputStream("fairnet.conf"));
		String vaultPath = appProps.getProperty("vault");

		this.id = id;

		if (Files.exists(Paths.get(vaultPath))) {
			VAULT_PATH = vaultPath;
		} else {
			VAULT_PATH = HOME + File.separator + VAULT_PATH;
		}

		File file = new File(VAULT_PATH + File.separator + this.id);
		if (!(file).exists() || !file.isDirectory()) {
			file.mkdir();
		}
		file = new File(VAULT_PATH + File.separator + this.id + File.separator + STRING_FRIENDS);
		if (!(file).exists() || !file.isDirectory()) {
			file.mkdir();
		}
		file = new File(VAULT_PATH + File.separator + this.id + File.separator + STRING_POSTS);
		if (!(file).exists() || !file.isDirectory()) {
			file.mkdir();
		}

		this.setId(id);

		KeyPair keyPair = RDBDEncryptionUtil.generateKeys();
		if ((Files.exists(Paths.get(VAULT_PATH + File.separator + this.id + File.separator + STRING_PRIVATE_KEY))
				&& (Files.exists(
						Paths.get(VAULT_PATH + File.separator + this.id + File.separator + STRING_PUBLIC_KEY))))) {
			publicKey = this.getPublicKey();
			privateKey = this.getPrivateKey();
		} else {
			if ((Files
					.exists(Paths.get(VAULT_PATH + File.separator + this.id + File.separator + STRING_PRIVATE_KEY)))) {
				privateKey = this.getPrivateKey();
			} else {
				this.setPrivateKey(RDBDEncryptionUtil.getPrivateKey(keyPair));
			}

			if ((Files.exists(Paths.get(VAULT_PATH + File.separator + this.id + File.separator + STRING_PUBLIC_KEY)))) {
				publicKey = this.getPublicKey();
			} else {
				this.setPublicKey(RDBDEncryptionUtil.getPublicKey(keyPair));
			}
		}

	}

	public String getId() {
		id = FairnetUtil.readFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_ID);
		return id;
	}

	public void setId(String id) {
		FairnetUtil.saveFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_ID, id);
		this.id = id;
	}

	public String getPublicKey() {
		publicKey = FairnetUtil.readFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_PUBLIC_KEY);
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		FairnetUtil.saveFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_PUBLIC_KEY, publicKey);
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		privateKey = FairnetUtil.readFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_PRIVATE_KEY);
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		if (privateKey != null) {
			FairnetUtil.saveFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_PRIVATE_KEY,
					privateKey);
		}
		this.privateKey = privateKey;
	}

	public String getName() {
		name = FairnetUtil.readFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_NAME);
		return name;
	}

	public void setName(String name) {
		FairnetUtil.saveFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_NAME, name);
		this.name = name;
	}

	public Post createPost(String title, String text) {
		Post post = new Post(title, text);
		FairnetUtil.saveFile(
				VAULT_PATH + File.separator + this.id + File.separator + STRING_POSTS + File.separator + post.getId(),
				post.getBody());
		posts.put(post.getId(), post);
		return post;
	}

	@Override
	public Post createPost(String title, String text, boolean isPublic) {
		Post post = new Post(title, text, isPublic);
		FairnetUtil.saveFile(
				VAULT_PATH + File.separator + this.id + File.separator + STRING_POSTS + File.separator + post.getId(),
				post.getBody());
		posts.put(post.getId(), post);
		return post;
	}

	@Override
	public boolean addFriend(Friend requester) throws Exception {
//		System.out.println(VAULT_PATH + File.separator + this.id + File.separator + STRING_FRIENDS + File.separator
//				+ requester.getId());
		FairnetUtil.saveFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_FRIENDS + File.separator
				+ requester.getId(), requester.getPublicKey());
		friends.put(requester.getPublicKey(), requester);
		return true;
	}

	@Override
	public List<String> getPosts() throws Exception {
		return (List<String>) new ArrayList<String>(posts.keySet());
	}

	@Override
	public List<String> getPosts(Friend requester) throws Exception {
		if (isFriend(requester.getPublicKey())) {
			return (List<String>) new ArrayList<String>(posts.keySet());
		} else {
			throw new PermissionDeniedException();
		}
	}

	@Override
	public List<Friend> getFriends() throws Exception {
		List<Friend> friends = new ArrayList<>();
		List<String> friendIds = FairnetUtil.getFiles(VAULT_PATH + File.separator + this.id + File.separator + STRING_FRIENDS);
		for (String friendId : friendIds) {
			String publicKey = FairnetUtil.readFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_FRIENDS + File.separator + friendId);
			friends.add(new Friend(friendId, publicKey));
		}
		return friends;
	}

	@Override
	public Post getPost(String id) throws Exception {
		String body = FairnetUtil
				.readFile(VAULT_PATH + File.separator + this.id + File.separator + STRING_POSTS + File.separator + id);
		Post post = new Post(id, "", body);	
		posts.put(post.getId(), post);
		return post;
	}

	@Override
	public Post getPost(Friend requester, String id) throws Exception {
//		Post post = posts.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);

		Post post = posts.get(id);

		if (isFriend(requester.getPublicKey())) {
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

	public boolean isFriend(String friendPublicKey) throws Exception {
		return this.getFriends().stream().anyMatch(f -> f.getPublicKey().equals(friendPublicKey));
	}

}
