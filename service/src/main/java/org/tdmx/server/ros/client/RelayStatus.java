/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.server.ros.client;

/**
 * A value object describing the final status of relay initiation.
 * 
 * @author Peter
 *
 */
public class RelayStatus {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public enum ErrorCode {
		// non retryable errors
		PCS_FAILURE(false, "Unable to communicate with the PCS."),
		ROS_CONNECTION_REFUSED(false, "Unable to connect to the ROS."),

		ROS_RPC_CHANNEL_CLOSED(true, "Channel to ROS has closed."),
		ROS_RELAY_DECLINED(true, "ROS declined to relay data."),
		ROS_RPC_CALL_FAILURE(false, "ROS RPC call failure."),
		//
		;

		private ErrorCode(boolean retry, String errorMsg) {
			this.retry = retry;
			this.errorMessage = errorMsg;
		}

		private boolean retry;
		private String errorMessage;

		public String getErrorMessage() {
			return errorMessage;
		}

		public boolean isRetryable() {
			return retry;
		}

	}

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final boolean success;
	private final String channelKey;
	private final ErrorCode errorCode;
	private final String rosTcpAddress;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private RelayStatus(boolean success, String channelKey, String rosTcpAddress, ErrorCode errorCode) {
		this.success = success;
		this.channelKey = channelKey;
		this.rosTcpAddress = rosTcpAddress;
		this.errorCode = errorCode;
	}

	public static RelayStatus success(String channelKey, String rosTcpAddress) {
		return new RelayStatus(true, channelKey, rosTcpAddress, null);
	}

	public static RelayStatus failure(String channelKey, ErrorCode errorCode) {
		return new RelayStatus(false, channelKey, null, errorCode);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RelayStatus [success=");
		builder.append(success);
		builder.append(", channelKey=");
		builder.append(channelKey);
		builder.append(", errorCode=");
		builder.append(errorCode);
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public boolean isSuccess() {
		return success;
	}

	public String getChannelKey() {
		return channelKey;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public String getRosTcpAddress() {
		return rosTcpAddress;
	}

}
