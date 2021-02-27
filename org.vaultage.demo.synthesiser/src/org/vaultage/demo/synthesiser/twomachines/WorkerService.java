package org.vaultage.demo.synthesiser.twomachines;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.Worker;
import org.vaultage.demo.synthesiser.traffic.SynchronisedIncrementResponseHandler;

/**
 * Service for generating workers
 *
 * @author Alfa Yohannis
 */
public class WorkerService {

	private static final String WORKER_DIRECTORY = "Z:\\workers\\";
	private static final String LOCAL_IP = "192.168.0.2";
	private static final int NUM_WORKERS = 3;

	public static void main(String[] args) throws Exception {

		System.out.println("Running worker service ...");

//		VaultageServer server = new VaultageServer("tcp://localhost:61616");
		VaultageServer server = new VaultageServer("tcp://139.162.228.32:61616");

		int port = Vaultage.DEFAULT_SERVER_PORT;

		Worker[] workers = new Worker[NUM_WORKERS];

		for (int i = 0; i < NUM_WORKERS; i++) {
			workers[i] = new Worker();
			workers[i].setId("Worker-" + i);
			workers[i].addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
			workers[i].register(server);
			workers[i].startServer(LOCAL_IP, port++);
			Files.write(Paths.get(WORKER_DIRECTORY + workers[i].getId() + ".txt"), workers[i].getPublicKey().getBytes(),
					StandardOpenOption.CREATE);
			System.out.println(workers[i].getId() + " created");
		}

		Scanner scanner = new Scanner(System.in);
		String key = "";
		while (!key.equals("Q") && !key.equals("q")) {
			System.out.println("Enter 'Q' or 'q' to stop: ");
			key = scanner.next().trim();
		}
		scanner.close();
		System.out.println("Shutting down ...");
		// remove connections
		for (int i = 0; i < NUM_WORKERS; i++) {
			workers[i].unregister();
		}
		System.out.println("Terminated!");
		System.exit(0);
	}

}
