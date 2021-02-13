package org.vaultage.core;

import java.util.HashSet;
import java.util.Set;

public abstract class ResponseMessageHandler {
	
	private Set<ResponseHandler> responseHandlers = new HashSet<>(); 
	
	public abstract void process(VaultageMessage message, String senderPublicKey, Object vault) throws Exception;

	public Set<ResponseHandler> getResponseHandlers() {
		return responseHandlers;
	}

	public void setResponseHandlers(Set<ResponseHandler> responseHandlers) {
		this.responseHandlers = responseHandlers;
	}
}
