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

import org.json.JSONArray;

import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public interface ChannelCallbackHandler {

    /**
     * Handle data for the channel
     *  处理通道的数据
     * @param action        - channel action (hb, te/tu etc.)
     *                      action - 频道动作（hb、te/tu 等）
     * @param message       - json message
     *                      消息 - json 消息
     * @throws BitfinexClientException raised in case of exception
     * * @throws BitfinexClientException 在异常情况下引发
     */
    void handleChannelData(final String action, final JSONArray message) throws BitfinexClientException;

    /**
     * returns channel symbol
     * * 返回管道符号
     */
    BitfinexStreamSymbol getSymbol();

    /**
     * returns channel id 返回通道id
     */
    int getChannelId();
}
