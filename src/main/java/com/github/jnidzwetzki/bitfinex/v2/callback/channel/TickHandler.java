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

import java.math.BigDecimal;
import java.util.function.BiConsumer;

import org.json.JSONArray;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexTick;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

public class TickHandler implements ChannelCallbackHandler {

    private final int channelId;
    private final BitfinexTickerSymbol symbol;

    private BiConsumer<BitfinexTickerSymbol, BitfinexTick> tickConsumer = (s, t) -> {};

    public TickHandler(int channelId, final BitfinexTickerSymbol symbol) {
        this.channelId = channelId;
        this.symbol = symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final String action, final JSONArray jsonArray) throws BitfinexClientException {
        BitfinexTick tick = jsonToBitfinexTick(jsonArray);
        tickConsumer.accept(symbol, tick);
    }

    @Override
    public BitfinexStreamSymbol getSymbol() {
        return symbol;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    private BitfinexTick jsonToBitfinexTick(final JSONArray jsonArray) {
        final BigDecimal bid = jsonArray.getBigDecimal(0); // 0 = BID 0 = 出价
        final BigDecimal bidSize = jsonArray.getBigDecimal(1);//  1 = BID SIZE 1 = 出价大小
        final BigDecimal ask = jsonArray.getBigDecimal(2); // 2 = ASK 2 = 询问
        final BigDecimal askSize = jsonArray.getBigDecimal(3);//  3 = ASK SIZE 3 = 询价
        final BigDecimal dailyChange = jsonArray.getBigDecimal(4);//  4 = Daily Change 4 = 每日变化
        final BigDecimal dailyChangePerc = jsonArray.getBigDecimal(5);// 5  = Daily Change % 5 = 每日变化百分比
        final BigDecimal price = jsonArray.getBigDecimal(6);//  6 = Last Price 6 = 最后价格
        final BigDecimal volume = jsonArray.getBigDecimal(7); // 7 = Volume 7 = 体积/卷
        final BigDecimal high = jsonArray.getBigDecimal(8); // 8 = High 高
        final BigDecimal low = jsonArray.getBigDecimal(9); // 9 = Low 低

        return new BitfinexTick(bid, bidSize, ask, askSize, dailyChange, dailyChangePerc, price, volume, high, low);
    }

    /**
     * bitfinex tick event consumer
     * * bitfinex 钩子事件消费者
     *
     * @param consumer of event
     */
    public void onTickEvent(BiConsumer<BitfinexTickerSymbol, BitfinexTick> consumer) {
        this.tickConsumer = consumer;
    }
}
