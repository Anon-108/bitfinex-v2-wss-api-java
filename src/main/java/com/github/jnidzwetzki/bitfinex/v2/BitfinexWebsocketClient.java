package com.github.jnidzwetzki.bitfinex.v2;

import java.util.Collection;

import com.github.jnidzwetzki.bitfinex.v2.command.BitfinexCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.manager.ConnectionFeatureManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.PositionManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.RawOrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.TradeManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.WalletManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

/**
 * Bitfinex Websocket Client exposing basic operations on server through commands
 * * Bitfinex Websocket Client 通过命令暴露服务器上的基本操作
 */
public interface BitfinexWebsocketClient {

    /**
     * connects to bitfinex server
     * * 连接到 bitfinex 服务器
     */
    void connect();

    /**
     * closes connection with bitfinex server
     * * 关闭与 bitfinex 服务器的连接
     */
    void close();

    /**
     * sends command {@link BitfinexCommand} to server
     * * 发送命令 {@link BitfinexCommand} 到服务器
     * @param command to execute on server
*                * @param 命令在服务器上执行
     */
    void sendCommand(BitfinexCommand command);

    /**
     * reconnects with server
     * * 重新连接服务器
     * @return true if reconnected
     * * @return true 如果重新连接
     */
    boolean reconnect();

    /**
     * convenient method of unsubscribing all channels
     * *取消订阅所有频道的便捷方法
     * @return true if success
     * * @return 如果成功则返回真
     */
    boolean unsubscribeAllChannels();

    /**
     * checks whether client is authenticated (permitted to execute user related events)
     * * 检查客户端是否经过身份验证（允许执行用户相关事件）
     * @return true if authenticated
     * * @return 如果认证成功
     */
    boolean isAuthenticated();

    /**
     * retrieves api key permissions for this client
     * * 检索此客户端的 api 密钥权限
     * @return api key permissions
     * * @return api 密钥权限
     */
    BitfinexApiKeyPermissions getApiKeyPermissions();

    /**
     * retrieves all subscribed channels in this client
     * * 检索此客户端中的所有订阅频道
     * @return collection of symbols
     * * @return 符号集合
     */
    Collection<BitfinexStreamSymbol> getSubscribedChannels();

    /**
     * retrieves immutable view of configuration that this client was initialized with
     * * 检索此客户端初始化时使用的不可变配置视图
     * @return configuration
     * * @return 配置
     */
    BitfinexWebsocketConfiguration getConfiguration();

    /**
     * retrieves callbacks interface where user may register listeners
     * * 检索用户可以注册监听器的回调接口
     * @return available callbacks
     * * @return 可用的回调
     */
    BitfinexApiCallbackListeners getCallbacks();

    /**
     * quote manager
     * * 报价管理器
     * @return quote manager
     * * @return 报价管理器
     *
     */
    QuoteManager getQuoteManager();

    /**
     * convenient way to handle orderbook events
     * * 处理订单簿事件的便捷方式
     * @return order book manager
     * * @return 订单簿管理器
     */
    OrderbookManager getOrderbookManager();

    /**
     * convenient way to handle raw orderbook events
     * * 处理原始订单簿事件的便捷方式
     * @return raw orderbook manager
     * * @return 原始订单管理器
     */
    RawOrderbookManager getRawOrderbookManager();

    /**
     * convenient way to handle position events
     * * 处理位置事件的便捷方式
     * @return position manager
     * * @return 职位经理
     */
    PositionManager getPositionManager();

    /**
     * convenient way to handle (my) order events
     * * 处理（我的）订单事件的便捷方式
     * @return order manager
     * * @return 订单管理器
     */
    OrderManager getOrderManager();

    /**
     * convenient way to handle executed trade events
     * * 处理已执行交易事件的便捷方式
     * @return trade manager
     * * @return 贸易经理
     */
    TradeManager getTradeManager();

    /**
     * convenient way to handle wallet events
     * * 处理钱包事件的便捷方式
     * @return wallet manager
     * * @return 钱包管理器
     */
    WalletManager getWalletManager();

    /**
     * connection feature manager
     * * 连接功能管理器
     * @return connection feature manager
     * * @return 连接功能管理器
     */
    ConnectionFeatureManager getConnectionFeatureManager();
}
