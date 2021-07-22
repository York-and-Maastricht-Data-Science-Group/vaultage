package org.vaultage.core;

import java.util.HashMap;
import java.util.Map;

public abstract class OperationResponseHandler {

	public final String DEFAULT_KEY = "VDK";

	/**
	 */
	protected final Map<String, Object> results = new HashMap<>();

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
	}

	/***
	 * To get the result associated with a token of any OperationResponseHandlers
	 * 
	 * @param token
	 * @return
	 */
	public Object getResult(String token) {
		Object result = results.remove(token);
		return (result != null) ? result : results.get(DEFAULT_KEY);
	}

}
