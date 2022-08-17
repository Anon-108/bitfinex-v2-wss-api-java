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
package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.command.CalculateCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexWallet;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;

public class WalletManager extends AbstractManager {

	/**
	 * WalletTable aggregator - key(Wallet-Type, Currency) = Wallet
	 * * WalletTable 聚合器 - key(Wallet-Type, Currency) = Wallet
	 */
	private final Table<BitfinexWallet.Type, String, BitfinexWallet> walletTable;

	/**
	 * Value of assets under management (not including losses/profits)
	 * * 管理资产的价值（不包括亏损/利润）
	 */
	private BigDecimal assetsUnderManagement = BigDecimal.ZERO;

	/**
	 * Value of assets under management (including losses/profits)
	 * * 管理资产的价值（包括亏损/利润）
	 */
	private BigDecimal assetsUnderManagementNet  = BigDecimal.ZERO;

	public WalletManager(final BitfinexWebsocketClient client, final ExecutorService executorService) {
		super(client, executorService);
		this.walletTable = HashBasedTable.create();
		client.getCallbacks().onMyWalletEvent((account, wallets) -> wallets.forEach(wallet -> {
            try {
                Table<BitfinexWallet.Type, String, BitfinexWallet> walletTable = getWalletTable();
                synchronized (walletTable) {
                    walletTable.put(wallet.getWalletType(), wallet.getCurrency(), wallet);
                    walletTable.notifyAll();
                }
            } catch (BitfinexClientException e) {
                e.printStackTrace();
            }
        }));
		client.getCallbacks().onBalanceUpdateEvent((account, balanceUpdate) -> {
			assetsUnderManagement = balanceUpdate.getAssetsUnderManagement();
			assetsUnderManagementNet = balanceUpdate.getAssetsUnderManagementNet();
		});
	}

	/**
	 * Get all wallets
	 * * 获取所有钱包
	 * @return
	 * @throws BitfinexClientException
	 */
	public Collection<BitfinexWallet> getWallets() throws BitfinexClientException {

		throwExceptionIfUnauthenticated();

		synchronized (walletTable) {
			return Collections.unmodifiableCollection(walletTable.values());
		}
	}

	/**
	 * Get all wallets
	 * * 获取所有钱包
	 * @return
	 * @throws BitfinexClientException
	 */
	public Table<BitfinexWallet.Type, String, BitfinexWallet> getWalletTable() throws BitfinexClientException {
		return walletTable;
	}

	/**
	 * Throw a new exception if called on a unauthenticated connection
	 * * 如果在未经身份验证的连接上调用，则抛出一个新异常
	 * @throws BitfinexClientException
	 */
	private void throwExceptionIfUnauthenticated() throws BitfinexClientException {
		if(! client.isAuthenticated()) {
			throw new BitfinexClientException("Unable to perform operation on an unauthenticated connection" +
					"无法对未经身份验证的连接执行操作");
		}
	}

	/**
	 * Calculate the wallet margin balance for the given currency (e.g., BTC)
	 * * 计算给定货币（例如 BTC）的钱包保证金余额
	 *
	 * @param symbol
	 * @throws BitfinexClientException
	 */
	public void calculateWalletMarginBalance(final String symbol) throws BitfinexClientException {
		throwExceptionIfUnauthenticated();

		client.sendCommand(new CalculateCommand("wallet_margin_" + symbol));
	}

	/**
	 * Calculate the wallet funding balance for the given currency (e.g., BTC)
	 * * 计算给定货币的钱包资金余额（例如，BTC）
	 *
	 * @param symbol
	 * @throws BitfinexClientException
	 */
	public void calculateWalletFundingBalance(final String symbol) throws BitfinexClientException {
		throwExceptionIfUnauthenticated();

		client.sendCommand(new CalculateCommand("wallet_funding_" + symbol));
	}

	/**
	 * Total Assets Under Management for associated account
	 * * 关联账户的总资产管理
	 * @return assets under management 管理资产
	 */
	public BigDecimal getAssetsUnderManagement() {
		return assetsUnderManagement;
	}

	/**
	 * Net Assets Under Management for associated account
	 * * 关联账户管理的净资产
	 * @return assets under management net
	 * 管理资产净额
	 */
	public BigDecimal getAssetsUnderManagementNet() {
		return assetsUnderManagementNet;
	}
}
