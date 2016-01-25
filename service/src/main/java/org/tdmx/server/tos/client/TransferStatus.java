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
package org.tdmx.server.tos.client;

/**
 * A value object describing the final status of internal transfer between client sessions.
 * 
 * @author Peter
 *
 */
public class TransferStatus {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public enum ErrorCode {
		// non retryable errors
		PCS_FAILURE(false, "Unable to communicate with the PCS."),
		TOS_CONNECTION_REFUSED(false, "Unable to connect to the TOS."),
		TOS_RPC_CHANNEL_CLOSED(false, "Channel to TOS has closed."),
		TOS_RPC_CALL_FAILURE(false, "TOS RPC call failure."),
		// retryable errors
		TOS_RELAY_DECLINED(true, "TOS declined to accept transfer of data."),
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
	private final String sessionId;
	private final ErrorCode errorCode;
	private final String tosTcpAddress;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private TransferStatus(boolean success, String sessionId, String tosTcpAddress, ErrorCode errorCode) {
		this.success = success;
		this.sessionId = sessionId;
		this.tosTcpAddress = tosTcpAddress;
		this.errorCode = errorCode;
	}

	public static TransferStatus success(String sessionId, String tosTcpAddress) {
		return new TransferStatus(true, sessionId, tosTcpAddress, null);
	}

	public static TransferStatus failure(ErrorCode errorCode) {
		return new TransferStatus(false, null, null, errorCode);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TransferStatus [success=");
		builder.append(success);
		builder.append(", sessionId=");
		builder.append(sessionId);
		builder.append(", tosTcpAddress=");
		builder.append(tosTcpAddress);
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

	public String getSessionId() {
		return sessionId;
	}

	public String getTosTcpAddress() {
		return tosTcpAddress;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

}
