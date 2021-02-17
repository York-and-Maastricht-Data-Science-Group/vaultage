package org.vaultage.demo.spreadcut;
import java.util.Scanner;

import org.vaultage.core.BaseBroker;

public class SpreadcutBroker extends BaseBroker {

	public static final String BROKER_PORT = "61617";
	public static final String BROKER_ADDRESS =
	String.format("tcp://localhost:%s", BROKER_PORT);
	
	public static void main(String[] args) throws Exception {
	
		SpreadcutBroker broker = new SpreadcutBroker();
		broker.start(BROKER_ADDRESS);
		
		System.out.println(String.format(
		"SpreadcutBroker service started in port %s. Press Enter to stop",
		BROKER_PORT));
		
		Scanner s = new Scanner(System.in);
		s.nextLine();
		s.close();
		
		broker.stop();
		System.out.println("SpreadcutBroker stopped");
	}
	
}