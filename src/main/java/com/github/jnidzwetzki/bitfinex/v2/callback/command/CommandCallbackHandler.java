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
package com.github.jnidzwetzki.bitfinex.v2.callback.command;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;

public interface CommandCallbackHandler {
	
	/**
	 * Handle callback of the command 处理命令的回调
	 * @param jsonObject to consume jsonObject 消费
	 * @throws BitfinexClientException raised in case of handling error 在处理错误的情况下引发
	 */
	void handleChannelData(final JSONObject jsonObject) throws BitfinexClientException;
}
