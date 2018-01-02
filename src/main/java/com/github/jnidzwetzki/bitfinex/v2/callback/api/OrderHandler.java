package com.github.jnidzwetzki.bitfinex.v2.callback.api;

import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrderState;

public class OrderHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(OrderHandler.class);

	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, final JSONArray jsonArray) 
			throws APIException {

		logger.info("Got order callback {}", jsonArray.toString());
		
		final JSONArray orders = jsonArray.getJSONArray(2);
		
		// No orders active
		if(orders.length() == 0) {
			notifyOrderLatch(bitfinexApiBroker);
			return;
		}
		
		// Snapshot or update
		if(! (orders.get(0) instanceof JSONArray)) {
			handleOrderCallback(bitfinexApiBroker, orders);
		} else {
			for(int orderPos = 0; orderPos < orders.length(); orderPos++) {
				final JSONArray orderArray = orders.getJSONArray(orderPos);
				handleOrderCallback(bitfinexApiBroker, orderArray);
			}
			
			notifyOrderLatch(bitfinexApiBroker);
		}
	}

	/**
	 * Notify the order latch
	 * @param bitfinexApiBroker
	 */
	private void notifyOrderLatch(final BitfinexApiBroker bitfinexApiBroker) {
		
		// All snapshots are completed
		final CountDownLatch connectionReadyLatch = bitfinexApiBroker.getConnectionReadyLatch();
		
		if(connectionReadyLatch != null) {
			connectionReadyLatch.countDown();
		}
	}

	/**
	 * Handle a single order callback
	 * @param bitfinexApiBroker 
	 * @param orderArray
	 * @throws APIException 
	 */
	private void handleOrderCallback(BitfinexApiBroker bitfinexApiBroker, final JSONArray order) throws APIException {		
		final ExchangeOrder exchangeOrder = new ExchangeOrder();
		exchangeOrder.setApikey(bitfinexApiBroker.getApiKey());
		exchangeOrder.setOrderId(order.getLong(0));
		exchangeOrder.setGroupId(order.optInt(1, -1));
		exchangeOrder.setCid(order.getLong(2));
		exchangeOrder.setSymbol(order.getString(3));
		exchangeOrder.setCreated(order.getLong(4));
		exchangeOrder.setUpdated(order.getLong(5));
		exchangeOrder.setAmount(order.getDouble(6));
		exchangeOrder.setAmountAtCreation(order.getDouble(7));
		exchangeOrder.setOrderType(BitfinexOrderType.fromString(order.getString(8)));
		
		final ExchangeOrderState orderState = ExchangeOrderState.fromString(order.getString(13));
		exchangeOrder.setState(orderState);
		
		exchangeOrder.setPrice(order.getDouble(16));
		exchangeOrder.setPriceAvg(order.optDouble(17, -1));
		exchangeOrder.setPriceTrailing(order.optDouble(18, -1));
		exchangeOrder.setPriceAuxLimit(order.optDouble(19, -1));
		exchangeOrder.setNotify(order.getInt(23) == 1 ? true : false);
		exchangeOrder.setHidden(order.getInt(24) == 1 ? true : false);

		bitfinexApiBroker.getOrderManager().updateOrder(exchangeOrder);
	}
}
