/*******************************************************************************
 *
 *    Copyright (C) 2015-2018 Jan Kristof Nidzwetzki
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *******************************************************************************/
package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bboxdb.commons.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.command.BitfinexCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.BitfinexCommands;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderCancelAllCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderCancelCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderCancelGroupCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderNewCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderUpdateCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrderStatus;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;

public class OrderManager extends SimpleCallbackManager<BitfinexSubmittedOrder> {

	/**
	 * The orders
	 * * 订单
	 */
	private final List<BitfinexSubmittedOrder> orders;

	/**
	 * The order timeout * 订单超时
	 */
	private final long TIMEOUT_IN_SECONDS = 120;

	/**
	 * The number of order retries on error
	 * * 错误重试次数
	 */
	private static final int ORDER_RETRIES = 3;

	/**
	 * The delay between two retries
	 * * 两次重试之间的延迟
	 */
	private static final int RETRY_DELAY_IN_MS = 1000;
	
	/**
	 * The Logger
	 * * 记录器
	 */
	private final static Logger logger = LoggerFactory.getLogger(OrderManager.class);


	public OrderManager(final BitfinexWebsocketClient client, final ExecutorService executorService) {
		super(executorService, client);
		this.orders = new ArrayList<>();
		client.getCallbacks().onMySubmittedOrderEvent((a, e) -> e.forEach(i -> updateOrderCallback(a, i)));
		client.getCallbacks().onMyOrderNotification(this::updateOrderCallback);
	}

	/**
	 * Clear all orders
	 * * 清除所有订单
	 */
	public void clear() {
		synchronized (orders) {
			orders.clear();
		}
	}

	/**
	 * Get the list with exchange orders
	 * * 获取交换订单列表
	 * @return
	 * @throws BitfinexClientException
	 */
	public List<BitfinexSubmittedOrder> getOrders() throws BitfinexClientException {
		synchronized (orders) {
			return orders;
		}
	}

	/**
	 * Update an order - process feedback from exchange
	 * * 更新订单 - 处理来自交易所的反馈
	 * @param exchangeOrder
	 */
	public void updateOrderCallback(final BitfinexAccountSymbol account, 
			final BitfinexSubmittedOrder exchangeOrder) {

		synchronized (orders) {
			// Replace order
			// 替换顺序
			orders.removeIf(o -> Objects.equals(o.getOrderId(), exchangeOrder.getOrderId()));

			// Remove canceled orders
			// 删除取消的订单
			if(exchangeOrder.getStatus() != BitfinexSubmittedOrderStatus.CANCELED) {
				orders.add(exchangeOrder);
			}

			orders.notifyAll();
		}

		notifyCallbacks(exchangeOrder);
	}


	/**
	 * Place an order and retry if Exception occur
	 * * 出现异常时下单重试
	 * @param order - new BitfinexOrder to place 新的 Bitfinex 下订单
	 * @return - The submitted order, can be used for updates 提交的订单，可用于更新
	 * @throws BitfinexClientException
	 * @throws InterruptedException
	 */
	public BitfinexSubmittedOrder placeOrderAndWaitUntilActive(final BitfinexOrder order) throws BitfinexClientException, InterruptedException {

		final BitfinexApiKeyPermissions capabilities = client.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new BitfinexClientException("Unable to wait for order 无法等待订单 " + order + " connection has not enough capabilities 连接没有足够的能力: " + capabilities);
		}

		order.setApiKey(client.getConfiguration().getApiKey());

		final Callable<BitfinexSubmittedOrder> orderCallable = () -> placeOrderOrderOnAPI(order);

		/*
		Bitfinex does not implement a happens-before relationship. Sometimes
		 canceling a stop-loss order and placing a new stop-loss order results
		in an 'ERROR, reason is Invalid order: not enough exchange balance'
		error for some seconds. The retryer tries to place the order up to
		three times
		Bitfinex 没有实现先发生关系。有时
		取消止损订单并下新的止损订单结果
		在“错误，原因是无效订单：没有足够的兑换余额”
		错误几秒钟。重试者尝试将订单下达至
		三次
		 */
		final Retryer<BitfinexSubmittedOrder> retryer = new Retryer<>(ORDER_RETRIES, RETRY_DELAY_IN_MS,
				TimeUnit.MILLISECONDS, orderCallable);
		retryer.execute();

		if(retryer.getNeededExecutions() > 1) {
			logger.info("Nedded {} executions for placing the order “用于下订单的内嵌 {} 执行", retryer.getNeededExecutions());
		}

		if(! retryer.isSuccessfully()) {
			final Exception lastException = retryer.getLastException();

			if(lastException == null) {
				throw new BitfinexClientException("Unable to execute order 无法执行订单");
			} else {
				throw new BitfinexClientException(lastException);
			}
		}
		
