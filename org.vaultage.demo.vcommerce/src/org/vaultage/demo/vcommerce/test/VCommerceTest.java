package org.vaultage.demo.vcommerce.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.vcommerce.Basket;
import org.vaultage.demo.vcommerce.Courier;
import org.vaultage.demo.vcommerce.CreateOrderResponseHandler;
import org.vaultage.demo.vcommerce.Customer;
import org.vaultage.demo.vcommerce.CustomerOrder;
import org.vaultage.demo.vcommerce.DeliveryStatus;
import org.vaultage.demo.vcommerce.GetItemsResponseHandler;
import org.vaultage.demo.vcommerce.GoodsReceiptConfirmation;
import org.vaultage.demo.vcommerce.GoodsReceiptOrder;
import org.vaultage.demo.vcommerce.Item;
import org.vaultage.demo.vcommerce.ReceiveGoodsResponseHandler;
import org.vaultage.demo.vcommerce.RemoteCourier;
import org.vaultage.demo.vcommerce.RemoteShop;
import org.vaultage.demo.vcommerce.RemoteWarehouse;
import org.vaultage.demo.vcommerce.Shop;
import org.vaultage.demo.vcommerce.TrackDeliveryResponseHandler;
import org.vaultage.demo.vcommerce.VcommerceBroker;
import org.vaultage.demo.vcommerce.Warehouse;

public class VCommerceTest {

	private static final long SLEEP_TIME = 1000;
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
		customer.getBillingAddress().setName(customer.getName());
		customer.getBillingAddress().setEmail("alice@wonderland.com");
		customer.getBillingAddress().setMobile("+44001122334455");
		customer.getBillingAddress().setAddress("Christopher Road 56");
		customer.getShippingAddress().setName(customer.getName());
		customer.getShippingAddress().setEmail("alice@wonderland.com");
		customer.getShippingAddress().setMobile("+44001122334455");
		customer.getShippingAddress().setAddress("Holgate Avenue 99");

		Shop shop = new Shop();
		shop.setId("Bobazon");
		shop.setName("Bobazon");

		Warehouse warehouse = new Warehouse();
		warehouse.setId("CharlieHouse");
		warehouse.setName("CharlieHouse");
		warehouse.getOutboundAddress().setName(warehouse.getName());
		warehouse.getOutboundAddress().setEmail("charlie@charliehouse.com");
		warehouse.getOutboundAddress().setMobile("+44556677889900");
		warehouse.getOutboundAddress().setAddress("Jump Street 21");

		Courier courier = new Courier();
		courier.setId("DanHL");
		courier.setName("DanHL");

		VaultageServer server = new VaultageServer(VcommerceBroker.BROKER_ADDRESS);
		customer.register(server);
		shop.register(server);
		warehouse.register(server);
		courier.register(server);

		shop.setWarehouse(warehouse);
		shop.setCourier(courier);

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
		GoodsReceiptOrder goodsReceiptOrder = new GoodsReceiptOrder();
		goodsReceiptOrder.setItems(items);
		goodsReceiptOrder.setRemarks("Goods will arrive on Monday 13:30 PM.");
		goodsReceiptOrder.setShopName(shop.getName());

		// set what how the shop should respond to when a warehouse has responded to a
		// GoodsReceiptOrder
		shop.setReceiveGoodsResponseHandler(new ReceiveGoodsResponseHandler() {
			@Override
			public void run(Shop localShop, RemoteWarehouse other, String responseToken,
					GoodsReceiptConfirmation result) throws Exception {
				for (Item receivedItem : result.getItems()) {
					Item item = localShop.getItems().stream().filter(i -> i.getItemId().equals(receivedItem.getItemId())
							|| i.getName().equals(receivedItem.getName())).findFirst().orElse(null);
					if (item == null) {
						localShop.getItems().add(receivedItem);
					} else {
						item.setQuantity(item.getQuantity() + receivedItem.getQuantity());
					}
				}
				synchronized (localShop.getReceiveGoodsResponseHandler()) {
					localShop.getReceiveGoodsResponseHandler().notify();
				}
			}

			@Override
			public void run(Warehouse me, RemoteWarehouse other, String responseToken, GoodsReceiptConfirmation result)
					throws Exception {
			}
		});
		// put the order
		synchronized (shop.getReceiveGoodsResponseHandler()) {
			shop.createGoodsReceiptOrder(goodsReceiptOrder);
			shop.getReceiveGoodsResponseHandler().wait();
		}

