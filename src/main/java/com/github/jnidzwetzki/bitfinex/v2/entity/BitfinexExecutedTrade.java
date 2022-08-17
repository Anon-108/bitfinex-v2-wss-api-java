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

import java.math.BigDecimal;
import java.util.Objects;

public class BitfinexExecutedTrade {

	/**
	 * trade id
	 * * 贸易编号
	 */
	private Long tradeId;

	/**
	 * execution timestamp
	 * * 执行时间戳
	 */
	private Long timestamp;

	/**
	 * amount - positive for buy, negative for sell
	 * * 金额 - 买入为正，卖出为负
	 */
	private BigDecimal amount;

	/**
	 * price at which trade was executed
	 * * 交易执行的价格
	 */
	private BigDecimal price;

	/**
	 * !! USED ONLY BY FUNDING
	 * 	 * Rate at which funding transaction occurred
	 * 	 仅用于资助
	 * * 资金交易发生的比率
	 */
	private BigDecimal rate;

	/**
	 * !! USED ONLY BY FUNDING
	 * Amount of time the funding transaction was for
	 * ！！仅用于资助
	 * * 资金交易的时间量
	 */
	private Long period;


	public Long getTradeId() {
		return tradeId;
	}

	public void setTradeId(Long tradeId) {
		this.tradeId = tradeId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BitfinexExecutedTrade that = (BitfinexExecutedTrade) o;
		return Objects.equals(tradeId, that.tradeId) &&
				Objects.equals(timestamp, that.timestamp) &&
				Objects.equals(amount, that.amount) &&
				Objects.equals(price, that.price) &&
				Objects.equals(rate, that.rate) &&
				Objects.equals(period, that.period);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tradeId, timestamp, amount, price, rate, period);
	}

	@Override
	public String toString() {
		return "BitfinexExecutedTrade [" +
				"tradeId=" + tradeId +
				", timestamp=" + timestamp +
				", amount=" + amount +
				", price=" + price +
				", rate=" + rate +
				", period=" + period +
				']';
	}
}
