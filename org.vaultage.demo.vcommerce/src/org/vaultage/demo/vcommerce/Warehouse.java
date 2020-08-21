package org.vaultage.demo.vcommerce;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// import org.vaultage.demo.vcommerce.GoodsReceiptOrder;
// import org.vaultage.demo.vcommerce.GoodsReceiptConfirmation;
// import org.vaultage.demo.vcommerce.GoodsIssueOrder;
// import org.vaultage.demo.vcommerce.GoodsIssueConfirmation;
// import org.vaultage.demo.vcommerce.CustomerOrder;
// import org.vaultage.demo.vcommerce.BillingAddress;
// import org.vaultage.demo.vcommerce.ShippingAddress;
// import org.vaultage.demo.vcommerce.OutboundAddress;
// import org.vaultage.demo.vcommerce.Item;
// import org.vaultage.demo.vcommerce.Basket;
// import org.vaultage.demo.vcommerce.ShippingOrder;
// import org.vaultage.demo.vcommerce.DeliveryStatus;
// import org.vaultage.demo.vcommerce.WarehouseBase;

public class Warehouse extends WarehouseBase {
	private String name = new String();

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
	
	public void getItemQuantity(Warehouse requesterWarehouse, String requestToken, String itemId) throws Exception {
		throw new Exception();
	}
	
	
	public void receiveGoods(Warehouse requesterWarehouse, String requestToken, GoodsReceiptOrder goodsReceiptOrder) throws Exception {
		throw new Exception();
	}
	
	
	public void issueGoods(Warehouse requesterWarehouse, String requestToken, GoodsIssueOrder goodsIssueOrder) throws Exception {
		throw new Exception();
	}
	
	
	public void getOutboundAddress(Warehouse requesterWarehouse, String requestToken) throws Exception {
		throw new Exception();
	}
	
		
}