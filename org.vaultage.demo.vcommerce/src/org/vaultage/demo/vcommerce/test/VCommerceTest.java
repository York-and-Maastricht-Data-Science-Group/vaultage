package org.vaultage.demo.vcommerce.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.vcommerce.Courier;
import org.vaultage.demo.vcommerce.Customer;
import org.vaultage.demo.vcommerce.GoodsReceiptOrder;
import org.vaultage.demo.vcommerce.Item;
import org.vaultage.demo.vcommerce.Shop;
import org.vaultage.demo.vcommerce.VcommerceBroker;
import org.vaultage.demo.vcommerce.Warehouse;

public class VCommerceTest {

	static VcommerceBroker BROKER;
	static double FIXED_RESPONSE = 10.0;

	@BeforeClass
	public static void startBroker() throws Exception {
		BROKER = new VcommerceBroker();
		BROKER.start(VcommerceBroker.BROKER_ADDRESS);
	}

	@AfterClass
	public static void stopBroker() throws Exception {
		BROKER.stop();
	}

	@Test
	public void testGoodsReceivedByCustomer() throws Exception {

		// initialisation
		boolean received = false;

		Customer customer = new Customer();
		customer.setId("Alice");
		customer.setName("Alice");

		Shop shop = new Shop();
		shop.setId("Bobazon");
		shop.setName("Bobazon");

		Warehouse warehouse = new Warehouse();
		warehouse.setId("CharlieHouse");
		warehouse.setName("CharlieHouse");

		Courier courier = new Courier();
		courier.setId("DanHL");
		courier.setName("DanHL");

		VaultageServer server = new VaultageServer(VcommerceBroker.BROKER_ADDRESS);
		customer.register(server);
		shop.register(server);
		warehouse.register(server);
		courier.register(server);

		/**
		 * start with the shop creating order to receive goods at the warehouse's
		 * in-bound
		 **/
		// create the items
		Item item1 = new Item();
		item1.setItemId("LTP");
		item1.setName("Lenovo ThinkPad");
		item1.setQuantity(8);
		Item item2 = new Item();
		item2.setItemId("MBP");
		item2.setName("Mac Book Pro");
		item2.setQuantity(11);
		List<Item> items = new ArrayList<>();
		items.add(item1);
		items.add(item2);
		// create the order
		GoodsReceiptOrder receiptOrder = new GoodsReceiptOrder();
		receiptOrder.setItems(items);
		receiptOrder.setRemarks("Goods will arrive on Monday 13:30 PM.");
		receiptOrder.setShopName(shop.getName());

		// put the order
		shop.createGoodsReceiptOrder(warehouse);

		// assert!
		assertEquals(true, received);

		// disconnect from the broker server
		customer.unregister();
		shop.unregister();
		warehouse.unregister();
		courier.unregister();
	}

}
