package com.github.jnidzwetzki.bitfinex.v2.symbol;

import java.util.Objects;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandleTimeFrame;
import com.github.jnidzwetzki.bitfinex.v2.entity.currency.BitfinexInstrument;
import com.github.jnidzwetzki.bitfinex.v2.entity.currency.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.currency.BitfinexFundingCurrency;

/**
 * Bitfinex symbol factory class
 * * Bitfinex 符号工厂类
 */
public final class BitfinexSymbols {

    private BitfinexSymbols() {

    }

    /**
     * Returns symbol for account.
     * * 返回帐户的符号。
     * used only within lib - no practical use for end-user
     * * 仅在 lib 中使用 - 对最终用户没有实际用途
     *
     * @param permissions of specified key 指定键的
     * @param apiKey      for this account 对于这个帐户
     * @return symbol
     */
    public static BitfinexAccountSymbol account(final BitfinexApiKeyPermissions permissions, 
    		final String apiKey) {
    	
        return new BitfinexAccountSymbol(permissions, apiKey);
    }
    
    /**
     * Returns symbol for account.
     * * 返回帐户的符号。
     * used only within lib - no practical use for end-user
     * * 仅在 lib 中使用 - 对最终用户没有实际用途
     *
     * @param permissions of specified key
     *                    * @param 指定键的权限
     * @return symbol
     */
    public static BitfinexAccountSymbol account( final BitfinexApiKeyPermissions permissions) {
        return new BitfinexAccountSymbol(permissions);
    }

    /**
     * Returns symbol for candlestick channel
     * * 返回烛台通道的符号
     *
     * @param currencyPair of candles
     *                     * @param currencyPair 蜡烛
     * @param timeframe    configuration of candles
     *                     * @param 蜡烛图的时间框架配置
     * @return symbol
     */
    public static BitfinexCandlestickSymbol candlesticks(final BitfinexCurrencyPair currencyPair, 
    		final BitfinexCandleTimeFrame timeframe) {
    	
        return new BitfinexCandlestickSymbol(currencyPair, Objects.requireNonNull(timeframe));
    }

    /**
     * Returns symbol for candlestick channel
     * * 返回烛台通道的符号
     *
     * @param currency       of candles 蜡烛
     * @param profitCurrency of candles 蜡烛
     * @param timeframe      configuration of candles 配置的蜡烛
     * @return symbol
     */
    public static BitfinexCandlestickSymbol candlesticks(final String currency, final String profitCurrency, 
    		final BitfinexCandleTimeFrame timeframe) {
    	
        final String currencyNonNull = Objects.requireNonNull(currency).toUpperCase();
        final String profitCurrencyNonNull = Objects.requireNonNull(profitCurrency).toUpperCase();
        
        return candlesticks(BitfinexCurrencyPair.of(currencyNonNull, profitCurrencyNonNull), timeframe);
    }

    /**
     * Returns symbol for executed trades channel
     * * 返回已执行交易通道的符号
     *
     * @param currencyPair of trades channel
     *                     货币对交易通道
     * @return symbol
     */
    public static BitfinexExecutedTradeSymbol executedTrades(final BitfinexCurrencyPair currencyPair) {
        return new BitfinexExecutedTradeSymbol(currencyPair);
    }

    /**
     * Returns symbol for candlestick channel
     * * 返回烛台通道的符号
     *
     * @param currency       of trades channel
     *                       * @param 交易通道货币
     * @param profitCurrency of trades channel
     *                       * @param 交易渠道的利润货币
     * @return symbol
     */
    public static BitfinexExecutedTradeSymbol executedTrades(final String currency, 
    		final String profitCurrency) {
    	
        final String currencyNonNull = Objects.requireNonNull(currency).toUpperCase();
        final String profitCurrencyNonNull = Objects.requireNonNull(profitCurrency).toUpperCase();
        
        return executedTrades(BitfinexCurrencyPair.of(currencyNonNull, profitCurrencyNonNull));
    }

    /**
     * returns symbol for raw order book channel
     * * 返回原始订单簿通道的符号
     *
     * @param currencyPair of raw order book channel
     *                     * @param currencyPair 原始订单簿通道
     * @return symbol
     */
    public static BitfinexOrderBookSymbol rawOrderBook(final BitfinexCurrencyPair currencyPair) {
        return new BitfinexOrderBookSymbol(currencyPair, BitfinexOrderBookSymbol.Precision.R0, null, null);
    }

