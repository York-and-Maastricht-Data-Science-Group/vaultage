package org.vaultage.demo.vcommerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shop extends ShopBase {
	private String name = new String();
	private List<Item> masterItems = new ArrayList<>();
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
							if (itemCount[0] >= 2) {
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
		throw new Exception();
	}

	public void createGoodsReceiptOrder(GoodsReceiptOrder goodsReceiptOrder) throws Exception {
		RemoteWarehouse requester = new RemoteWarehouse(this, warehouse.getPublicKey());
		requester.receiveGoods(goodsReceiptOrder);
	}

}