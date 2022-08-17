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
package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderFlag;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrderStatus;
import com.github.jnidzwetzki.bitfinex.v2.entity.currency.BitfinexCurrencyPair;
import com.google.common.collect.Collections2;

public class BitfinexOrderTest {

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
			BitfinexCurrencyPair.registerDefaults();	
		}
	}

	@Test
	public void testStateFromString() {
		Assert.assertEquals(BitfinexSubmittedOrderStatus.ACTIVE, BitfinexSubmittedOrderStatus.fromString("ACTIVE"));
		Assert.assertEquals(BitfinexSubmittedOrderStatus.EXECUTED, BitfinexSubmittedOrderStatus.fromString("EXECUTED @ 18867.0(-0.01)"));
		Assert.assertEquals(BitfinexSubmittedOrderStatus.CANCELED, BitfinexSubmittedOrderStatus.fromString("CANCELED"));
		Assert.assertEquals(BitfinexSubmittedOrderStatus.PARTIALLY_FILLED, BitfinexSubmittedOrderStatus.fromString("PARTIALLY FILLED"));
		Assert.assertEquals(BitfinexSubmittedOrderStatus.POSTONLY_CANCELED, BitfinexSubmittedOrderStatus.fromString("POSTONLY CANCELED"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testStateFromStringInvalid() {
		BitfinexSubmittedOrderStatus.fromString("ABC");
	}
	
	@Test
	public void testOrderStatusFlags0() {
		final BitfinexOrder bitfinexNewOrder = new BitfinexOrder();
		Assert.assertTrue(bitfinexNewOrder.getOrderFlags().isEmpty());
		Assert.assertEquals(0, bitfinexNewOrder.getCombinedFlags());		
	}
	
	@Test
	public void testOrderStatusFlags1() {
		final BitfinexOrder bitfinexNewOrder = new BitfinexOrder();
		bitfinexNewOrder.setOrderFlags(BitfinexOrderFlag.OCO.getFlag());
		Assert.assertEquals(BitfinexOrderFlag.OCO.getFlag(), bitfinexNewOrder.getCombinedFlags());
		Assert.assertTrue(bitfinexNewOrder.getOrderFlags().contains(BitfinexOrderFlag.OCO));
	}
	
	@Test
	public void testOrderStatusFlags2() {
		final Collection<List<BitfinexOrderFlag>> orderflagPermutations 
			= Collections2.permutations(Arrays.asList(BitfinexOrderFlag.values()));
		
		for(final List<BitfinexOrderFlag> flags : orderflagPermutations) {
			// Convert to set
			// 转换为集合
			final HashSet<BitfinexOrderFlag> orderFlags = new HashSet<>(flags);

			final BitfinexOrder bitfinexNewOrder = new BitfinexOrder();
			bitfinexNewOrder.setOrderFlags(orderFlags);
			Assert.assertEquals(orderFlags, bitfinexNewOrder.getOrderFlags());
			
			// Marge and parse flags from and to long
			// Marge 和解析标志 from 和 to long
			bitfinexNewOrder.setOrderFlags(bitfinexNewOrder.getCombinedFlags());
			
			Assert.assertEquals(orderFlags, bitfinexNewOrder.getOrderFlags());
		}
	}
	
	@Test
	public void testOrderStatusFlags3() {
		final BitfinexOrder bitfinexNewOrder1 = new BitfinexOrder();
		bitfinexNewOrder1.setApiKey("abc");
		
		final BitfinexOrder bitfinexNewOrder2 = new BitfinexOrder();
		bitfinexNewOrder2.setApiKey("def");
		
		Assert.assertNotEquals(bitfinexNewOrder1, bitfinexNewOrder2);
		Assert.assertNotEquals(bitfinexNewOrder1.hashCode(), bitfinexNewOrder2.hashCode());
	}

	
}
