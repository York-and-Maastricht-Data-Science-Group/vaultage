package org.rdbd.demo.fairnet.handler;

import org.rdbd.core.server.RDBDHandler;

public class UserRegistrationHandler extends RDBDHandler {

	private boolean isSuccess = false;

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	
}
