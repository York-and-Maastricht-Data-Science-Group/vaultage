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
// import org.vaultage.demo.vcommerce.ShopBase;

public class Shop extends ShopBase {

	public Shop() throws Exception {
		super();
	}
	
	public Shop(String address, int port) throws Exception {
		super(address, port);
	}
	
	// getter

	// setter

	// operations
	
	public void getItems(Shop requesterShop, String requestToken) throws Exception {
		throw new Exception();
	}
	
	
	public void createOrder(Shop requesterShop, String requestToken, Basket basket) throws Exception {
		throw new Exception();
	}
	
		
}