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
package com.github.jnidzwetzki.bitfinex.v2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import org.bboxdb.commons.concurrent.ExceptionSafeRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.command.PingCommand;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.util.EventsInTimeslotManager;

public class HeartbeatThread extends ExceptionSafeRunnable {

	/**
	 * The ticker timeout
	 * * 代码超时
	 */
	public static final long TICKER_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

	/**
	 * The API timeout
	 * * API 超时
	 */
	public static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(45);

	/**
	 * The API timeout
	 * * API 超时
	 */
	private static final long HEARTBEAT = TimeUnit.SECONDS.toMillis(5);

	/**
	 * Max reconnects in 10 minutes
	 * * 最多在 10 分钟内重新连接
	 */
	private static final int MAX_RECONNECTS_IN_TIME = 10;

	/**
	 * The API broker
	 * * API 代理
	 */
	private final BitfinexWebsocketClient bitfinexApiBroker;

	/**
	 * websocketEndpoint
	 * * websocket端点
	 */
	private final WebsocketClientEndpoint websocketEndpoint;


	/**
	 * The reconnect timeslot manager
	 * * 重新连接时隙管理器
	 */
	private final EventsInTimeslotManager eventsInTimeslotManager;

	/**
	 * last heartbeat supplier
	 * * 最后的心跳供应商
	 */
	private final Supplier<Long> lastHeartbeatSupplier;

	/**
	 * The Logger
	 * * 记录器
	 */
	private final static Logger logger = LoggerFactory.getLogger(HeartbeatThread.class);

	/**
	 * callback registry reference
	 * * 回调注册表参考
	 */
	private final BitfinexApiCallbackRegistry callbackRegistry;

	/**
	 * new heartbeat thread constructor
	 * * 新的心跳线程构造函数
	 * @param bitfinexApiBroker			- bitfinex api broken
	 *                                  * @param bitfinexApiBroker - bitfinex api 损坏
	 * @param websocketClientEndpoint	- websocket endpoint
	 *                                  * @param websocketClientEndpoint - websocket 端点
	 * @param lastHeartbeatSupplier     - last heartbeat supplier
	 *                                  * @param lastHeartbeatSupplier - 最后的心跳供应商
	 */
	public HeartbeatThread(final BitfinexWebsocketClient bitfinexApiBroker,
						   final WebsocketClientEndpoint websocketClientEndpoint,
						   final Supplier<Long> lastHeartbeatSupplier) {
		this.bitfinexApiBroker = bitfinexApiBroker;
		this.callbackRegistry = (BitfinexApiCallbackRegistry) bitfinexApiBroker.getCallbacks();
		this.websocketEndpoint = websocketClientEndpoint;
		this.lastHeartbeatSupplier = lastHeartbeatSupplier;

		this.eventsInTimeslotManager = new EventsInTimeslotManager(
				MAX_RECONNECTS_IN_TIME,
				10,
				TimeUnit.MINUTES);
	}

    @Override
    public void runThread() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(3));
                if (websocketEndpoint == null) {
                    continue;
                }
                if (!websocketEndpoint.isConnected()) {
					callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_BY_REMOTE);
					if (this.bitfinexApiBroker.getConfiguration().isAutoReconnect()) {
						logger.error("We are not connected, reconnecting 我们没有连接，正在重新连接");
						executeReconnect();
					}
                    continue;
                }
                sendHeartbeatIfNeeded();
                if (!checkTickerFreshness()) {
					callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_BY_REMOTE);
					if (this.bitfinexApiBroker.getConfiguration().isAutoReconnect()) {
						logger.error("Ticker are outdated, reconnecting 代码已过时，正在重新连接");
						executeReconnect();
					}
                    continue;
                }
                if (checkConnectionTimeout()) {
					callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_BY_REMOTE);
					if (this.bitfinexApiBroker.getConfiguration().isAutoReconnect()) {
						logger.error("Global connection heartbeat time out, reconnecting 全局连接心跳超时，正在重新连接");
						executeReconnect();
					}
                }
            }
        } catch (final InterruptedException e) {
            logger.debug("Heartbeat thread was interrupted, exiting 心跳线程被中断，正在退出");
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            logger.error("Exception raised 引发异常", e);
        }
    }

	/**
	 * Are all tickers up-to-date
	 * * 所有代码都是最新的
	 * @return
	 */
	private boolean checkTickerFreshness() {
		final QuoteManager quoteManager = bitfinexApiBroker.getQuoteManager();
		final Map<BitfinexStreamSymbol, Long> heartbeatValues = quoteManager.getLastTickerActivity();

		return checkTickerFreshness(heartbeatValues);
	}

	/**
	 * Are all ticker up-to-date
	 * * 所有股票都是最新的
	 * @return
	 */
	@VisibleForTesting
	public static boolean checkTickerFreshness(final Map<BitfinexStreamSymbol, Long> heartbeatValues) {
		final long currentTime = System.currentTimeMillis();

		final List<BitfinexStreamSymbol> outdatedSymbols = heartbeatValues.entrySet().stream()
			.filter(e -> e.getValue() + TICKER_TIMEOUT < currentTime)
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());

		outdatedSymbols.forEach(symbol -> {
			logger.debug("Symbol {} 已过时 is outdated by {}ms",
					symbol, currentTime - heartbeatValues.get(symbol));
		});
		return outdatedSymbols.isEmpty();
	}

	/**
	 * Send a heartbeat package on the connection
	 * * 在连接上发送心跳包
	 */
	private void sendHeartbeatIfNeeded() {
		final long nextHeartbeat = lastHeartbeatSupplier.get() + HEARTBEAT;

		if(nextHeartbeat < System.currentTimeMillis()) {
			logger.debug("Send heartbeat 发送心跳");
			bitfinexApiBroker.sendCommand(new PingCommand());
		}
	}

	/**
	 * Check for connection timeout
	 * * 检查连接超时
	 * @return
	 */
	private boolean checkConnectionTimeout() {
        return lastHeartbeatSupplier.get() + CONNECTION_TIMEOUT < System.currentTimeMillis();
    }

	/**
	 * Execute the reconnect
	 * * 执行重新连接
	 * @throws InterruptedException
	 */
	private void executeReconnect() throws InterruptedException {
		// Close connection
		// 关闭连接
		websocketEndpoint.close();

		// Store the reconnect time to prevent too much
		// 存储重连时间，防止过多
		// reconnects in a short timeframe. Otherwise the
		// 在短时间内重新连接。 否则
		// rate limit will apply and the reconnects are not successfully
		// 将应用速率限制并且重新连接不成功
		logger.info("Wait for next reconnect timeslot 等待下一个重新连接时隙");
		eventsInTimeslotManager.recordNewEvent();
		eventsInTimeslotManager.waitForNewTimeslot();
		logger.info("Wait for next reconnect timeslot DONE 等待下一个重新连接时隙 DONE");

		bitfinexApiBroker.reconnect();
	}
}
