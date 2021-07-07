package org.eclipse.epsilon.emc.vaultage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.epsilon.eol.EolModule;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.FairnetBroker;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;
import org.vaultage.demo.fairnet.Post;

class App {

	public static FairnetBroker BROKER;

	public static void main(String[] args) throws Exception {

		BROKER = new FairnetBroker();
		BROKER.start(FairnetBroker.BROKER_ADDRESS);

		VaultageServer brokerServer = new VaultageServer(FairnetBroker.BROKER_ADDRESS);
		
		FairnetVault alice = new FairnetVault();
		alice.setId("Alice");
		alice.setName("Alice");
		alice.register(brokerServer);

		alice.createPost("Alice Content 01", true);
		alice.createPost("Alice Content 02", false);
		alice.createPost("Alice Content 03", true);
		alice.addOperationResponseHandler(new GetPostsResponder());
		alice.addOperationResponseHandler(new GetPostResponder());
		
		for (Post post : alice.getPosts()) {
			System.out.println(post.getId() + ": " +post.getContent());
		}
		
		FairnetVault bob = new FairnetVault();
		bob.setId("Bob");
		bob.setName("Bob");
		bob.register(brokerServer);

		bob.createPost("Bob Content 01", true);
		bob.createPost("Bob Content 02", true);
		bob.addOperationResponseHandler(new GetPostsResponder());
		bob.addOperationResponseHandler(new GetPostResponder());

		for (Post post : bob.getPosts()) {
			System.out.println(post.getId() + ": " +post.getContent());
		}
		
		FairnetVault charlie = new FairnetVault();
		charlie.setId("Charlie");
		charlie.setName("Charlie");
		charlie.register(brokerServer);

		charlie.createPost("Charlie Content 01", true);
		charlie.addOperationResponseHandler(new GetPostsResponder());
		charlie.addOperationResponseHandler(new GetPostResponder());

		for (Post post : charlie.getPosts()) {
			System.out.println(post.getId() + ": " +post.getContent());
		}
		
		exchangePublicKeys(alice, bob);
		exchangePublicKeys(charlie, bob);
				
		EolModule module = new EolModule();

		Set<Package> packages = new HashSet<Package>();
		packages.add(bob.getClass().getPackage());
		VaultageModel model = new VaultageModel(bob, packages);
		model.setName("M");
		module.getContext().getModelRepository().addModel(model);
		module.getContext().getOperationContributorRegistry().add(new VaultageOperationContributor());

		try {
			String script = Files.readString(Paths.get("model/AppScript.eol"));
			module.parse(script);
			module.execute();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		alice.shutdownServer();
		alice.unregister();
		bob.shutdownServer();
		bob.unregister();
		charlie.shutdownServer();
		charlie.unregister();
		BROKER.stop();
	}
	
	private static void exchangePublicKeys(FairnetVault user1, FairnetVault user2) {
		// add user 2 as a friend to user 1's vault
		Friend user1friend = new Friend();
		user1friend.setName(user2.getName());
		user1friend.setPublicKey(user2.getPublicKey());
		user1.getFriends().add(user1friend);

		// add user 2 as a friend to user 1's vault
		Friend user2friend = new Friend();
		user2friend.setName(user1.getName());
		user2friend.setPublicKey(user1.getPublicKey());
		user2.getFriends().add(user2friend);
	}

}
