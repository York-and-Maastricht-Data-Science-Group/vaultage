package org.vaultage.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VaultageMessage {
	
	private String senderId;
	private String from;
	private String to;
	private String operation;
	private Map<String, String> values = new HashMap<>();
	private String token;

	public VaultageMessage() {
		this.token = UUID.randomUUID().toString();
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getValue(String key) {
		return values.get(key);
	}

	public void putValue(String key, String value) {
		this.values.put(key, value);
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

}
