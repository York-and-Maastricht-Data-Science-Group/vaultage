package org.eclipse.epsilon.emc.vaultage.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

public class FairnetQueryTest {

	private static FairnetBroker BROKER;
	private static FairnetVault alice;
	private static FairnetVault bob;
	private static FairnetVault charlie;
	private static EolModule module;
	private static VaultageServer brokerServer;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BROKER = new FairnetBroker();
		BROKER.start(FairnetBroker.BROKER_ADDRESS);

		brokerServer = new VaultageServer(FairnetBroker.BROKER_ADDRESS);

		// Alice
		alice = new FairnetVault();
		alice.setId("Alice");
		alice.setName("Alice");
		alice.register(brokerServer);

		for (int i = 1; i <= 9; i++) {
			alice.createPost("Alice Content 0" + i, true).setId("alice-0" + i);
		}
		alice.addOperationResponseHandler(new GetPostsResponder());
		alice.addOperationResponseHandler(new GetPostResponder());
		alice.addOperationResponseHandler(new QueryResponder());

		for (Post post : alice.getPosts()) {
			System.out.println(post.getId() + ": " + post.getContent());
		}

		// Bob
		bob = new FairnetVault();
		bob.setId("Bob");
		bob.setName("Bob");
		bob.register(brokerServer);

		bob.createPost("Bob Content 01", true).setId("bob-01");
		bob.createPost("Bob Content 02", false).setId("bob-02");
		bob.createPost("Bob Content 03", true).setId("bob-03");
		bob.addOperationResponseHandler(new GetPostsResponder());
		bob.addOperationResponseHandler(new GetPostResponder());
		bob.addOperationResponseHandler(new QueryResponder());

		for (Post post : bob.getPosts()) {
			System.out.println(post.getId() + ": " + post.getContent());
		}

		// Charlie
		charlie = new FairnetVault();
		charlie.setId("Charlie");
		charlie.setName("Charlie");
		charlie.register(brokerServer);

		for (int i = 1; i <= 9; i++) {
			charlie.createPost("Charlie Content 0" + i, true).setId("charlie-0" + i);
		}
		charlie.addOperationResponseHandler(new GetPostsResponder());
		charlie.addOperationResponseHandler(new GetPostResponder());
		charlie.addOperationResponseHandler(new QueryResponder());

		for (Post post : charlie.getPosts()) {
			System.out.println(post.getId() + ": " + post.getContent());
		}

