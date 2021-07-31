package org.eclipse.epsilon.emc.vaultage.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.emc.vaultage.VaultageEolContextParallel;
import org.eclipse.epsilon.emc.vaultage.VaultageModel;
import org.eclipse.epsilon.emc.vaultage.VaultageOperationContributor;
import org.eclipse.epsilon.emc.vaultage.VaultagePropertyCallExpression;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.concurrent.EolModuleParallel;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.FairnetBroker;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;
import org.vaultage.demo.fairnet.Post;

import com.google.gson.internal.LinkedTreeMap;

public class FairnetQueryTest {

	private static FairnetBroker BROKER;
	private static FairnetVault alice;
	private static FairnetVault bob;
	private static FairnetVault charlie;
	private static EolModule module;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BROKER = new FairnetBroker();
		BROKER.start(FairnetBroker.BROKER_ADDRESS);

		VaultageServer brokerServer = new VaultageServer(FairnetBroker.BROKER_ADDRESS);

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
		exchangePublicKeys(charlie, bob);

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

		module = new EolModuleParallel(new VaultageEolContextParallel()) {
//		module = new EolModule() {
			public ModuleElement adapt(org.eclipse.epsilon.common.parse.AST cst, ModuleElement parentAst) {
				ModuleElement element = super.adapt(cst, parentAst);
				if (element instanceof PropertyCallExpression) {
					element = new VaultagePropertyCallExpression();
				}
				return element;
			};
		};

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

		module = new EolModuleParallel(new VaultageEolContextParallel()) {
//			module = new EolModule() {
			public ModuleElement adapt(org.eclipse.epsilon.common.parse.AST cst, ModuleElement parentAst) {
				ModuleElement element = super.adapt(cst, parentAst);
				if (element instanceof PropertyCallExpression) {
					element = new VaultagePropertyCallExpression();
				}
				return element;
			};
		};

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
		Collection<?> result = (Collection<?>)  temp;
//		result.stream().forEach(item -> {
//			System.out.println(((LinkedTreeMap<?, ?>) item).get("content"));
//		});
		assertEquals(2, result.size());
	}

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
		for (Post post : alice.getPosts().stream().filter(p -> p.getIsPublic()).collect(Collectors.toList())) {
			boolean val = result.stream().anyMatch(p -> p.getContent().equals(post.getContent()));
			assertEquals(true, val);
		}
		
		for (Post post : bob.getPosts().stream().filter(p -> p.getIsPublic()).collect(Collectors.toList())) {
			boolean val = result.stream().anyMatch(p -> p.getContent().equals(post.getContent()));
			assertEquals(true, val);
		}

		for (Post post : charlie.getPosts().stream().filter(p -> p.getIsPublic()).collect(Collectors.toList())) {
			boolean val = result.stream().anyMatch(p -> p.getContent().equals(post.getContent()));
			assertEquals(true, val);
		}

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
