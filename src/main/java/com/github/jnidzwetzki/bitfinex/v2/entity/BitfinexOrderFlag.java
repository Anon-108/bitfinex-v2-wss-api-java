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
package com.github.jnidzwetzki.bitfinex.v2.entity;

public enum BitfinexOrderFlag {

	// one cancels other order
	// 一个取消另一个订单
	OCO(16384),
	
	// post-only limit order
	// 后限价单
	POSTONLY(4096),
	
	// hidden order
	//隐藏订单
	HIDDEN(64),
	
	// Excludes variable rate funding offers
	// 不包括浮动利率融资优惠
	NO_VR(524288),
	
	// Close position if present
	// 如果存在则平仓
	POS_CLOSE(512),
	
	// Reduce margin position only
	// 只减少保证金头寸
	REDUCE_ONLY(1024);
	
	/**
	 * The order flag
	 * * 订单标志
	 */
	private final int flag;
	
	private BitfinexOrderFlag(final int flags) {
		this.flag = flags;
	}
	
	/**
	 * Get the order flag
	 * * 获取订单标志
	 * @return
	 */
	public int getFlag() {
		return flag;
	}
}
