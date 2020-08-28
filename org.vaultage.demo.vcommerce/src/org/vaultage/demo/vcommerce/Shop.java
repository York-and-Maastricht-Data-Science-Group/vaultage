package org.vaultage.demo.vcommerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shop extends ShopBase {
	private String name = new String();
	private List<Item> masterItems = new ArrayList<>();
	private List<CustomerOrder> customerOrders = new ArrayList<>();
	private Warehouse warehouse;
	private Courier courier;

	public Shop() throws Exception {
		super();
	}

	public Shop(String address, int port) throws Exception {
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

	public Warehouse getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(Warehouse warehouse) {
		this.warehouse = warehouse;
	}

	public Courier getCourier() {
		return courier;
	}

	public void setCourier(Courier courier) {
		this.courier = courier;
	}

	public List<Item> getItems() {
		return masterItems;
	}

	public void setItems(List<Item> items) {
		this.masterItems = items;
	}

	public void getItems(String requesterPublicKey, String requestToken) throws Exception {
		final List<Item> itemsForCustomer = new ArrayList<>();

		if (masterItems.size() > 0) {
			int[] itemCount = { 0 };
			Map<String, Item> tokenItem = new HashMap<>();
			for (Item masterItem : masterItems) {
				this.setGetItemQuantityResponseHandler(new GetItemQuantityResponseHandler() {
					@Override
					public void run(Warehouse me, RemoteWarehouse other, String responseToken, Integer result)
							throws Exception {
					}

					@Override
					public void run(Shop me, RemoteWarehouse other, String responseToken, Integer result)
							throws Exception {
						synchronized (masterItem) {
							Item item = tokenItem.get(responseToken);
							item.setQuantity(result);
							itemsForCustomer.add(item);
							itemCount[0] = itemCount[0] + 1;
							if (itemCount[0] >= masterItems.size()) {
								RemoteShop remoteShop = new RemoteShop(Shop.this, requesterPublicKey);
								remoteShop.respondToGetItems(itemsForCustomer, requestToken);
							}
						}
					}
				});

				RemoteWarehouse remoteWarehouse = new RemoteWarehouse(Shop.this, warehouse.getPublicKey());
				String token = remoteWarehouse.getItemQuantity(masterItem.getItemId());
				tokenItem.put(token, masterItem);
			}
		} else {
			RemoteShop remoteShop = new RemoteShop(Shop.this, requesterPublicKey);
			remoteShop.respondToGetItems(itemsForCustomer, requestToken);
		}
	}

	public void createOrder(String requesterPublicKey, String requestToken, Basket basket) throws Exception {
		CustomerOrder customerOrder = new CustomerOrder();
		customerOrder.setItems(new ArrayList<>());
		customerOrders.add(customerOrder);
		
		int[] itemCount = { 0 };
		Map<String, Item> tokenItem = new HashMap<>();
		for (Item basketItem : basket.getItems()) {
			this.setGetItemQuantityResponseHandler(new GetItemQuantityResponseHandler() {

				@Override
				public void run(Warehouse me, RemoteWarehouse other, String responseToken, Integer result)
						throws Exception {
				}

				@Override
				public void run(Shop localShop, RemoteWarehouse remoteWarehouse, String responseToken, Integer stockQty)
						throws Exception {
					synchronized (basketItem) {

						// check each item in the basket if they are available
						Item item = tokenItem.get(responseToken);
						if (item.getQuantity() > stockQty) {
							item.setMessage("We don't have enough stock for this item.");
							item.setQuantity(stockQty);
						}
						customerOrder.getItems().add(item);

						// response if all items have been processed
						itemCount[0] = itemCount[0] + 1;
						if (itemCount[0] >= basket.getItems().size()) {

							// tell warehouse to issue the goods and make them ready at the out-bound
							// terminal
							GoodsIssueOrder goodsIssueOrder = new GoodsIssueOrder();
							goodsIssueOrder.setItems(new ArrayList<>());
							for (Item i : customerOrder.getItems()) {
								goodsIssueOrder.getItems().add(i);
							}

							Shop.this.setIssueGoodsResponseHandler(new IssueGoodsResponseHandler() {
								@Override
								public void run(Warehouse me, RemoteWarehouse other, String responseToken,
										GoodsIssueConfirmation result) throws Exception {
								}

								@Override
								public void run(Shop me, RemoteWarehouse other, String responseToken,
										GoodsIssueConfirmation result) throws Exception {

									// tell the courier to pick up goods and deliver them
									ShippingOrder shippingOrder = new ShippingOrder();
									shippingOrder.setGoodsIssueOrderId(result.getGoodsIssueOrderId());
									shippingOrder.setCustomerPublicKey(requesterPublicKey);
									shippingOrder.setWarehousePulicKey(warehouse.getPublicKey());

									Shop.this.setDeliverGoodsResponseHandler(new DeliverGoodsResponseHandler() {
										@Override
										public void run(Courier me, RemoteCourier other, String responseToken,
												String result) throws Exception {
										}

										@Override
										public void run(Shop me, RemoteCourier other, String responseToken,
												String trackingId) throws Exception {

											// send the order back to customer as a sign that the order has been agreed
											// by
											// the store
											customerOrder.setOrderId(customerOrder.getId());
											customerOrder.setTrackingId(trackingId);
											RemoteShop remoteShop = new RemoteShop(Shop.this, requesterPublicKey);
											remoteShop.respondToCreateOrder(customerOrder, requestToken);
										}
									});

									RemoteCourier remoteCourier = new RemoteCourier(Shop.this, courier.getPublicKey());
									remoteCourier.deliverGoods(shippingOrder);
								}
							});

							remoteWarehouse.issueGoods(goodsIssueOrder);
						}
					}
				}
			});

			RemoteWarehouse remoteWarehouse = new RemoteWarehouse(Shop.this, warehouse.getPublicKey());
			String token = remoteWarehouse.getItemQuantity(basketItem.getItemId());
			tokenItem.put(token, basketItem);
		}
	}

	public void createGoodsReceiptOrder(GoodsReceiptOrder goodsReceiptOrder) throws Exception {
		RemoteWarehouse requester = new RemoteWarehouse(this, warehouse.getPublicKey());
		requester.receiveGoods(goodsReceiptOrder);
	}

	public void trackDelivery(String trackingId, Courier courier2) throws Exception {
		RemoteCourier remoteCourier = new RemoteCourier(this, courier.getPublicKey());
		remoteCourier.trackDelivery(trackingId);
	}

}