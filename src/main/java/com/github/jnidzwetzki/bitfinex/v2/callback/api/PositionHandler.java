package com.github.jnidzwetzki.bitfinex.v2.callback.api;

import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.Position;

public class PositionHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(PositionHandler.class);

	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, final JSONArray jsonArray) 
			throws APIException {
		
		logger.info("Got position callback {}", jsonArray.toString());
		
		final JSONArray positions = jsonArray.getJSONArray(2);
		
		// No positons active
		if(positions.length() == 0) {
			notifyLatch(bitfinexApiBroker);
			return;
		}
		
		// Snapshot or update
		if(! (positions.get(0) instanceof JSONArray)) {
			handlePositionCallback(bitfinexApiBroker, positions);
		} else {
			for(int orderPos = 0; orderPos < positions.length(); orderPos++) {
				final JSONArray orderArray = positions.getJSONArray(orderPos);
				handlePositionCallback(bitfinexApiBroker, orderArray);
			}
		}		
		
		notifyLatch(bitfinexApiBroker);
	}

	/**
	 * Notify the snapshot latch
	 * @param bitfinexApiBroker
	 */
	private void notifyLatch(final BitfinexApiBroker bitfinexApiBroker) {
		
		// All snapshots are completes
		final CountDownLatch positionSnapshotLatch = bitfinexApiBroker.getPositionSnapshotLatch();
		
		if(positionSnapshotLatch != null) {
			positionSnapshotLatch.countDown();
		}
	}

	/**
	 * Handle a position update
	 * @param bitfinexApiBroker
	 * @param positions
	 */
	private void handlePositionCallback(final BitfinexApiBroker bitfinexApiBroker, final JSONArray positions) {
		final String currencyString = positions.getString(0);
		BitfinexCurrencyPair currency = BitfinexCurrencyPair.fromSymbolString(currencyString);
				
		final Position position = new Position(currency);
		position.setStatus(positions.getString(1));
		position.setAmount(positions.getDouble(2));
		position.setBasePrice(positions.getDouble(3));
		position.setMarginFunding(positions.getDouble(4));
		position.setMarginFundingType(positions.getDouble(5));
		position.setPl(positions.optDouble(6, -1));
		position.setPlPercent(positions.optDouble(7, -1));
		position.setPriceLiquidation(positions.optDouble(8, -1));
		position.setLeverage(positions.optDouble(9, -1));
		
		bitfinexApiBroker.getPositionManager().updatePosition(position);
	}

}