		exchangePublicKeys(alice, bob);
		exchangePublicKeys(bob, charlie);
		charlie.getTrustedVaultIds().add(alice.getPublicKey());

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		alice.shutdownServer();
		alice.unregister();
		bob.shutdownServer();
		bob.unregister();
		charlie.shutdownServer();
		charlie.unregister();
		BROKER.stop();
		System.out.println("Finished!");
	}

	@Before
	public void beforeTest() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		module = new VaultageEolModuleParallel(new VaultageEolContextParallel());

		Set<Package> packages = new HashSet<Package>();
		packages.add(bob.getClass().getPackage());
		VaultageModel model = new VaultageModel(bob, packages);
		model.setName("M");
		module.getContext().getModelRepository().addModel(model);
		module.getContext().getOperationContributorRegistry().add(new VaultageOperationContributor());
		((EolModuleParallel) module).getContext().setParallelism(100);
	}

	@Test
	public void testCreateEntity() throws Exception {

		String script = "var friend = new Friend;" + "friend.name = \"Alex\";" + "return friend.name;";
		module.parse(script);
		Object result = module.execute();
		assertEquals("Alex", result);

	}

	@Test
	public void testSelectOne() throws Exception {

		String script = "var bob = FairnetVault.all" + ".selectOne( v | v.name = \"Bob\");" + "return bob.name;";
		module.parse(script);
		Object result = module.execute();
		assertEquals("Bob", result);

	}

	@Test
	public void testQuery() throws Exception {
		module = new VaultageEolModuleParallel(new VaultageEolContextParallel());

		Set<Package> packages = new HashSet<Package>();
		packages.add(alice.getClass().getPackage());
		VaultageModel model = new VaultageModel(alice, packages);
		model.setName("M");
		module.getContext().getModelRepository().addModel(model);
		module.getContext().getOperationContributorRegistry().add(new VaultageOperationContributor());
		((EolModuleParallel) module).getContext().setParallelism(100);

		// ---
		String script = Files.readString(Paths.get("model/Query.eol"));
		module.parse(script);
		Object temp = module.execute();

		System.out.println("\nRetrieved Posts: ");
		System.out.println(temp);
		Collection<?> result = (Collection<?>) temp;
//		result.stream().forEach(item -> {
//			System.out.println(((LinkedTreeMap<?, ?>) item).get("content"));
//		});
		assertEquals(2, result.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPosts() throws Exception {
		String script = Files.readString(Paths.get("model/GetPosts.eol"));
		module.parse(script);

		System.out.println();
		Collection<Post> result = (Collection<Post>) module.execute();
//		Thread.sleep(4000);

		System.out.println("\nRetrieved Posts: ");
		for (Post post : result) {
			System.out.println(post.getContent());
		}
		for (Post post : alice.getPosts().stream().filter(p -> p.getIsPublic()).collect(Collectors.toList())) {
			boolean val = result.stream().anyMatch(p -> p.getContent().equals(post.getContent()));
			assertEquals(true, val);
		}

		for (Post post : charlie.getPosts().stream().filter(p -> p.getIsPublic()).collect(Collectors.toList())) {
			boolean val = result.stream().anyMatch(p -> p.getContent().equals(post.getContent()));
			assertEquals(true, val);
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testIdentifyOnlyRequiredVariables() throws Exception {
		String script = Files.readString(Paths.get("model/OnlyRequiredVariables.eol"));
		module.parse(script);

		System.out.println();
		Collection<Post> result = (Collection<Post>) module.execute();

		System.out.println("\nRetrieved Posts: ");
		for (Post post : result) {
			System.out.println(post.getContent());
		}

		boolean val = result.stream().anyMatch(p -> p.getContent().contains("Alice Content 01"));
		assertEquals(true, val);
		val = result.stream().anyMatch(p -> p.getContent().contains("Bob Content 01"));
		assertEquals(true, val);
		val = result.stream().anyMatch(p -> p.getContent().contains("Charlie Content 01"));
		assertEquals(true, val);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPostsQuery() throws Exception {
		String script = Files.readString(Paths.get("model/GetPostsQuery.eol"));
		module.parse(script);

		System.out.println();
		Collection<Post> result = (Collection<Post>) module.execute();
//		Thread.sleep(4000);

		System.out.println("\nRetrieved Posts: ");
		for (Post post : result) {
			System.out.println(post.getContent());
		}

		boolean val = result.stream().anyMatch(p -> p.getContent().contains("Alice Content 01"));
		assertEquals(true, val);
		val = result.stream().anyMatch(p -> p.getContent().contains("Bob Content 01"));
		assertEquals(true, val);
		val = result.stream().anyMatch(p -> p.getContent().contains("Charlie Content 01"));
		assertEquals(true, val);
	}

	@Test
	public void testPreventCallingOperation() throws Exception {
		String script = Files.readString(Paths.get("model/PreventOperation.eol"));
		module.parse(script);

		EolRuntimeException exception = null;
		try {
			Object result = module.execute();
		} catch (EolRuntimeException e) {
			exception = e;
		}
		assertEquals(true, exception instanceof VaultageEolRuntimeException);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMultilevelQuery() throws Exception {

		module = new VaultageEolModuleParallel(new VaultageEolContextParallel());

		Set<Package> packages = new HashSet<Package>();
		packages.add(alice.getClass().getPackage());
		VaultageModel model = new VaultageModel(alice, packages);
		model.setName("M");
		module.getContext().getModelRepository().addModel(model);
		module.getContext().getOperationContributorRegistry().add(new VaultageOperationContributor());
		((EolModuleParallel) module).getContext().setParallelism(100);

		String script = Files.readString(Paths.get("model/MultilevelQuery.eol"));
		module.parse(script);

		List<Object> result = (List<Object>) module.execute();
		System.out.println("Result: " + result.toString());
		assertEquals(true, result.size() > 0);
		System.console();
	}

	/***
	 * This test send the public key of Alice to Charlie. Charlie receives it and
	 * sends it back to Alice.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPassingVariable() throws Exception {

		module = new VaultageEolModuleParallel(new VaultageEolContextParallel());

		Set<Package> packages = new HashSet<Package>();
		packages.add(alice.getClass().getPackage());
		VaultageModel model = new VaultageModel(alice, packages);
		model.setName("M");
		module.getContext().getModelRepository().addModel(model);
		module.getContext().getOperationContributorRegistry().add(new VaultageOperationContributor());
		((EolModuleParallel) module).getContext().setParallelism(100);

		String script = Files.readString(Paths.get("model/PassingVariable.eol"));
		module.parse(script);

		String result = (String) module.execute();
		System.out.println("Returned origin: " + result.toString());
		assertEquals(alice.getPublicKey(), result);
		System.console();
	}

	/***
	 * This test sends an operation to Bob. Bob returns the return value of the
	 * operation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPassingOperation() throws Exception {

		module = new VaultageEolModuleParallel(new VaultageEolContextParallel());

		Set<Package> packages = new HashSet<Package>();
		packages.add(alice.getClass().getPackage());
		VaultageModel model = new VaultageModel(alice, packages);
		model.setName("M");
		module.getContext().getModelRepository().addModel(model);
		module.getContext().getOperationContributorRegistry().add(new VaultageOperationContributor());
		((EolModuleParallel) module).getContext().setParallelism(100);

		String script = Files.readString(Paths.get("model/PassingOperation.eol"));
		module.parse(script);

		int result = (int) module.execute();
		System.out.println("Bob's number of friends: " + result);
		assertEquals(2, result);
		System.console();
	}

	/***
	 * This is to count all the posts in the network. Alice initiate the chain
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCountAllPosts() throws Exception {

		module = new VaultageEolModuleParallel(new VaultageEolContextParallel());

		Set<Package> packages = new HashSet<Package>();
		packages.add(alice.getClass().getPackage());
		VaultageModel model = new VaultageModel(alice, packages);
		model.setName("M");
		module.getContext().getModelRepository().addModel(model);
		module.getContext().getOperationContributorRegistry().add(new VaultageOperationContributor());
		((EolModuleParallel) module).getContext().setParallelism(100);

		// Dan
		FairnetVault dan = new FairnetVault();
		dan.setId("Dan");
		dan.setName("Dan");
		dan.register(brokerServer);

		for (int i = 1; i <= 9; i++) {
			dan.createPost("Dan Content 0" + i, true).setId("dan-0" + i);
		}
		dan.addOperationResponseHandler(new GetPostsResponder());
		dan.addOperationResponseHandler(new GetPostResponder());
		dan.addOperationResponseHandler(new QueryResponder());

		for (Post post : dan.getPosts()) {
			System.out.println(post.getId() + ": " + post.getContent());
		}

		// Erin
		FairnetVault erin = new FairnetVault();
		erin.setId("Erin");
		erin.setName("Erin");
		erin.register(brokerServer);

		for (int i = 1; i <= 9; i++) {
			erin.createPost("Erin Content 0" + i, true).setId("erin-0" + i);
		}
		erin.addOperationResponseHandler(new GetPostsResponder());
		erin.addOperationResponseHandler(new GetPostResponder());
		erin.addOperationResponseHandler(new QueryResponder());

		for (Post post : erin.getPosts()) {
			System.out.println(post.getId() + ": " + post.getContent());
		}

		exchangePublicKeys(charlie, dan);
		exchangePublicKeys(charlie, erin);
		dan.getTrustedVaultIds().add(alice.getPublicKey());
	
		System.out.println(alice.getPublicKey());

		// load query
		String script = Files.readString(Paths.get("model/CountAllPosts.eol"));
		module.parse(script);

		int result = (int) module.execute();
		System.out.println("Total number of posts: " + result);
		assertEquals(39, result);
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
