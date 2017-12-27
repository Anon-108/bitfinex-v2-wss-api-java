package com.github.jnidzwetzki.bitfinex.v2.test;

import org.junit.Assert;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrderState;

public class ExchangeOrderStateTest {

	@Test
	public void testStateFromString() {
		Assert.assertEquals(ExchangeOrderState.STATE_ACTIVE, ExchangeOrderState.fromString("ACTIVE"));
		Assert.assertEquals(ExchangeOrderState.STATE_EXECUTED, ExchangeOrderState.fromString("EXECUTED @ 18867.0(-0.01)"));
		Assert.assertEquals(ExchangeOrderState.STATE_CANCELED, ExchangeOrderState.fromString("CANCELED"));
		Assert.assertEquals(ExchangeOrderState.STATE_PARTIALLY_FILLED, ExchangeOrderState.fromString("PARTIALLY FILLED"));
		Assert.assertEquals(ExchangeOrderState.STATE_POSTONLY_CANCELED, ExchangeOrderState.fromString("POSTONLY CANCELED"));		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testStateFromStringInvalid() {
		ExchangeOrderState.fromString("ABC");
	}
}
