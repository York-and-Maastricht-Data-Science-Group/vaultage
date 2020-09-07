package org.vaultage.demo.vcommerce;

import java.util.ArrayList;
import java.util.List;

public class Warehouse extends WarehouseBase {
	private String name = new String();
	private OutboundAddress outboundAddress = new OutboundAddress();

	List<Item> stockItems = new ArrayList<>();

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

	public OutboundAddress getOutboundAddress() {
		return outboundAddress;
	}

	public void setOutboundAddress(OutboundAddress outboundAddress) {
		this.outboundAddress = outboundAddress;
	}

	// operations
	public void getItemQuantity(String requesterPublicKey, String requestToken, String itemId) throws Exception {
		int result = 0;
		Item item = stockItems.stream().filter(i -> i.getItemId().equals(itemId)).findFirst().orElse(null);
		if (item != null) {
			result = item.getQuantity();
		}
		RemoteWarehouse confirmer = new RemoteWarehouse(this, requesterPublicKey);
		confirmer.respondToGetItemQuantity(result, requestToken);
	}

	public void receiveGoods(String requesterPublicKey, String requestToken, GoodsReceiptOrder goodsReceiptOrder)
			throws Exception {
		GoodsReceiptConfirmation result = new GoodsReceiptConfirmation();
		result.setItems(new ArrayList<>());

		for (Item receivedItem : goodsReceiptOrder.getItems()) {
			Item item = stockItems.stream().filter(
					i -> i.getItemId().equals(receivedItem.getItemId()) || i.getName().equals(receivedItem.getName()))
					.findFirst().orElse(null);
			if (item == null) {
				stockItems.add(receivedItem);
			} else {
				item.setQuantity(item.getQuantity() + receivedItem.getQuantity());
			}
			result.getItems().add(receivedItem);
		}
		
		RemoteWarehouse confirmer = new RemoteWarehouse(this, requesterPublicKey);
		result.setRemarks("All items were received well.");
		confirmer.respondToReceiveGoods(result, requestToken);
	}

	public void issueGoods(String requesterPublicKey, String requestToken, GoodsIssueOrder goodsIssueOrder)
			throws Exception {
		GoodsIssueConfirmation result = new GoodsIssueConfirmation();
		result.setItems(new ArrayList<>());
		
		// reduce the quantity of the stock items based on the goods issue order
		for (Item toBeIssuedItem : goodsIssueOrder.getItems()) {
			Item stockItem = stockItems.stream().filter(
					i -> i.getItemId().equals(toBeIssuedItem.getItemId()) || i.getName().equals(toBeIssuedItem.getName()))
					.findFirst().orElse(null);
			stockItem.setQuantity(stockItem.getQuantity() - toBeIssuedItem.getQuantity());
			result.getItems().add(toBeIssuedItem);
		}

		result.setGoodsIssueOrderId(goodsIssueOrder.getId());
		RemoteWarehouse confirmer = new RemoteWarehouse(this, requesterPublicKey);
		result.setRemarks("All items are in the outbound terminal ready to be delivered.");
		confirmer.respondToIssueGoods(result, requestToken);
	}

	public void getOutboundAddress(String requesterPublicKey, String requestToken) throws Exception {
		RemoteWarehouse confirmer = new RemoteWarehouse(this, requesterPublicKey);
		confirmer.respondToGetOutboundAddress(this.getOutboundAddress(), requestToken);
	}

}