package org.vaultage.demo.pollen.test;

import org.vaultage.demo.pollen.RemoteUser;
import org.vaultage.demo.pollen.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.User;

public class UnitTestNumberPollResponseHandler implements SendNumberPollResponseHandler {
	private double result = 0;

	@Override
	public void run(User me, RemoteUser other, String responseToken, double result) throws Exception {
		this.result = result;
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public double getResult() {
		return result;
	}
}