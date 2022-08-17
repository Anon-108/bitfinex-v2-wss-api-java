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
package com.github.jnidzwetzki.bitfinex.v2.entity.currency;

public class BitfinexFundingCurrency implements BitfinexInstrument {
	
	/**
	 * The funding currency
	 * * 融资货币
	 */
	private final String currency;

	public BitfinexFundingCurrency(final String currency) {
		this.currency = currency;
	}
	
	/**
	 * Get the currency
	 * * 获取货币
	 * @return
	 */
	public String getCurrency() {
		return currency;
	}
	
	/**
	 * Convert to bitfinex string
	 * * 转换为 bitfinex 字符串
	 * @return
	 */
	@Override
	public String toBitfinexString() {
		return "f" + currency;
	}
	
	/**
	 * 
	 */
	public static BitfinexFundingCurrency fromSymbolString(final String string) {
		if(! string.startsWith("f")) {
			throw new IllegalArgumentException("This is not a funding currency symbol 这不是资金货币符号: " + string);
		}
		
		final String currency = string.substring(1);
		
		return new BitfinexFundingCurrency(currency);
	}

	@Override
	public String toString() {
		return "BitfinexFundingCurrency [currency=" + currency + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currency == null) ? 0 : currency.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitfinexFundingCurrency other = (BitfinexFundingCurrency) obj;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		return true;
	}
	
}
