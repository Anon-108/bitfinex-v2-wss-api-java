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
package com.github.jnidzwetzki.bitfinex.v2.entity.currency;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.io.ByteSource;
import org.bboxdb.commons.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.google.common.base.Charsets;

public class BitfinexCurrencyPair implements BitfinexInstrument {

	/**
	 * The known currencies
	 * * 已知货币
	 */
	private final static Map<String, BitfinexCurrencyPair> instances = new ConcurrentHashMap<>();

	/**
	 * The Bitfinex symbol URL
	 * * Bitfinex 符号 URL
	 */
	public static final String SYMBOL_URL = "https://api.bitfinex.com/v1/symbols_details";

	/**
	 * Load and register all known currencies
	 * * 加载并注册所有已知货币
	 *
	 * @throws BitfinexClientException
	 */
	public static void registerDefaults() throws BitfinexClientException {

		try {
			final URL url = new URL(SYMBOL_URL);
			final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.addRequestProperty("User-Agent", "Mozilla");
			ByteSource byteSource = new ByteSource() {
				@Override
				public InputStream openStream() throws IOException {
					return connection.getInputStream();
				}
			};
			final String symbolJson = byteSource.asCharSource(Charsets.UTF_8).read();
			final JSONArray jsonArray = new JSONArray(symbolJson);

			for(int i = 0; i < jsonArray.length(); i++) {
				final JSONObject currency = jsonArray.getJSONObject(i);
				final String pair = currency.getString("pair");

                final Pair<String, String> pairs = parsePair(pair);
                final double minOrderSize = currency.getDouble("minimum_order_size");
                final BitfinexCurrencyType type = parseCurrencyType(pairs);
                register(pairs.getElement1(), pairs.getElement2(), type, minOrderSize);
            }
			
        } catch (IOException e) {
            throw new BitfinexClientException(e);
        }
    }

	/**
	 * Parse the currency type
	 * * 解析货币类型
	 * @param pairs 对
	 * @return the type of the currency 货币类型
	 */
    private static BitfinexCurrencyType parseCurrencyType(final Pair<String, String> pairs) {
    	
    	final String PERPETUAL_END = "F0".toLowerCase();
    	
		if(pairs.getElement1().toLowerCase().endsWith(PERPETUAL_END) 
				&& pairs.getElement2().toLowerCase().endsWith(PERPETUAL_END)) {
			return BitfinexCurrencyType.PERPETUAL;
		}
    	
		return BitfinexCurrencyType.CURRENCY;
	}

	/**
     * Parse the currency pair. Some new pairs contain ':' and are longer than 6 chars, e.g. "dusk:usd".
	 * * 解析货币对。一些新的对包含 ':' 并且长度超过 6 个字符，例如“黄昏：美元”。
     * Pairs with the format *f0:*f0 (e.g. btcf0:ustf0) are 'perpetual contracts'
	 * * 格式为 *f0:*f0 的货币对（例如 btcf0:ustf0）是“永续合约”
     * @param pair bitfinex's currency pair.
	 *             * @param pair bitfinex 的货币对。
     * @return A {@link Pair} with currency1 as first and currency2 as second element.
	 * * @return 一个 {@link Pair}，其中 currency1 作为第一个元素，currency2 作为第二个元素。
     */
    private static Pair<String, String> parsePair(final String pair) throws BitfinexClientException{
        final int idx = pair.indexOf(":");

        final String currency1;
        final String currency2;

        if (idx > -1) {
            currency1 = pair.substring(0, idx).toUpperCase();
            currency2 = pair.substring(idx + 1).toUpperCase();
        } else {
            if (pair.length() != 6) {
                throw new BitfinexClientException("The currency pair is not 6 chars long 货币对不是 6 个字符长: " + pair);
            }
            currency1 = pair.substring(0, 3).toUpperCase();
            currency2 = pair.substring(3, 6).toUpperCase();
        }
        return new Pair<>(currency1, currency2);
    }

	public static void unregisterAll() {
		instances.clear();
	}

	/**
	 * Registers currency pair for use within library
	 * * 注册货币对以在图书馆内使用
	 *
	 * @param currency         currency (from) 货币货币（从）
	 * @param profitCurrency   currency (to) 利润货币货币（到）
	 * @param type the currency type  输入货币类型
	 * @param minimalOrderSize minimal order size 最小订单量
	 * @return registered instance of 的注册实例 {@link BitfinexCurrencyPair}
	 */
	public static BitfinexCurrencyPair register(final String currency,
			final String profitCurrency, final BitfinexCurrencyType type, final double minimalOrderSize) {

		final String key = buildCacheKey(currency, profitCurrency);

		final BitfinexCurrencyPair newCurrency = new BitfinexCurrencyPair(currency, profitCurrency,
				type, minimalOrderSize);

		final BitfinexCurrencyPair oldCurrency = instances.putIfAbsent(key, newCurrency);

		// The currency was already registered
		if(oldCurrency != null) {
			throw new IllegalArgumentException("The currency  货币" + key + " is already known 已经知道");
		}

		return newCurrency;
	}

