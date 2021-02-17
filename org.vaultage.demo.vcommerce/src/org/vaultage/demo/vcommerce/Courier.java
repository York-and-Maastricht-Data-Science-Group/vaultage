package org.vaultage.demo.vcommerce;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.vaultage.core.Vault;

public class Courier extends CourierBase {
	private String name = new String();
	private Map<String, ShippingOrder> trackShippingOrders = new HashMap<>();

	public Courier() throws Exception {
		super();
	}

	public Courier(String address, int port) throws Exception {
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

	public void deliverGoods(String requesterPublicKey, String requestToken, ShippingOrder shippingOrder)
			throws Exception {

		/** get the shipping address directly from the customer **/
		String customerPK = shippingOrder.getCustomerPublicKey();
		RemoteCustomer remoteCustomer = new RemoteCustomer(this, customerPK);

		this.addOperationResponseHandler(new GetShippingAddressResponseHandler() {
			@Override
			public void run(Customer me, RemoteCustomer other, String responseToken, ShippingAddress result)
					throws Exception {
			}

			@Override
			public void run(Vault localVault, RemoteCustomer remoteVault, String responseToken, ShippingAddress result)
					throws Exception {
				/** get the out-bound address directly from the warehouse **/
				String warehousePK = shippingOrder.getWarehousePulicKey();
				RemoteWarehouse remoteWarehouse = new RemoteWarehouse(Courier.this, warehousePK);

				Courier.this.addOperationResponseHandler(new GetOutboundAddressResponseHandler() {
					@Override
					public void run(Warehouse me, RemoteWarehouse other, String responseToken, OutboundAddress result)
							throws Exception {
					}

					@Override
					public void run(Vault localVault, RemoteWarehouse remoteVault, String responseToken,
							OutboundAddress result) throws Exception {
						// send the trackingId back to shop
						RemoteCourier remoteCourier = new RemoteCourier(Courier.this, requesterPublicKey);
						String trackingId = UUID.randomUUID().toString();
						trackShippingOrders.put(trackingId, shippingOrder);
						remoteCourier.respondToDeliverGoods(trackingId, requestToken);
					}

				});
				remoteWarehouse.getOutboundAddress();

			}
		});
		remoteCustomer.getShippingAddress();
	}

	public void trackDelivery(String requesterPublicKey, String requestToken, String trackingId) throws Exception {

		RemoteCourier remoteCourier = new RemoteCourier(this, requesterPublicKey);
		DeliveryStatus ds = new DeliveryStatus();
		if (trackShippingOrders.get(trackingId) != null) {
			ds.setStatus("Delivering");
		} else {
			ds.setStatus("Delivered");
		}
		remoteCourier.respondToTrackDelivery(ds, requestToken);
	}

}