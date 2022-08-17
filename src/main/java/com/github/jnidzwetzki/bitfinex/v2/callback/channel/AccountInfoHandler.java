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
package com.github.jnidzwetzki.bitfinex.v2.callback.channel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.BalanceInfoHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.MyExecutedTradeHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.NotificationHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.OrderHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.PositionHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.WalletHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexBalanceUpdate;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexMyExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexPosition;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexWallet;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class AccountInfoHandler implements ChannelCallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(AccountInfoHandler.class);
    
    private final Map<String, ChannelCallbackHandler> channelHandler = new HashMap<>();

    private final int channelId;
    private final BitfinexAccountSymbol symbol;

    private final HeartbeatHandler heartbeatHandler;
    private final PositionHandler positionHandler;
    private final BalanceInfoHandler balanceInfoHandler;
    private final WalletHandler walletHandler;
    private final OrderHandler orderHandler;
    private final MyExecutedTradeHandler tradeHandler;
    private final NotificationHandler notificationHandler;

    public AccountInfoHandler(final int channelId, final BitfinexAccountSymbol symbol) {
        this.channelId = channelId;
        this.symbol = symbol;

        heartbeatHandler = new HeartbeatHandler(channelId, symbol);
        channelHandler.put("hb", heartbeatHandler);

        positionHandler = new PositionHandler(channelId, symbol);
        channelHandler.put("ps", positionHandler); // Position snapshot 位置快照
        channelHandler.put("pn", positionHandler); // Position new 新位置
        channelHandler.put("pu", positionHandler); // Position updated 位置已更新
        channelHandler.put("pc", positionHandler); // Position canceled 位置取消

        final ChannelCallbackHandler fundingHandler = new DoNothingHandler();
        channelHandler.put("fos", fundingHandler); // Founding offer snapshot 创始报价快照
        channelHandler.put("fon", fundingHandler); // Founding offer notification 创始报价通知
        channelHandler.put("fou", fundingHandler); // Founding offer update 创始报价更新
        channelHandler.put("foc", fundingHandler); // Founding offer cancel  创始报价取消

        channelHandler.put("fcs", fundingHandler); // Founding credit snapshot 创始信用快照
        channelHandler.put("fcn", fundingHandler); // Founding credit notification 创始信用通知
        channelHandler.put("fcu", fundingHandler); // Founding credit update 创始信用更新
        channelHandler.put("fcc", fundingHandler); // Founding credit cancel 创始信用注销

        channelHandler.put("fls", fundingHandler); // Founding loans snapshot 创始贷款快照
        channelHandler.put("fln", fundingHandler); // Founding loans notification 创始贷款通知
        channelHandler.put("flu", fundingHandler); // Founding loans update 创始贷款更新
        channelHandler.put("flc", fundingHandler); // Founding loans cancel 创始贷款取消
        channelHandler.put("fte", fundingHandler); // Founding funding trade executed 创始资金交易执行
        channelHandler.put("ftu", fundingHandler); // Founding funding trade updated 创始资金交易更新

        channelHandler.put("ats", new DoNothingHandler()); // Ats - Unknown Ats - 未知

        walletHandler = new WalletHandler(channelId, symbol);
        channelHandler.put("ws", walletHandler); // Wallet snapshot 钱包快照
        channelHandler.put("wu", walletHandler); // Wallet update 钱包更新

        orderHandler = new OrderHandler(channelId, symbol);
        channelHandler.put("os", orderHandler); // Order snapshot 订单快照
        channelHandler.put("on", orderHandler); // Order notification 订单通知
        channelHandler.put("ou", orderHandler); // Order update 订单更新
        channelHandler.put("oc", orderHandler); // Order cancellation 取消订单

        tradeHandler = new MyExecutedTradeHandler(channelId, symbol);
        channelHandler.put("te", tradeHandler); // Trade executed 交易执行
        channelHandler.put("tu", tradeHandler); // Trade updates 贸易更新

        balanceInfoHandler = new BalanceInfoHandler(channelId, symbol);
        channelHandler.put("bu", balanceInfoHandler); // balance update 余额更新

        notificationHandler = new NotificationHandler(channelId, symbol);
        channelHandler.put("n", notificationHandler); // General notification 一般通知
    }

    @Override
    public void handleChannelData(final String action, final JSONArray message) throws BitfinexClientException {
        if (message.toString().contains("ERROR")) {
            logger.error("Got error message 收到错误信息: {}", message.toString());
        }
        
        final ChannelCallbackHandler handler = channelHandler.get(action);
        if (handler == null) {
            logger.error("No match found for action  找不到匹配的操作{}  and message 和留言 {}", action, message);
            return;
        }
        
        try {
            handler.handleChannelData(action, message);
        } catch (final BitfinexClientException e) {
            logger.error("Got exception while handling callback 处理回调时出现异常", e);
        }
    }

    @Override
    public BitfinexStreamSymbol getSymbol() {
        return symbol;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    public void onHeartbeatEvent(final Consumer<Long> heartbeatConsumer) {
        heartbeatHandler.onHeartbeatEvent(heartbeatConsumer);
    }

    public void onPositionsEvent(final BiConsumer<BitfinexAccountSymbol, Collection<BitfinexPosition>> consumer) {
        positionHandler.onPositionsEvent(consumer);
    }

    public void onWalletsEvent(final BiConsumer<BitfinexAccountSymbol, Collection<BitfinexWallet>> consumer) {
        walletHandler.onWalletsEvent(consumer);
    }

    public void onTradeEvent(final BiConsumer<BitfinexAccountSymbol, BitfinexMyExecutedTrade> tradeConsumer) {
        tradeHandler.onTradeEvent(tradeConsumer);
    }

    public void onSubmittedOrderEvent(final BiConsumer<BitfinexAccountSymbol, Collection<BitfinexSubmittedOrder>> consumer) {
        orderHandler.onSubmittedOrderEvent(consumer);
    }

    public void onOrderNotification(final BiConsumer<BitfinexAccountSymbol, BitfinexSubmittedOrder> consumer) {
        notificationHandler.onOrderNotification(consumer);
    }

    public void onBalanceUpdate(final BiConsumer<BitfinexAccountSymbol, BitfinexBalanceUpdate> consumer) {
        balanceInfoHandler.onBalanceUpdate(consumer);
    }
}
