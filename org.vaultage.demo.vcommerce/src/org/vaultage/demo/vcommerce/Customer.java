package org.vaultage.demo.vcommerce;

public class Customer extends CustomerBase {
	private String name = new String();
	private BillingAddress billingAddress = new BillingAddress();
	private ShippingAddress shippingAddress = new ShippingAddress();

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

	public BillingAddress getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
	}

	public ShippingAddress getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(ShippingAddress shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	// operations
	public void getBillingAddress(String requesterPublicKey, String requestToken) throws Exception {
		RemoteCustomer remoteCustomer = new RemoteCustomer(this, requesterPublicKey);
		remoteCustomer.respondToGetBillingAddress(this.billingAddress, requestToken);
	}

	public void getShippingAddress(String requesterPublicKey, String requestToken) throws Exception {
		RemoteCustomer remoteCustomer = new RemoteCustomer(this, requesterPublicKey);
		remoteCustomer.respondToGetShippingAddress(this.shippingAddress, requestToken);
	}

	public void getItems(Shop shop) throws Exception {
		RemoteShop requester = new RemoteShop(this, shop.getPublicKey());
		requester.getItems();
	}

	public void putOrder(Shop shop, Basket basket) throws Exception {
		RemoteShop remoteShop = new RemoteShop(this, shop.getPublicKey());
		remoteShop.createOrder(basket);
	}

	public void trackDelivery(String trackingId, Courier courier) throws Exception {
		RemoteCourier remoteCourier = new RemoteCourier(this, courier.getPublicKey());
		remoteCourier.trackDelivery(trackingId);
		
	}

}