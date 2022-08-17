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

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderBookEntry;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;

public class RawOrderbookManager extends AbstractManager {

	/**
	 * The channel callbacks
	 * * 频道回调
	 */
	private final BiConsumerCallbackManager<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> channelCallbacks;

	public RawOrderbookManager(final BitfinexWebsocketClient client, ExecutorService executorService) {
		super(client, executorService);
		this.channelCallbacks = new BiConsumerCallbackManager<>(executorService, client);
		client.getCallbacks().onRawOrderbookEvent((sym, entries) -> {
			entries.forEach(e -> handleNewOrderbookEntry(sym, e));
		});
	}
	
	/**
	 * Register a new trading orderbook callback
	 * * 注册一个新的交易订单回调
	 * @param symbol
	 * @param callback
	 * @throws BitfinexClientException
	 */
	public void registerOrderbookCallback(final BitfinexOrderBookSymbol symbol,
										  final BiConsumer<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> callback) throws BitfinexClientException {
		
		channelCallbacks.registerCallback(symbol, callback);
	}
	
	/**
	 * Remove the a trading orderbook callback
	 * * 移除交易订单簿回调
	 * @param symbol
	 * @param callback
	 * @return true/false
	 * @throws BitfinexClientException
	 */
	public boolean removeOrderbookCallback(final BitfinexOrderBookSymbol symbol,
			final BiConsumer<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> callback) throws BitfinexClientException {
		return channelCallbacks.removeCallback(symbol, callback);
	}

	/**
	 * Subscribe a orderbook
	 * * 订阅订单
	 * @param symbol
	 */
	public void subscribeOrderbook(final BitfinexOrderBookSymbol symbol) {
		
		final SubscribeOrderbookCommand subscribeOrderbookCommand = new SubscribeOrderbookCommand(symbol);
		client.sendCommand(subscribeOrderbookCommand);
	}
	
	/**
	 * Unsubscribe a orderbook
	 * * 取消订阅订单
	 * @param symbol
	 */
	public void unsubscribeOrderbook(final BitfinexOrderBookSymbol symbol) {
		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(symbol);
		client.sendCommand(command);
	}
	
	/**
	 * Handle a new orderbook entry
	 * * 处理一个新的订单簿条目
	 * @param symbol
	 * @param entry
	 */
	public void handleNewOrderbookEntry(final BitfinexOrderBookSymbol symbol, final BitfinexOrderBookEntry entry) {
		
		channelCallbacks.handleEvent(symbol, entry);
	}
}
