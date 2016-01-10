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

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final boolean success;
	private final String channelKey;
	private final String errorMessage;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private RelayStatus(boolean success, String channelKey, String errorMessage) {
		this.success = success;
		this.channelKey = channelKey;
		this.errorMessage = errorMessage;
	}

	public static RelayStatus success(String channelKey) {
		return new RelayStatus(true, channelKey, null);
	}

	public static RelayStatus failure(String channelKey, String errorMsg) {
		return new RelayStatus(false, channelKey, errorMsg);
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
		builder.append(", errorMessage=");
		builder.append(errorMessage);
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

	public String getErrorMessage() {
		return errorMessage;
	}

}
