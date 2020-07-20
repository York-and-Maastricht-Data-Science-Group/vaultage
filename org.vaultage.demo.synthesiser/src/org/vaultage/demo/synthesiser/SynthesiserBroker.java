package org.vaultage.demo.synthesiser;

import java.util.Scanner;

import org.vaultage.core.BaseBroker;

public class SynthesiserBroker extends BaseBroker {

	public static final String BROKER_PORT = "61617";
	public static final String BROKER_ADDRESS =
	String.format("tcp://localhost:%s", BROKER_PORT);
	
	public static void main(String[] args) throws Exception {
	
		SynthesiserBroker broker = new SynthesiserBroker();
		broker.start(BROKER_ADDRESS);
		
		System.out.println(String.format(
		"SynthesiserBroker service started in port %s. Press Enter to stop",
		BROKER_PORT));
		
		Scanner s = new Scanner(System.in);
		s.nextLine();
		s.close();
		
		broker.stop();
		System.out.println("SynthesiserBroker stopped");
	}
	
}