package org.eclipse.epsilon.emc.vaultage.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.epsilon.emc.vaultage.VaultageEolContextParallel;
import org.eclipse.epsilon.emc.vaultage.VaultageEolModuleParallel;
import org.eclipse.epsilon.emc.vaultage.VaultageEolRuntimeException;
import org.eclipse.epsilon.emc.vaultage.VaultageModel;
import org.eclipse.epsilon.emc.vaultage.VaultageOperationContributor;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.concurrent.EolModuleParallel;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.FairnetBroker;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;

public class QueryPerformanceTest {

	private static FairnetBroker BROKER;
	private static List<FairnetVault> vaults = new ArrayList<>();
	private static EolModule module;
	private static VaultageServer brokerServer;
	private static final int NUM_OF_VAULTS = 16;
	private static final int NUM_OF_POSTS = 200;
	private static final int CONTENT_SIZE = 1000;
	private static final int ITERATION = 13;
	private static final int LOADING_EFFECT_SKIP = 3;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BROKER = new FairnetBroker();
		BROKER.start(FairnetBroker.BROKER_ADDRESS);

		brokerServer = new VaultageServer(FairnetBroker.BROKER_ADDRESS);

		String content = "";
		for (int i = 1; i <= CONTENT_SIZE; i++) {
			content += "A";
		}

		int vaultNum = 100;
		for (int iv = 0; iv < NUM_OF_VAULTS; iv++) {
			FairnetVault vault = new FairnetVault();
			vaultNum += 1;
			vault.setId("vault-" + vaultNum);
			vault.setName("vault-" + vaultNum);
			vault.getVaultage().setEncrypted(true);
			vault.register(brokerServer);

			int postNum = NUM_OF_POSTS;
			for (int i = 1; i <= NUM_OF_POSTS; i++) {
				postNum += 1;
				Post post = vault.createPost(content, true);
				post.setTitle("Post-" + postNum);
				post.setId(vault.getId() + "-post-" + postNum);
			}
			System.out.println("Posts' size: " + vault.getPosts().size());

			vault.addOperationResponseHandler(new GetPostsResponder());
			vault.addOperationResponseHandler(new GetPostResponder());
			vault.addOperationResponseHandler(new QueryResponder());
			vaults.add(vault);
		}

		int samaCount = 0;
		for (FairnetVault vault1 : vaults) {
			for (FairnetVault vault2 : vaults) {
				if (!vault1.equals(vault2)) {
					exchangePublicKeys(vault1, vault2);
					vault1.getTrustedVaultIds().add(vault2.getPublicKey());
					vault2.getTrustedVaultIds().add(vault1.getPublicKey());
				}
			}
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for (FairnetVault vault : vaults) {
			vault.shutdownServer();
			vault.unregister();
		}
		BROKER.stop();
		System.out.println("Finished!");
	}

	@Before
	public void beforeTest() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		module = new VaultageEolModuleParallel(new VaultageEolContextParallel());
		FairnetVault requester = vaults.get(0);
		Set<Package> packages = new HashSet<Package>();
		packages.add(requester.getClass().getPackage());
		VaultageModel model = new VaultageModel(requester, packages);
		model.setName("M");
		module.getContext().getModelRepository().addModel(model);
		module.getContext().getOperationContributorRegistry().add(new VaultageOperationContributor());
		((EolModuleParallel) module).getContext().setParallelism(100);
	}

	@Test
	public void testCollectAllPosts() throws Exception {

		String script = Files.readString(Paths.get("model/PerformanceTest.eol"));

		Thread.sleep(3000);
		
		List<Long> results = new ArrayList<>();
		for (int i = 1; i <= ITERATION; i++) {
			long startTime = System.currentTimeMillis();
			module.parse(script);
			List<Post> result = (List<Post>) module.execute();
			long endTime = System.currentTimeMillis();

			if (i > LOADING_EFFECT_SKIP) {
				long time = endTime - startTime;
				results.add(time);
				System.out.println("Total Number of Posts: " + result.size());
				System.out.println("Time : " + time + " milliseconds");
//				assertEquals((NUM_OF_VAULTS - 1) * NUM_OF_POSTS, result.size());
				Thread.sleep(3000);
			}
		}

		long total = 0;
		for (int i = 0; i < results.size(); i++) {
			System.out.println("I " + (i + 1) + ": " + results.get(i) + " milliseconds");
			total += results.get(i);
		}
		double average = (total * 1.0) / (results.size() * 1.0);
		System.out.println(String.format("Average %.2f milliseconds", average));
		System.console();
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

		// create remote vaults for both
		user1.getRemoteVaults().put(user2.getPublicKey(), new RemoteFairnetVault(user1, user2.getPublicKey()));
		user2.getRemoteVaults().put(user1.getPublicKey(), new RemoteFairnetVault(user2, user1.getPublicKey()));
		user1.getTrustedVaultIds().add(user2.getPublicKey());
		user2.getTrustedVaultIds().add(user1.getPublicKey());
	}

}