		return retryer.getResult();
	}

	/**
	 * Execute a new Order
	 * * 执行新订单
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private BitfinexSubmittedOrder placeOrderOrderOnAPI(final BitfinexOrder order) throws Exception {
		final CountDownLatch waitLatch = new CountDownLatch(1);

		final Consumer<BitfinexSubmittedOrder> ordercallback = (o) -> {
			if(Objects.equals(o.getClientId(), order.getClientId())) {
				waitLatch.countDown();
			}
		};

		registerCallback(ordercallback);

		try {
			placeOrder(order);

			waitLatch.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

			if(waitLatch.getCount() != 0) {
				throw new BitfinexClientException("Timeout while waiting for order 等待订单超时");
			}

			// Check for order error
			final BitfinexSubmittedOrder placedOrder = client
					.getOrderManager()
					.getOrders()
					.stream()
					.filter(o -> o.getClientId() == order.getClientId())
					.findFirst()
					.orElseThrow(() -> new BitfinexClientException("Unable to find order 找不到订单: " + order.getClientId()));
			
			if(placedOrder.getStatus() == BitfinexSubmittedOrderStatus.ERROR) {
				throw new BitfinexClientException("Unable to place order 无法下订单 " + order);
			}

			return placedOrder;
		} catch (Exception e) {
			throw e;
		} finally {
			removeCallback(ordercallback);
		}
	}

	/**
	 * Cancel a order 取消订单
	 * @param id
	 * @throws BitfinexClientException, InterruptedException
	 */
	public void cancelOrderAndWaitForCompletion(final long id) throws BitfinexClientException, InterruptedException {

		final BitfinexApiKeyPermissions capabilities = client.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new BitfinexClientException("Unable to cancel order 无法取消订单 " + id + " connection has not enough capabilities 连接没有足够的能力: " + capabilities);
		}

		final Callable<Boolean> orderCallable = () -> cancelOrderOnAPI(id);

		// See comment in placeOrder()
		// 请参阅 placeOrder() 中的注释
		final Retryer<Boolean> retryer = new Retryer<>(ORDER_RETRIES, RETRY_DELAY_IN_MS,
				TimeUnit.MILLISECONDS, orderCallable);
		retryer.execute();

		if(retryer.getNeededExecutions() > 1) {
			logger.info("Nedded {} executions for canceling the order 执行以取消订单", retryer.getNeededExecutions());
		}

		if(! retryer.isSuccessfully()) {
			final Exception lastException = retryer.getLastException();

			if(lastException == null) {
				throw new BitfinexClientException("Unable to cancel order 无法取消订单");
			} else {
				throw new BitfinexClientException(lastException);
			}
		}
	}

	/**
	 * Cancel the order on the API
	 * * 取消API上的订单
	 * @param id
	 * @return
	 * @throws BitfinexClientException
	 * @throws InterruptedException
	 */
	private boolean cancelOrderOnAPI(final long id) throws BitfinexClientException, InterruptedException {
		final CountDownLatch waitLatch = new CountDownLatch(1);

		final Consumer<BitfinexSubmittedOrder> ordercallback = (o) -> {
			if(o.getOrderId() == id && o.getStatus() == BitfinexSubmittedOrderStatus.CANCELED) {
				waitLatch.countDown();
			}
		};

		registerCallback(ordercallback);

		try {
			logger.info("Cancel order 取消订单: {}", id);
			cancelOrder(id);
			waitLatch.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

			if(waitLatch.getCount() != 0) {
				throw new BitfinexClientException("Timeout while waiting for order 等待订单超时");
			}

			return true;
		} catch (Exception e) {
			throw e;
		} finally {
			removeCallback(ordercallback);
		}
	}


	/**
	 * Place a new order
	 * * 下一个新订单
	 * @throws BitfinexClientException
	 */
	public void placeOrder(final BitfinexOrder order) throws BitfinexClientException {

		final BitfinexApiKeyPermissions capabilities = client.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new BitfinexClientException("Unable to place order 无法下订单" + order + " connection has not enough capabilities: 连接没有足够的能力：" + capabilities);
		}
		
		if(order instanceof BitfinexSubmittedOrder) {
			logger.info("Updating existing order 更新现有订单 {}", order);
			final BitfinexCommand orderCommand = new OrderUpdateCommand((BitfinexSubmittedOrder) order);
			client.sendCommand(orderCommand);
		} else {
			logger.info("Executing new order 执行新订单  {}", order);
			final BitfinexCommand orderCommand = new OrderNewCommand(order);
			client.sendCommand(orderCommand);
		}
	}

	/**
	 * Cancel the given order
	 * * 取消给定的订单
	 * @param id
	 * @throws BitfinexClientException
	 */
	public void cancelOrder(final long id) throws BitfinexClientException {

		final BitfinexApiKeyPermissions capabilities = client.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new BitfinexClientException("Unable to cancel order  无法取消订单 " + id + " connection has not enough capabilities: 连接没有足够的能力： " + capabilities);
		}

		logger.info("Cancel order with id 取消带id的订单 {}", id);
		final OrderCancelCommand cancelOrder = new OrderCancelCommand(id);
		client.sendCommand(cancelOrder);
	}

	/**
	 * Cancel the given order group
	 * 取消给定的订单组
	 * @param id
	 * @throws BitfinexClientException
	 */
	public void cancelOrderGroup(final int id) throws BitfinexClientException {

		final BitfinexApiKeyPermissions capabilities = client.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new BitfinexClientException("Unable to cancel order group 无法取消订单组 " + id + " connection has not enough capabilities: 连接没有足够的能力：" + capabilities);
		}

		logger.info("Cancel order group 取消订单组 {}", id);
		final OrderCancelGroupCommand cancelOrder = new OrderCancelGroupCommand(id);
		client.sendCommand(cancelOrder);
	}

    /**
     * Cancel the given order group
	 * 取消给定的订单组
     * @throws BitfinexClientException
     */
    public void cancelAllOrders() throws BitfinexClientException {

        final BitfinexApiKeyPermissions capabilities = client.getApiKeyPermissions();

        if(! capabilities.isOrderWritePermission()) {
            throw new BitfinexClientException("Unable to cancel all orders - connection has not enough capabilities: 无法取消所有订单 - 连接功能不足：" + capabilities);
        }

        logger.info("Cancel all active orders 取消所有有效订单");
        OrderCancelAllCommand cancelOrders = BitfinexCommands.cancelAllOrders();
        client.sendCommand(cancelOrders);
    }
}