    /**
     * Returns symbol for raw order book channel
     * * 返回原始订单簿通道的符号
     *
     * @param currency       of raw order book channel
     * @param profitCurrency of raw order book channel
     * @return symbol
     */
    public static BitfinexOrderBookSymbol rawOrderBook(final String currency, final String profitCurrency) {
        final String currencyNonNull = Objects.requireNonNull(currency).toUpperCase();
        final String profitCurrencyNonNull = Objects.requireNonNull(profitCurrency).toUpperCase();
        
        return rawOrderBook(BitfinexCurrencyPair.of(currencyNonNull, profitCurrencyNonNull));
    }

    /**
     * returns symbol for order book channel
     * * 返回订单簿通道的符号
     *
     * @param currencyPair of order book channel
     *                     * @param currencyPair 订单簿通道
     * @param precision    of order book
     *                     * @param 订单簿精度
     * @param frequency    of order book
     *                     * @param 订单频率
     * @param pricePoints  in initial snapshot
     *                     * @param pricePoints 在初始快照中
     * @return symbol
     */
    public static BitfinexOrderBookSymbol orderBook(final BitfinexCurrencyPair currencyPair, 
    		final BitfinexOrderBookSymbol.Precision precision,
    		final BitfinexOrderBookSymbol.Frequency frequency, final int pricePoints) {
    	
        if (precision == BitfinexOrderBookSymbol.Precision.R0) {
            throw new IllegalArgumentException("Use BitfinexSymbols#rawOrderBook() factory method instead 改用 BitfinexSymbols#rawOrderBook() 工厂方法");
        }
        
        return new BitfinexOrderBookSymbol(currencyPair, precision, frequency, pricePoints);
    }

    /**
     * Returns symbol for candlestick channel
     * * 返回烛台通道的符号
     *
     * @param currency       of order book
     *                       订单簿货币
     * @param profitCurrency of order book
     *                       * @param 订单簿的利润货币
     * @param precision      of order book
     *                       * @param 订单簿精度
     * @param frequency      of order book
     *                       * @param 订单频率
     * @param pricePoints    in initial snapshot
     *                       * @param pricePoints 在初始快照中
     * @return symbol
     */
    public static BitfinexOrderBookSymbol orderBook(final String currency, final String profitCurrency, 
    		final BitfinexOrderBookSymbol.Precision precision,
    		final BitfinexOrderBookSymbol.Frequency frequency, final int pricePoints) {
    	
        final String currencyNonNull = Objects.requireNonNull(currency).toUpperCase();
        final String profitCurrencyNonNull = Objects.requireNonNull(profitCurrency).toUpperCase();
        
        return orderBook(BitfinexCurrencyPair.of(currencyNonNull, profitCurrencyNonNull), precision, frequency, pricePoints);
    }

    /**
     * returns symbol for ticker channel
     * * 返回代码通道的符号
     *
     * @param currencyPair of ticker channel
     *                     * @param currencyPair 的报价通道
     * @return symbol
     */
    public static BitfinexTickerSymbol ticker(final BitfinexInstrument currencyPair) {
        return new BitfinexTickerSymbol(currencyPair);
    }

    /**
     * returns symbol for ticker channel
     * * 返回代码通道的符号
     *
     * @param currency       of ticker
     *                       * @param 股票代码货币
     * @param profitCurrency of ticker
     *                       * @param 股票代码的利润货币
     * @return symbol
     */
    public static BitfinexTickerSymbol ticker(final String currency, final String profitCurrency) {
    	
        final String currencyNonNull = Objects.requireNonNull(currency).toUpperCase();
        final String profitCurrencyNonNull = Objects.requireNonNull(profitCurrency).toUpperCase();
        
        return ticker(BitfinexCurrencyPair.of(currencyNonNull, profitCurrencyNonNull));
    }

    /**
     * returns symbol for funding
     * * 返回资金符号
     * 
     * @param bitfinexCurrency
     * @return
     */
	public static BitfinexFundingSymbol funding(final BitfinexFundingCurrency bitfinexCurrency) {
		return new BitfinexFundingSymbol(bitfinexCurrency);
	}
	
	 /**
     * returns symbol for funding
      * * 返回资金符号
     * 
     * @param bitfinexCurrency
     * @return
     */
	public static BitfinexFundingSymbol funding(final String bitfinexCurrency) {
		
        final String currencyNonNull = Objects.requireNonNull(bitfinexCurrency).toUpperCase();
        final BitfinexFundingCurrency currency = new BitfinexFundingCurrency(currencyNonNull);
        
		return funding(currency);
	}
}
