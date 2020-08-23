package org.vaultage.demo.vcommerce;

public class Customer extends CustomerBase {
	private String name = new String();

	public Customer() throws Exception {
		super();
	}
	
	public Customer(String address, int port) throws Exception {
		super(address, port);
	}
	
	// getter
	public String getName() {
		return this.name;
	}

	// setter
	public void setName(String name) {
		this.name = name;
	}

	// operations
	
	public void getBillingAddress(String requesterPublicKey, String requestToken) throws Exception {
		throw new Exception();
	}
	
	
	public void getShippingAddress(String requesterPublicKey, String requestToken) throws Exception {
		throw new Exception();
	}

	public void getItems(Shop shop) throws Exception {
		RemoteShop requester = new RemoteShop(this, shop.getPublicKey());
		requester.getItems();
	}
	
		
}