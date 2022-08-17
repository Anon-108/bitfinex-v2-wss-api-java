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
package com.github.jnidzwetzki.bitfinex.v2;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.google.common.util.concurrent.MoreExecutors;
import com.github.jnidzwetzki.bitfinex.v2.SequenceNumberAuditor.ErrorPolicy;
import com.github.jnidzwetzki.bitfinex.v2.command.AuthCommand;

public class BitfinexWebsocketConfiguration {

    /**
     * api key
     * * Opi 键
     */
    private String apiKey;

    /**
     * api secret
     * * api 秘密
     */
    private String apiSecret;

    /**
     * false if authentication should be skipped
     * * 如果应跳过身份验证，则为 false
     */
    private boolean authenticationEnabled = false;

    /**
     * false if heartbeat thread should not be running
     * * false 如果心跳线程不应该运行
     */
    private boolean heartbeatThreadActive = true;

    /**
     * false if deadman switch should not be active
     * * 如果死机开关不应该处于活动状态，则为 false
     */
    private boolean deadmanSwitchActive = false;

    /**
     * false if managers should not be active
     * * 如果经理不应该处于活动状态，则为 false
     */
    private boolean managersActive = true;

    /**
     * authentication nonce producer
     * * 认证随机数生产者
     */
    private Supplier<String> authNonceProducer = AuthCommand.AUTH_NONCE_PRODUCER_TIMESTAMP;

    /**
     * executor service used by managers
     * * 管理者使用的执行器服务
     */
    private ExecutorService executorService = MoreExecutors.newDirectExecutorService();

    /**
     * delay in millis used by {@link PooledBitfinexApiBroker}.
     * * {@link PooledBitfinexApiBroker} 使用的毫秒延迟。
     * Server will throw 429 on #connect() if too low.
     * * 如果太低，服务器将在 #connect() 上抛出 429。
     */
    private int connectionEstablishingDelay = 7_500;
    
    /**
	 * The error handling policy
     * * 错误处理策略
	 */
	private ErrorPolicy errorPolicy = ErrorPolicy.LOG_ONLY;

    /**
     * automatically tries to reconnect if connection is lost
     * * 如果连接丢失，自动尝试重新连接
     */
    private boolean autoReconnect = true;

    public BitfinexWebsocketConfiguration() {

    }

    public BitfinexWebsocketConfiguration(final BitfinexWebsocketConfiguration copy) {
        this.apiKey = copy.apiKey;
        this.apiSecret = copy.apiSecret;
        this.authenticationEnabled = copy.authenticationEnabled;
        this.heartbeatThreadActive = copy.heartbeatThreadActive;
        this.deadmanSwitchActive = copy.deadmanSwitchActive;
        this.managersActive = copy.managersActive;
        this.authNonceProducer = copy.authNonceProducer;
        this.executorService = copy.executorService;
        this.connectionEstablishingDelay = copy.connectionEstablishingDelay;
        this.errorPolicy = copy.errorPolicy;
        this.autoReconnect = copy.autoReconnect;
    }

    public void setApiCredentials(final String apiKey, final String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.authenticationEnabled = true;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    public void setAuthenticationEnabled(final boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }

    public boolean isHeartbeatThreadActive() {
        return heartbeatThreadActive;
    }

    public void setHeartbeatThreadActive(final boolean heartbeatThreadActive) {
        this.heartbeatThreadActive = heartbeatThreadActive;
    }

    public boolean isDeadmanSwitchActive() {
        return deadmanSwitchActive;
    }

    public void setDeadmanSwitchActive(final boolean deadmanSwitchActive) {
        this.deadmanSwitchActive = deadmanSwitchActive;
    }

    public boolean isManagersActive() {
        return managersActive;
    }

    public void setManagersActive(final boolean managersActive) {
        this.managersActive = managersActive;
    }

    public Supplier<String> getAuthNonceProducer() {
        return authNonceProducer;
    }

    public void setAuthNonceProducer(final Supplier<String> authNonceProducer) {
        this.authNonceProducer = authNonceProducer;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    public int getConnectionEstablishingDelay() {
        return connectionEstablishingDelay;
    }

    public void setConnectionEstablishingDelay(int connectionEstablishingDelay) {
        this.connectionEstablishingDelay = connectionEstablishingDelay;
    }

	public ErrorPolicy getErrorPolicy() {
		return errorPolicy;
	}

	public void setErrorPolicy(final ErrorPolicy errorPolicy) {
		this.errorPolicy = errorPolicy;
	}

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }


}
