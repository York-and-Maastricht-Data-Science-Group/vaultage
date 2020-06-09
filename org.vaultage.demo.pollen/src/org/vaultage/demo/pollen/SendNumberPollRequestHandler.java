/**** protected region SendNumberPollRequestHandler on begin ****/
package org.vaultage.demo.pollen;

import java.util.List;
import java.util.Scanner;

import org.vaultage.core.VaultageMessage;

public class SendNumberPollRequestHandler extends SendNumberPollRequestBaseHandler {

	private Scanner scanner;

	public SendNumberPollRequestHandler(Scanner scanner) {
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
/**** protected region SendNumberPollRequestHandler end ****/