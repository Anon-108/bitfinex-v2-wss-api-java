package com.github.jnidzwetzki.bitfinex.v2;

/**
 * bitfinex client factory
 * * bitfinex 客户端工厂
 */
public final class BitfinexClientFactory {

    private BitfinexClientFactory() {

    }

    /**
     * bitfinex client with default configuration - only public channels
     * * 具有默认配置的 bitfinex 客户端 - 仅限公共频道
     *
     * @return {@link SimpleBitfinexApiBroker} client
     * * @return {@link SimpleBitfinexApiBroker} 客户端
     */
    public static BitfinexWebsocketClient newSimpleClient() {
        return newSimpleClient(new BitfinexWebsocketConfiguration());
    }

    /**
     * bitfinex client
     * * bitfinex 客户端
     *
     * @param config - config
     *               * @param 配置 - 配置
     * @return {@link SimpleBitfinexApiBroker} client
     *      * @return {@link SimpleBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient newSimpleClient(final BitfinexWebsocketConfiguration config) {
        final BitfinexApiCallbackRegistry callbackRegistry = new BitfinexApiCallbackRegistry();
		final SequenceNumberAuditor sequenceNumberAuditor = new SequenceNumberAuditor();
		
		sequenceNumberAuditor.setErrorPolicy(config.getErrorPolicy());
		
		return new SimpleBitfinexApiBroker(config, callbackRegistry, sequenceNumberAuditor, false);
    }

    /**
     * bitfinex client with subscribed channel managed.
     * * 管理订阅频道的 bitfinex 客户端。
     * spreads amount of subscribed channels across multiple websocket physical connections.
     * * 跨多个 websocket 物理连接传播订阅频道的数量。
     *
     * @return {@link PooledBitfinexApiBroker} client
     * * @return {@link PooledBitfinexApiBroker} 客户端
     */
    public static BitfinexWebsocketClient newPooledClient() {
        return newPooledClient(new BitfinexWebsocketConfiguration(), 30);
    }

    /**
     * bitfinex client with subscribed channel managed.
     * * 管理订阅频道的 bitfinex 客户端。
     * spreads amount of subscribed channels across multiple websocket physical connections.
     * * 跨多个 websocket 物理连接传播订阅频道的数量。
     *
     * @param config                - config
     *                              * @param 配置 - 配置
     * @param channelsPerWebsocketConnection - channels per websocket connection - 5 - 30 (limit by bitfinex exchange)
        * @param channelsPerWebsocketConnection - 每个 websocket 连接的通道数 - 5 - 30（bitfinex 交换限制）
     * @return {@link PooledBitfinexApiBroker} client
     * * @return {@link PooledBitfinexApiBroker} 客户端
     */
    public static BitfinexWebsocketClient newPooledClient(final BitfinexWebsocketConfiguration config, 
    		final int channelsPerWebsocketConnection) {
    	
        if (channelsPerWebsocketConnection < 5 || channelsPerWebsocketConnection > 30) {
            throw new IllegalArgumentException("'channelsPerWebsocketConnection' must be in range [5, 30) channelsPerWebsocketConnection 必须在 [5, 30) 范围内");
        }
    	
        final BitfinexApiCallbackRegistry callbacks = new BitfinexApiCallbackRegistry();
		final SequenceNumberAuditor sequenceNumberAuditor = new SequenceNumberAuditor();
		
		sequenceNumberAuditor.setErrorPolicy(config.getErrorPolicy());

		return new PooledBitfinexApiBroker(config, callbacks, sequenceNumberAuditor, channelsPerWebsocketConnection);
    }

}
