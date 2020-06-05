package org.vaultage.demo.fairnet;

import java.util.Scanner;

import org.vaultage.core.BaseBroker;

public class FairnetBroker extends BaseBroker {

	public static final String BROKER_PORT = "61617";
	public static final String BROKER_ADDRESS =
			String.format("tcp://localhost:%s", BROKER_PORT);

	public static void main(String[] args) throws Exception {

		FairnetBroker broker = new FairnetBroker();
		broker.start(BROKER_ADDRESS);

		System.out.println(String.format(
				"FairnetBroker service started in port %s. Press Enter to stop",
				BROKER_PORT));

		Scanner s = new Scanner(System.in);
		s.nextLine();
		s.close();

		broker.stop();
		System.out.println("FairnetBroker stopped");
	}

}