package com.github.jnidzwetzki.bitfinex.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import org.ta4j.core.Tick;

import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;

public class TickerManager {
	
	/**
	 * The last ticks
	 */
	protected final Map<String, Tick> lastTick;
	
	/**
	 * The last tick timestamp
	 */
	protected final Map<String, Long> lastTickTimestamp;
	
	/**
	 * The channel callbacks
	 */
	private final Map<String, List<BiConsumer<String, Tick>>> channelCallbacks;

	/**
	 * The executor service
	 */
	private final ExecutorService executorService;

	public TickerManager(final ExecutorService executorService) {
		this.executorService = executorService;
		this.lastTick = new HashMap<>();
		this.lastTickTimestamp = new HashMap<>();
		this.channelCallbacks = new HashMap<>();
	}
	
	/**
	 * Get the last heartbeat for the symbol
	 * @param symbol
	 * @return
	 */
	public long getHeartbeatForSymbol(final String symbol) {
		synchronized (lastTick) {
			final Long heartbeat = lastTickTimestamp.get(symbol);
			
			if(heartbeat == null) {
				return -1;
			}
			
			return heartbeat;
		}
	}
	
	/**
	 * Update the channel heartbeat
	 * @param channel
	 */
	public void updateChannelHeartbeat(final String symbol) {
		synchronized (lastTick) {
			lastTickTimestamp.put(symbol, System.currentTimeMillis());
		}
	}
	
	/**
	 * Get a set with active symbols
	 * @return
	 */
	public Set<String> getActiveSymbols() {
		synchronized (lastTick) {
			return lastTick.keySet();
		}
	}
	
	/**
	 * Get the last tick for a given symbol
	 * @param currencyPair
	 * @return 
	 */
	public Tick getLastTick(final BitfinexCurrencyPair currencyPair) {
		final String bitfinexString = currencyPair.toBitfinexString();
		return getLastTick(bitfinexString);
	}
	
	/**
	 * Get the last tick for a given symbol
	 * @param currencyPair
	 * @return 
	 */
	public Tick getLastTick(final String bitfinexString) {
		synchronized (lastTick) {
			return lastTick.get(bitfinexString);
		}
	}
	
	/**
	 * Invalidate the ticket heartbeat values
	 */
	public void invalidateTickerHeartbeat() {
		// Invalidate last tick timetamps
		synchronized (lastTick) {
			lastTickTimestamp.clear();	
		}
	}
	
	/**
	 * Register a new tick callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerTickCallback(final String symbol, final BiConsumer<String, Tick> callback) throws APIException {
		
		channelCallbacks.putIfAbsent(symbol, new ArrayList<>());
				
		final List<BiConsumer<String, Tick>> callbacks = channelCallbacks.get(symbol);
		
		synchronized (callbacks) {
			callbacks.add(callback);	
		}
	}
	
	/**
	 * Remove the a tick callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeTickCallback(final String symbol, final BiConsumer<String, Tick> callback) throws APIException {
		
		if(! channelCallbacks.containsKey(symbol)) {
			throw new APIException("Unknown ticker string: " + symbol);
		}
			
		final List<BiConsumer<String, Tick>> callbacks = channelCallbacks.get(symbol);
		
		synchronized (callbacks) {
			return callbacks.remove(callback);
		}
	}
	
	/**
	 * Process a list with ticks
	 * @param symbol
	 * @param ticksArray
	 */
	public void handleTicksList(final String symbol, final List<Tick> ticksBuffer) {
		
		// Notify callbacks async
		final List<BiConsumer<String, Tick>> callbacks = channelCallbacks.get(symbol);
		
		if(callbacks == null) {
			return;
		}
				
		synchronized(callbacks) {
			if(callbacks.isEmpty()) {
				return;
			}
			
			for (final Tick tick : ticksBuffer) {
				callbacks.forEach((c) -> {
					c.accept(symbol, tick);
				});
			}
		}
	}
	
	/**
	 * Handle a new tick
	 * @param symbol
	 * @param tick
	 */
	public void handleNewTick(final String symbol, final Tick tick) {
		synchronized (lastTick) {
			lastTick.put(symbol, tick);
			lastTickTimestamp.put(symbol, System.currentTimeMillis());
		}
		
		final List<BiConsumer<String, Tick>> callbacks = channelCallbacks.get(symbol);

		if(callbacks == null) {
			return;
		}
				
		synchronized(callbacks) {
			if(callbacks.isEmpty()) {
				return;
			}
			
			callbacks.forEach((c) -> {
				final Runnable runnable = () -> c.accept(symbol, tick);
				executorService.submit(runnable);
			});
		}
	}
	
}
