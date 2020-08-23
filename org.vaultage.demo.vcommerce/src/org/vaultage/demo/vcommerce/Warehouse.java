package org.vaultage.demo.vcommerce;

import java.util.ArrayList;
import java.util.List;


public class Warehouse extends WarehouseBase {
	private String name = new String();

	List<Item> itemList = new ArrayList<>();

	public Warehouse() throws Exception {
		super();
	}

	public Warehouse(String address, int port) throws Exception {
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

	public void getItemQuantity(String requesterPublicKey, String requestToken, String itemId) throws Exception {
		int result = 0;
		Item item = itemList.stream().filter(i -> i.getItemId().equals(itemId)).findFirst().orElse(null);
		if (item != null) {
			result = item.getQuantity();
		}
		RemoteWarehouse confirmer = new RemoteWarehouse(this, requesterPublicKey);
		confirmer.respondToGetItemQuantity(result, requestToken);
	}

	public void receiveGoods(String requesterPublicKey, String requestToken, GoodsReceiptOrder goodsReceiptOrder)
			throws Exception {
		GoodsReceiptConfirmation result = new GoodsReceiptConfirmation();
		
		for (Item receivedItem : goodsReceiptOrder.getItems()) {
			Item item = itemList.stream().filter(
					i -> i.getItemId().equals(receivedItem.getItemId()) || i.getName().equals(receivedItem.getName()))
					.findFirst().orElse(null);
			if (item == null) {
				itemList.add(receivedItem);
			} else {
				item.setQuantity(item.getQuantity() + receivedItem.getQuantity());
			}
			result.setItems(goodsReceiptOrder.getItems());
		}
		
		RemoteWarehouse confirmer = new RemoteWarehouse(this, requesterPublicKey);
		result.setRemarks("All items were received well.");
		confirmer.respondToReceiveGoods(result, requestToken);
	}

	public void issueGoods(String requesterPublicKey, String requestToken, GoodsIssueOrder goodsIssueOrder)
			throws Exception {
		throw new Exception();
	}

	public void getOutboundAddress(String requesterPublicKey, String requestToken) throws Exception {
		throw new Exception();
	}

}