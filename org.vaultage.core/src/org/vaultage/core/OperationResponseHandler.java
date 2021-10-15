package org.vaultage.core;

import java.util.HashMap;
import java.util.Map;

public abstract class OperationResponseHandler {

	public final String DEFAULT_KEY = "VDK";

	/**
	 */
	protected final Map<String, Object> results = new HashMap<>();

	/**
	 * To hold response handler, only responding when it receives value or timeout
	 */
	protected final Map<String, Object> holders = new HashMap<>();

	/**
	 * To get the result of any OperationResponseHandlers
	 * 
	 * @return the result
	 */
	public Object getResult() {

		return results.getOrDefault(DEFAULT_KEY, null);
	}

	/**
	 * To set the result of any OperationResponseHandlers
	 * 
	 * @param result the result to set
	 */
	public void setResult(Object result) {
		this.results.put(DEFAULT_KEY, result);
	}

	/**
	 * To add the result of any OperationResponseHandlers
	 * 
	 * @param result the result to set
	 */
	public void addResult(String key, Object result) {
		this.results.put(key, result);
//		Object holder = this.holders.get(key);
//		if (holder != null) {
//			synchronized (holder) {
//				holder.notify();
//			}
//		}
	}

	/***
	 * To get the result associated with a token of any OperationResponseHandlers
	 * 
	 * @param token
	 * @return
	 */
	public Object getResult(String token) {
		Object result = results.remove(token);
//		System.out.println("find: " + token + ", " + result);
		if (result == null) {
			System.console();
		}
		return (result != null) ? result : results.get(DEFAULT_KEY);
	}

	/***
	 * Add a holder that forces a request to wait
	 * @param key
	 * @param holder
	 */
	public void addHolder(String key, Object holder) {
		this.holders.put(key, holder);
	}
	
	/***
	 * Get the holder that forces a request to wait
	 * @param key
	 * @return
	 */
	public Object getHolder(String key) {
		return this.holders.get(key);
	}

}
