package org.vaultage.demo.pollen;

import java.util.Scanner;

import org.vaultage.core.VaultageMessage;

public class SendNumberPollRequestHandler extends SendNumberPollRequestBaseHandler {

	private Scanner scanner;

	public SendNumberPollRequestHandler(Scanner scanner) {
		isImmediatelyResponded = true;
		this.scanner = scanner;
	}

	@Override
	public double run(VaultageMessage senderMessage, NumberPoll poll) throws Exception {
		return (double) result;

	}

	public Scanner getScanner() {
		return scanner;
	}

	public void setScanner(Scanner scanner) {
		this.scanner = scanner;
	}
}
