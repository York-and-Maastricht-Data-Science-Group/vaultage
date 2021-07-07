package org.vaultage.core;

public abstract class OperationResponseHandler {

	/**
	 * To hold the result of any OperationResponseHandlers
	 */
	protected Object result;

	/**
	 * To get the result of any OperationResponseHandlers
	 * @return the result
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * To set the result of any OperationResponseHandlers
	 * @param result the result to set
	 */
	public void setResult(Object result) {
		this.result = result;
	}
	
}
