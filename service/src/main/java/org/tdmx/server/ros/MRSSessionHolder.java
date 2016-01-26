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
package org.tdmx.server.ros;

import org.tdmx.core.api.v01.mrs.ws.MRS;

/**
 * 
 * @author Peter
 *
 */
public class MRSSessionHolder {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final MRS mrs;
	private final String mrsSessionId;
	private final Integer errorCode;
	private final String errorMessage;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private MRSSessionHolder(MRS mrs, String mrsSessionId, Integer errorCode, String errorMessage) {
		this.mrs = mrs;
		this.mrsSessionId = mrsSessionId;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public static MRSSessionHolder error(int errorCode, String errorMessage) {
		return new MRSSessionHolder(null, null, errorCode, errorMessage);
	}

	public static MRSSessionHolder success(MRS mrs, String sessionId) {
		return new MRSSessionHolder(mrs, sessionId, null, null);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public MRS getMrs() {
		return mrs;
	}

	public String getMrsSessionId() {
		return mrsSessionId;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
