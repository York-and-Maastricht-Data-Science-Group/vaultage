
package org.vaultage.demo.fairnet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.vaultage.core.BytesToOutputTypeConverter;
import org.vaultage.core.StreamReceiver;
import org.vaultage.core.Vault;

public class FairnetVault extends FairnetVaultBase {
	private String name = new String();
	private List<Friend> friends = new ArrayList<Friend>();
	private List<Post> posts = new ArrayList<Post>();

	public FairnetVault() throws Exception {
		super();
	}

	public FairnetVault(String address, int port) throws Exception {
		super(address, port);
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
	public void setPort(int port) {
		this.getVaultage().setPort(port);
	}

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
	public void addFriend(String requesterPublicKey, String requestToken, String friendName) throws Exception {
		Friend friend = new Friend();
		friend.setPublicKey(requesterPublicKey);
		friends.add(friend);
		(new RemoteFairnetVault(this, requesterPublicKey)).respondToAddFriend(true, requestToken);
	}

	@Override
	public void getPost(String requesterPublicKey, String requestToken, String postId) throws Exception {
		if (isFriend(requesterPublicKey)) {
			Post post = this.getPostById(postId);
			(new RemoteFairnetVault(this, requesterPublicKey)).respondToGetPost(post, requestToken);
		}

	}

	@Override
	public void getPosts(String requesterPublicKey, String requestToken) throws Exception {
		List<String> posts;
		if (isFriend(requesterPublicKey)) {
			posts = this.posts.stream().filter(p -> p.getIsPublic()).map(p -> p.getId())
					.collect(Collectors.toCollection(ArrayList::new));
			(new RemoteFairnetVault(this, requesterPublicKey)).respondToGetPosts(posts, requestToken);
		}

	}

	@Override
	public void getFile(String requesterPublicKey, String requestToken, InetSocketAddress receiverSocketAddress,
			String fileId) throws Exception {
	
		File file = null;
		if ("data.txt".equals(fileId)) {
			file = new File("resource" + File.separator + "data.txt");
		}

		RemoteFairnetVault remoteRequester = new RemoteFairnetVault(this, requesterPublicKey, receiverSocketAddress);
		byte[] dataInputStream = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		
		remoteRequester.respondToGetFile(dataInputStream, requestToken);
	}

	public void downloadFile(FairnetVault peer, String fileId) throws Exception {

		// setup localIpAddress and port to receive stream
		String receiverAddress = InetAddress.getLoopbackAddress().getHostAddress();
		int receiverPort = 54322;

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		RemoteFairnetVault remotePeer = new RemoteFairnetVault(this, peer.getPublicKey());

		BytesToOutputTypeConverter bytesToFile = new BytesToOutputTypeConverter(File.class) {
			@Override
			public Object customConvert(byte[] bytes) {
				if (this.outputType.equals(File.class)) {
					File file = null;
					try {
						file = new File(Vault.DEFAULT_DOWNLOAD_DIR + File.separator + fileId);
						if (!file.getParentFile().exists()) {
							file.getParentFile().mkdir();
						}
						if (file.exists()) {
							file.delete();
						}
						Files.write(file.toPath(), bytes, StandardOpenOption.CREATE_NEW);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return file;
				} else {
					return null;
				}
			}
		};

		StreamReceiver streamReceiver = remotePeer.getFile(outputStream, receiverAddress, receiverPort, bytesToFile,
				fileId);
		synchronized (streamReceiver) {
			streamReceiver.wait();
		}

		System.out.println("Finished!");
	}

	
}