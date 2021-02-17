package org.vaultage.demo.vcommerce.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.Vault;
import org.vaultage.core.Vaultage;
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

		VaultageServer server = new VaultageServer(VcommerceBroker.BROKER_ADDRESS);

		Customer customer = new Customer();
		Shop shop = new Shop();
		Warehouse warehouse = new Warehouse();
		Courier courier = new Courier();

		DeliveryStatus[] dsCustomer = new DeliveryStatus[1];
		DeliveryStatus[] dsShop = new DeliveryStatus[1];

		runSequentialScenario(server, customer, shop, warehouse, courier, dsCustomer, dsShop);

		// assert!
		assertEquals(dsCustomer[0].getStatus(), dsShop[0].getStatus());

		// disconnect from the broker server

		customer.unregister();
		shop.unregister();
		warehouse.unregister();
		courier.unregister();
	}

	@Test
	public void testGoodsReceivedByCustomerDirectMessage() throws Exception {

		VaultageServer server = new VaultageServer(VcommerceBroker.BROKER_ADDRESS);

		Customer customer = new Customer();
		Shop shop = new Shop();
		Warehouse warehouse = new Warehouse();
		Courier courier = new Courier();

		// temporary values to assert
		DeliveryStatus[] dsCustomer = new DeliveryStatus[1];
		DeliveryStatus[] dsShop = new DeliveryStatus[1];

		int port = new Integer(Vaultage.DEFAULT_SERVER_PORT);

		customer.startServer("127.0.0.1", port++);
		shop.startServer("192.168.56.1", port++);
		courier.startServer("192.168.14.2", port++);
		warehouse.startServer("192.168.99.80", port++);

		/**
		 * Set up all vaults to trust each other. Therefore, they don't have to use a
		 * broker to communicate. Initially, vaults will use a broker as a channel to
		 * communicate to get the trusted addresses of other vaults. If other vaults are
		 * trusted, they will use direct messaging instead. It the remote vaults cannot
		 * be reached by direct messaging, a broker will be re-used.
		 **/
		Vault[] vaults = { customer, shop, warehouse, courier };
		for (Vault vault1 : vaults) {
			for (Vault vault2 : vaults) {
				if (!vault1.equals(vault2)) {
					vault1.getVaultage().getPublicKeyToRemoteAddress().put(vault2.getPublicKey(),
							vault2.getVaultage().getDirectMessageServerAddress());
				}
			}
		}

		Thread.sleep(SLEEP_TIME);

		runSequentialScenario(server, customer, shop, warehouse, courier, dsCustomer, dsShop);

		// assert!
		assertEquals(dsCustomer[0].getStatus(), dsShop[0].getStatus());

		customer.unregister();
		shop.unregister();
		warehouse.unregister();
		courier.unregister();

		customer.shutdownServer();
		shop.shutdownServer();
		warehouse.shutdownServer();
		courier.shutdownServer();
	}

	protected void runSequentialScenario(VaultageServer server, Customer customer, Shop shop, Warehouse warehouse,
			Courier courier, DeliveryStatus[] dsCustomer, DeliveryStatus[] dsShop)
			throws Exception, InterruptedException {
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

		shop.setId("Bobazon");
		shop.setName("Bobazon");

		warehouse.setId("CharlieHouse");
		warehouse.setName("CharlieHouse");
		warehouse.getOutboundAddress().setName(warehouse.getName());
		warehouse.getOutboundAddress().setEmail("charlie@charliehouse.com");
		warehouse.getOutboundAddress().setMobile("+44556677889900");
		warehouse.getOutboundAddress().setAddress("Jump Street 21");

		courier.setId("DanHL");
		courier.setName("DanHL");

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
		shop.addOperationResponseHandler(new ReceiveGoodsResponseHandler() {
			@Override
			public void run(Vault localVault, RemoteWarehouse other, String responseToken,
					GoodsReceiptConfirmation result) throws Exception {
				Shop localShop = (Shop) localVault;
				for (Item receivedItem : result.getItems()) {
					Item item = localShop.getItems().stream().filter(i -> i.getItemId().equals(receivedItem.getItemId())
							|| i.getName().equals(receivedItem.getName())).findFirst().orElse(null);
					if (item == null) {
						localShop.getItems().add(receivedItem);
					} else {
						item.setQuantity(item.getQuantity() + receivedItem.getQuantity());
					}
				}
				synchronized (localShop.getOperationResponseHandler(ReceiveGoodsResponseHandler.class)) {
					localShop.getOperationResponseHandler(ReceiveGoodsResponseHandler.class).notify();
				}
			}

			@Override
			public void run(Warehouse me, RemoteWarehouse other, String responseToken, GoodsReceiptConfirmation result)
					throws Exception {
			}
		});
		// put the order
		synchronized (shop.getOperationResponseHandler(ReceiveGoodsResponseHandler.class)) {
			shop.createGoodsReceiptOrder(goodsReceiptOrder);
			shop.getOperationResponseHandler(ReceiveGoodsResponseHandler.class).wait();
		}

		/**
		 * Customer request a list of items and their quantities.
		 */
		final List<Item> availableItems = new ArrayList<Item>();
		customer.addOperationResponseHandler(new GetItemsResponseHandler() {
			@Override
			public void run(Vault localVault, RemoteShop other, String responseToken, List<Item> result)
					throws Exception {
				Customer localCustomer = (Customer) localVault;
				availableItems.clear();
				availableItems.addAll(result);
				synchronized (localCustomer.getOperationResponseHandler(GetItemsResponseHandler.class)) {
					localCustomer.getOperationResponseHandler(GetItemsResponseHandler.class).notify();
				}
			}

			@Override
			public void run(Shop localCustomer, RemoteShop other, String responseToken, List<Item> result)
					throws Exception {
			}
		});

		synchronized (customer.getOperationResponseHandler(GetItemsResponseHandler.class)) {
			customer.getItems(shop);
			customer.getOperationResponseHandler(GetItemsResponseHandler.class).wait();
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
		customer.addOperationResponseHandler(new CreateOrderResponseHandler() {
			@Override
			public void run(Shop me, RemoteShop other, String responseToken, CustomerOrder result) throws Exception {
			}

			@Override
			public void run(Vault localVault, RemoteShop other, String responseToken, CustomerOrder result)
					throws Exception {
				Customer me = (Customer) localVault;
				synchronized (me.getOperationResponseHandler(CreateOrderResponseHandler.class)) {
					customerOrders[0] = result;
					me.getOperationResponseHandler(CreateOrderResponseHandler.class).notify();
				}
			}
		});
		synchronized (customer.getOperationResponseHandler(CreateOrderResponseHandler.class)) {
			customer.putOrder(shop, basket);
			customer.getOperationResponseHandler(CreateOrderResponseHandler.class).wait();
		}

		// Customer tracks the delivery
		String trackingId = customerOrders[0].getTrackingId();

		customer.addOperationResponseHandler(new TrackDeliveryResponseHandler() {
			@Override
			public void run(Courier me, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
			}

			@Override
			public void run(Vault localVault, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
				if (localVault instanceof Customer) {
					Customer me = (Customer) localVault;
					synchronized (me.getOperationResponseHandler(TrackDeliveryResponseHandler.class)) {
						dsCustomer[0] = result;
						me.getOperationResponseHandler(TrackDeliveryResponseHandler.class).notify();
					}
				}
			}
		});
		synchronized (customer.getOperationResponseHandler(TrackDeliveryResponseHandler.class)) {
			customer.trackDelivery(trackingId, courier);
			customer.getOperationResponseHandler(TrackDeliveryResponseHandler.class).wait();
		}

		// Shop tracks the delivery

		shop.addOperationResponseHandler(new TrackDeliveryResponseHandler() {
			@Override
			public void run(Courier me, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
			}

			@Override
			public void run(Vault localVault, RemoteCourier other, String responseToken, DeliveryStatus result)
					throws Exception {
				if (localVault instanceof Shop) {
					Shop me = (Shop) localVault;
					synchronized (me.getOperationResponseHandler(TrackDeliveryResponseHandler.class)) {
						dsShop[0] = result;
						me.getOperationResponseHandler(TrackDeliveryResponseHandler.class).notify();
					}
				}
			}

		});
		synchronized (shop.getOperationResponseHandler(TrackDeliveryResponseHandler.class)) {
			shop.trackDelivery(trackingId, courier);
			shop.getOperationResponseHandler(TrackDeliveryResponseHandler.class).wait();
		}
	}

}
