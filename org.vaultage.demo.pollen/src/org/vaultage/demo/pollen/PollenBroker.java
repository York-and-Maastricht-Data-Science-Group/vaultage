package org.vaultage.demo.pollen;

import java.util.Scanner;

import org.vaultage.core.BaseBroker;

public class PollenBroker extends BaseBroker {

	public static final String BROKER_PORT = "61616";
	public static final String BROKER_ADDRESS =
			String.format("tcp://localhost:%s", BROKER_PORT);

	public static void main(String[] args) throws Exception {

		PollenBroker broker = new PollenBroker();
		broker.start(BROKER_ADDRESS);

		System.out.println(String.format(
				"PollenBroker service started in port %s. Press Enter to stop",
				BROKER_PORT));

		Scanner s = new Scanner(System.in);
		s.nextLine();
		s.close();

		broker.stop();
		System.out.println("PollenBroker stopped");
	}

}