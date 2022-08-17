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

import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.google.common.collect.Sets;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexConnectionFeature;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.command.SetConnectionFeaturesCommand;

public class ConnectionFeatureManager extends AbstractManager {
	
	/**
	 * The connection features
	 * * 连接功能
	 */
	private final Set<BitfinexConnectionFeature> connectionFeatures;
	
	/**
	 * The active connection features * 主动连接功能
	 * (we got a status update for our requested features) *（我们得到了我们要求的功能的状态更新）
	 */
	private int activeConnectionFeatures;
	
	
	public ConnectionFeatureManager(final BitfinexWebsocketClient client,
			final ExecutorService executorService) {
		super(client, executorService);
		
		this.connectionFeatures = Sets.newConcurrentHashSet();
		this.activeConnectionFeatures = 0;
	}

	/**
	 * Enable a connection feature * 启用连接功能
	 * @param feature
	 */
	public void enableConnectionFeature(final BitfinexConnectionFeature feature) {
		connectionFeatures.add(feature);
		applyConnectionFeatures();
	}
	
	/**
	 * Disable a connection feature *禁用连接功能
	 * @param feature
	 */
	public void disableConnectionFeature(final BitfinexConnectionFeature feature) {
		connectionFeatures.remove(feature);
		applyConnectionFeatures();
	}

	/**
	 * Is the given connection feature enabled? * 是否启用了给定的连接功能？
	 * @param feature
	 * @return
	 */
	public boolean isConnectionFeatureEnabled(final BitfinexConnectionFeature feature) {
		return connectionFeatures.contains(feature);
	}
	
	/**
	 * Get the active connection features * 获取活动连接功能
	 * @return
	 */
	public int getActiveConnectionFeatures() {
		return activeConnectionFeatures;
	}
	
	/**
	 * Is the given connection feature active? * 给定的连接功能是否有效？
	 * @param feature
	 * @return
	 */
	public boolean isConnectionFeatureActive(final BitfinexConnectionFeature feature) {
		return (activeConnectionFeatures | feature.getFeatureFlag()) == activeConnectionFeatures;
	}
	
	/**
	 * Set the active connection features * 设置活动连接功能
	 * @param activeConnectionFeatures
	 */
	public void setActiveConnectionFeatures(final int activeConnectionFeatures) {
		this.activeConnectionFeatures = activeConnectionFeatures;
	}
	
	/**
	 * Apply the set connection features to connection * 将设置的连接功能应用于连接
	 */
	public void applyConnectionFeatures() {
		final SetConnectionFeaturesCommand apiCommand = new SetConnectionFeaturesCommand(connectionFeatures);
		client.sendCommand(apiCommand);
	}
}