		/**
		 * Customer request a list of items and their quantities.
		 */
		final List<Item> availableItems = new ArrayList<>();
		customer.setGetItemsResponseHandler(new GetItemsResponseHandler() {
			@Override
			public void run(Customer localCustomer, RemoteShop other, String responseToken, List<Item> result)
					throws Exception {
				availableItems.clear();
				availableItems.addAll(result);
				synchronized (localCustomer.getGetItemsResponseHandler()) {
					localCustomer.getGetItemsResponseHandler().notify();
				}
			}

			@Override
			public void run(Shop localCustomer, RemoteShop other, String responseToken, List<Item> result)
					throws Exception {
			}
		});

		synchronized (customer.getGetItemsResponseHandler()) {
			customer.getItems(shop);
			customer.getGetItemsResponseHandler().wait();
		}
//		// display of all available items
//		for (Item item : availableItems) {
//			System.out.println(item.getName() + ": " + item.getQuantity());
//		}

		/***
		 * User starts to order
		 */
		// filling basket
		Basket basket = new Basket();
		basket.setItems(new ArrayList<>());
		Item itemToBuy1 = new Item();
		itemToBuy1.setItemId(availableItems.get(0).getItemId());
		itemToBuy1.setName(availableItems.get(0).getName());
		itemToBuy1.setQuantity(2);
		basket.getItems().add(itemToBuy1);

		Item itemToBuy2 = new Item();
		itemToBuy2.setItemId(availableItems.get(1).getItemId());
		itemToBuy2.setName(availableItems.get(1).getName());
		itemToBuy2.setQuantity(1);
		basket.getItems().add(itemToBuy2);

		CustomerOrder[] customerOrders = new CustomerOrder[1];
		customer.setCreateOrderResponseHandler(new CreateOrderResponseHandler() {
			@Override
			public void run(Shop me, RemoteShop other, String responseToken, CustomerOrder result) throws Exception {
			}

			@Override
			public void run(Customer me, RemoteShop other, String responseToken, CustomerOrder result)
					throws Exception {
				synchronized (me.getCreateOrderResponseHandler()) {
					customerOrders[0] = result;
					me.getCreateOrderResponseHandler().notify();
				}
			}
		});
		synchronized (customer.getCreateOrderResponseHandler()) {
			customer.putOrder(shop, basket);
			customer.getCreateOrderResponseHandler().wait();
		}

		// Customer tracks the delivery
		String trackingId = customerOrders[0].getTrackingId();
		DeliveryStatus[] dsCustomer = new DeliveryStatus[1];
		customer.setTrackDeliveryResponseHandler(new TrackDeliveryResponseHandler() {
			@Override
			public void run(Courier me, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
			}

			@Override
			public void run(Shop me, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
			}

			@Override
			public void run(Customer me, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
				synchronized (me.getTrackDeliveryResponseHandler()) {
					dsCustomer[0] = result;
					me.getTrackDeliveryResponseHandler().notify();
				}
			}
		});
		synchronized (customer.getTrackDeliveryResponseHandler()) {
			customer.trackDelivery(trackingId, courier);
			customer.getTrackDeliveryResponseHandler().wait();
		}
		
		// Shop tracks the delivery
		DeliveryStatus[] dsShop = new DeliveryStatus[1];
		shop.setTrackDeliveryResponseHandler(new TrackDeliveryResponseHandler() {
			@Override
			public void run(Courier me, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
			}

			@Override
			public void run(Shop me, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
				synchronized (me.getTrackDeliveryResponseHandler()) {
					dsShop[0] = result;
					me.getTrackDeliveryResponseHandler().notify();
				}
			}

			@Override
			public void run(Customer me, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
				
			}
		});
		synchronized (shop.getTrackDeliveryResponseHandler()) {
			shop.trackDelivery(trackingId, courier);
			shop.getTrackDeliveryResponseHandler().wait();
		}

		// assert!
		assertEquals(dsCustomer[0].getStatus(), dsShop[0].getStatus());

		// disconnect from the broker server

		customer.unregister();
		shop.unregister();
		warehouse.unregister();
		courier.unregister();
	}

}