	/**
	 * Retrieves bitfinex currency pair
	 * * 检索 bitfinex 货币对
	 * @param currency       currency (from) 货币（从）
	 * @param profitCurrency currency (to) 货币（到）
	 * @return BitfinexCurrencyPair Bitfinex 货币对
	 */
	public static BitfinexCurrencyPair of(final String currency, final String profitCurrency) {
		final String key = buildCacheKey(currency, profitCurrency);

		final BitfinexCurrencyPair bcp = instances.get(key);

		if (bcp == null) {
			throw new IllegalArgumentException("CurrencyPair is not registered 货币对未注册: " + currency + " " + profitCurrency);
		}

		return bcp;
	}

	/**
	 * Build the cache key
	 * * 构建缓存键
	 *
	 * @param currency1
	 * @param currency2
	 * @return
	 */
	private static String buildCacheKey(final String currency1, final String currency2) {
		return currency1 + "_" + currency2;
	}

	/**
	 * lists all available pairs
	 * * 列出所有可用的对
	 *
	 * @return list of BitfinexCurrencyPair
	 * * @return BitfinexCurrencyPair 列表
	 */
	public static Collection<BitfinexCurrencyPair> values() {
		return instances.values();
	}

	/**
	 * The name of the first currency
	 * * 第一种货币的名称
	 */
	private final String currency1;

	/**
	 * The name of the second currency
	 * * 第二种货币的名称
	 */
	private final String currency2;
	
	/**
	 * The currency type
	 * * 货币类型
	 */
	private final BitfinexCurrencyType currencyType;

	/**
	 * The minimum order size
	 * * 最小订单大小
	 */
	private double minimumOrderSize;

	private BitfinexCurrencyPair(final String pair1, final String pair2, 
			final BitfinexCurrencyType currencyType, final double minimumOrderSize) {
		
		this.currency1 = pair1;
		this.currency2 = pair2;
		this.currencyType = currencyType;
		this.minimumOrderSize = minimumOrderSize;
	}

	/**
	 * Get the minimum order size
	 * * 获取最小订单大小
	 * @return
	 */
	public double getMinimumOrderSize() {
		return minimumOrderSize;
	}
	
	/**
	 * Get the currency type
	 * * 获取货币类型
	 * @return
	 */
	public BitfinexCurrencyType getCurrencyType() {
		return currencyType;
	}

	/**
	 * Set the minimum order size
	 * * 设置最小订单大小
	 * @param minimumOrderSize
	 */
	public void setMinimumOrderSize(final double minimumOrderSize) {
		this.minimumOrderSize = minimumOrderSize;
	}

	/**
	 * Construct from string
	 * * 从字符串构造
	 * @param symbolString
	 * @return
	 */
	public static BitfinexCurrencyPair fromSymbolString(final String symbolString) {
		for (final BitfinexCurrencyPair currency : BitfinexCurrencyPair.values()) {
			if (currency.toBitfinexString().equalsIgnoreCase(symbolString)) {
				return currency;
			}
		}
		throw new IllegalArgumentException("Unable to find currency pair for 找不到货币对: " + symbolString);
	}

	/**
	 * Convert to bitfinex string (t means trading pair)
	 * * 转换为bitfinex字符串（t表示交易对）
	 * @return
	 */
	@Override
	public String toBitfinexString() {
		if (currency1.length() > 3 || currency2.length() > 3) {
			return "t" + currency1 + ":" + currency2;
		}
		
		return "t" + currency1 + currency2;
	}

	/**
	 * Get the first currency
	 * * 获取第一个货币
	 * @return
	 */
	public String getCurrency1() {
		return currency1;
	}

	/**
	 * Set the second currency
	 * * 设置第二种货币
	 * @return
	 */
	public String getCurrency2() {
		return currency2;
	}

	@Override
	public String toString() {
		return "BitfinexCurrencyPair [currency1=" + currency1 + ", currency2=" + currency2 + ", currencyType="
				+ currencyType + ", minimumOrderSize=" + minimumOrderSize + "]";
	}

}
