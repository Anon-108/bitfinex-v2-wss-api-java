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

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

@ClientEndpoint
public class WebsocketClientEndpoint implements Closeable {

	/**
	 * The user session
	 * * 用户会话
	 */
	private Session userSession = null;

	/**
	 * The message consumer
	 * * 消息消费者
	 */
	private final Consumer<String> onMessageConsumer;

	/**
	 * The error consumer
	 * * 错误消费者
	 */
	private final Consumer<Throwable> onErrorConsumer;

	/**
	 * The close reason consumer
	 * * 关闭原因消费者
	 */
	private final Consumer<CloseReason> onCloseConsumer;
	/**
	 * The endpoint URL
	 * * 端点 URL
	 */
	private final URI endpointURI;
	
	/**
	 * The Logger
	 * * 记录器
	 */
	private final static Logger logger = LoggerFactory.getLogger(SimpleBitfinexApiBroker.class);

	/**
	 * The wait for connection latch
	 * * 等待连接锁存器
	 */
	private CountDownLatch connectLatch = new CountDownLatch(0);

	public WebsocketClientEndpoint(URI bitfinexURI, Consumer<String> onMessageConsumer,
								   Consumer<CloseReason> onCloseConsumer,
								   Consumer<Throwable> onErrorConsumer) {
		this.endpointURI = bitfinexURI;
		this.onMessageConsumer = onMessageConsumer;
		this.onCloseConsumer = onCloseConsumer;
		this.onErrorConsumer = onErrorConsumer;
	}

	/**
	 * Open a new connection and wait until connection is ready
	 * * 打开一个新连接并等待连接准备好
	 * 
	 * @throws DeploymentException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void connect() throws DeploymentException, IOException, InterruptedException {
		final WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		connectLatch = new CountDownLatch(1);
		this.userSession = container.connectToServer(this, endpointURI);
		connectLatch.await(15, TimeUnit.SECONDS);
	}

	@OnOpen
	public void onOpen(final Session userSession) {
		logger.debug("Websocket is now open Websocket 现已打开");
		connectLatch.countDown();
	}

	@OnClose
	public void onClose(final Session userSession, final CloseReason reason) {
		logger.debug("Closing websocket 关闭 websocket: {}", reason);
		onCloseConsumer.accept(reason);
		this.userSession = null;
	}

	@OnMessage(maxMessageSize=1048576)
	public void onMessage(final String message) {
		onMessageConsumer.accept(message);
	}
	
	@OnError
    public void onError(final Session session, final Throwable t) {
		logger.error("OnError called  调用 OnError{}", Throwables.getStackTraceAsString(t));
		onErrorConsumer.accept(t);
		connectLatch.countDown();
    }

	/**
	 * Send a new message to the server
	 * * 向服务器发送新消息
	 * @param message
	 */
	public void sendMessage(final String message) {
		if(userSession == null) {
			logger.error("Unable to send message, user session is null 无法发送消息，用户会话为空");
			return;
		}
		if(userSession.getAsyncRemote() == null) {
			logger.error("Unable to send message, async remote is null 无法发送消息，异步远程为空");
			return;
		}
		userSession.getAsyncRemote().sendText(message);
	}

	/**
	 * Close the connection
	 * * 关闭连接
	 */
	@Override
	public void close() {
		if(userSession == null) {
			return;
		}
		
		try {
			final CloseReason closeReason = new CloseReason(CloseCodes.NORMAL_CLOSURE, "Socket closed");
			userSession.close(closeReason);
		} catch (Throwable e) {
			logger.error("Got exception while closing socket 关闭套接字时出现异常", e);
		}
		
		userSession = null;
	}
	
	/**
	 * Is this websocket connected
	 * * 这个 websocket 是否连接
	 * @return
	 */
	public boolean isConnected() {
		return userSession != null;
	}
}
