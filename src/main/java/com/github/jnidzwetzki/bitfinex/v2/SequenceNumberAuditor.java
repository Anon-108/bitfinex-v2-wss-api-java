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

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceNumberAuditor {
	
	public enum ErrorPolicy {
		LOG_ONLY,
		RUNTIME_EXCEPTION;
	}
	
	/**
	 * The public sequence
	 * * 公共序列
	 */
	private long publicSequence;
	
	/**
	 * The private sequence
	 * * 私有序列
	 */
	private long privateSequence;
	
	/**
	 * The error policy
	 * * 错误策略
	 */
	private ErrorPolicy errorPolicy;
	
	/**
	 * Was an error reported?
	 * * 是否报告了错误？
	 */
	private boolean failed;
	
	/**
	 * The Logger
	 * * 记录器
	 */
	private final static Logger logger = LoggerFactory.getLogger(SequenceNumberAuditor.class);
	
	public SequenceNumberAuditor() {
		this.errorPolicy = ErrorPolicy.LOG_ONLY;
		reset();
	}

	/**
	 * Reset the number generator
	 * * 重置数字生成器
	 */
	public void reset() {
		logger.debug("Resetting sequence auditor 重置序列审核员");
		this.publicSequence = -1;
		this.privateSequence = -1;
		this.failed = false;
	}
	
	/**
	 * Audit the package
	 * * 审核包
	 * @param jsonArray
	 */
	public void auditPackage(final JSONArray jsonArray) {		
		final long channelId = jsonArray.getInt(0);
		final boolean isHeartbeat = jsonArray.optString(1, "").equals("hb");
		
		// Channel 0 uses the private and public sequence, other channels use only the public sequence
		//通道 0 使用私有和公共序列，其他通道仅使用公共序列
		// An exception is heartbeat of channel 0, in this case, only the public sequence is used
		// 异常是通道0的心跳，此时只使用公共序列
		if(channelId == 0) {
			if(isHeartbeat) {
				checkPublicSequence(jsonArray);
			} else {
				checkPublicAndPrivateSequence(jsonArray);
			}
		} else {
			checkPublicSequence(jsonArray);
		}
	}

	/**
	 * Check the public and the private sequence
	 * * 检查公共和私人序列
	 * @param jsonArray
	 */
	private void checkPublicAndPrivateSequence(final JSONArray jsonArray) {
		final long nextPublicSequnceNumber = jsonArray.getLong(jsonArray.length() - 2);
		final long nextPrivateSequnceNumber = jsonArray.getLong(jsonArray.length() - 1);

		auditPublicSequence(nextPublicSequnceNumber);
		auditPrivateSequence(nextPrivateSequnceNumber);
	}

	/** 
	 * Check the public sequence
	 * * 检查公共序列
	 */
	private void checkPublicSequence(final JSONArray jsonArray) {
		final long nextPublicSequnceNumber = jsonArray.getLong(jsonArray.length() - 1);
		
		auditPublicSequence(nextPublicSequnceNumber);
	}

	/**
	 * Audit the public sequence
	 * * 审计公共序列
	 * 
	 * @param nextPublicSequnceNumber
	 */
	private void auditPublicSequence(final long nextPublicSequnceNumber) {
		if(publicSequence == -1) {
			publicSequence = nextPublicSequnceNumber;
			return;
		}
		
		if(publicSequence + 1 != nextPublicSequnceNumber) {
			final String errorMessage = String.format(
					"Got %d as next public sequence number, expected %d 将 %d 作为下一个公共序列号，预期为 %d",
					publicSequence + 1, nextPublicSequnceNumber);
			
			handleError(errorMessage);
			return;
		}
		
		publicSequence++;
	}

	/**
	 * Audit the private sequence
	 * * 审计私有序列
	 * @param nextPrivateSequnceNumber
	 */
	private void auditPrivateSequence(final long nextPrivateSequnceNumber) {
		if(privateSequence == -1) {
			privateSequence = nextPrivateSequnceNumber;
			return;
		}
		
		if(privateSequence + 1 != nextPrivateSequnceNumber) {
			final String errorMessage = String.format(
					"Got %d as next private sequence number, expected %d 得到 %d 作为下一个私有序列号，预期为 %d",
					privateSequence + 1, nextPrivateSequnceNumber);
			
			handleError(errorMessage);
			return;
		}
		
		privateSequence++;
	}
	
	/**
	 * Handle the sequence number error
	 * * 处理序列号错误
	 * @param errorMessage
	 */
	private void handleError(final String errorMessage) {
		
		failed = true;
		
		switch (errorPolicy) {
		case LOG_ONLY:
			logger.error(errorMessage);
			break;
			
		case RUNTIME_EXCEPTION:
			throw new RuntimeException(errorMessage);

		default:
			logger.error("Got error 收到错误{} but unkown error policy但未知错误政策 {}", errorMessage, errorPolicy);
			break;
		}
	}
	
	/**
	 * Get the last private sequence
	 * * 获取最后一个私有序列
	 * @return
	 */
	public long getPrivateSequence() {
		return privateSequence;
	}
	
	/**
	 * Get the last public sequence
	 * * 获取最后一个公共序列
	 * @return
	 */
	public long getPublicSequence() {
		return publicSequence;
	}
	
	/**
	 * Get the error policy
	 * * 获取错误策略
	 * @return
	 */
	public ErrorPolicy getErrorPolicy() {
		return errorPolicy;
	}
	
	/**
	 * Set the error policy
	 * * 设置错误策略
	 */
	public void setErrorPolicy(final ErrorPolicy errorPolicy) {
		this.errorPolicy = errorPolicy;
	}
	
	/**
	 * Has the audit failed?
	 * * 审核失败了吗？
	 * @return
	 */
	public boolean isFailed() {
		return failed;
	}
}
