package org.vaultage.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.vaultage.wallet.PaymentInformation;

public class VaultageMessage {

	public enum MessageType {
		REQUEST, RESPONSE
	};

	private String senderAddress;
	private int senderPort = -1;
	private String senderId;
	private String from;
	private String to;
	private MessageType messageType;
	private String operation;
	private Map<String, String> values = new HashMap<>();
	private String token;
	private PaymentInformation paymentInformation;

	public VaultageMessage() {
	}

	public void initToken() {
		token = UUID.randomUUID().toString();
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

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	public int getSenderPort() {
		return senderPort;
	}

	public void setSenderPort(int senderPort) {
		this.senderPort = senderPort;
	}

	public PaymentInformation getPaymentInformation() {
		return paymentInformation;
	}

	public void setPaymentInformation(PaymentInformation paymentInformation) {
		this.paymentInformation = paymentInformation;
	}
}
