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
package org.tdmx.server.rs.sas.resource;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ps")
@XmlType(name = "ProcessingState")
public class ProcessingStateValue {

	public enum FIELD {
		STATUS("status"),
		TIMESTAMP("timestamp"),
		ERROR_CODE("errorCode"),
		ERROR_MESSAGE("errorMessage");

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	private String status;
	private Date timestamp;
	private Integer errorCode;
	private String errorMessage;

	public String getCliRepresentation() {
		StringBuilder buf = new StringBuilder();
		buf.append("ProcessingState");
		buf.append("; ").append(status);
		buf.append("; ").append(timestamp);
		if (errorCode != null) {
			buf.append("; ").append(errorCode);
			buf.append("; ").append(errorMessage);
		}
		return buf.toString();
	}

	public static ProcessingState mapTo(ProcessingStateValue state) {
		if (state == null) {
			return null;
		}
		return ProcessingState.newProcessingState(EnumUtils.mapTo(ProcessingStatus.class, state.getStatus()),
				state.getTimestamp(), state.getErrorCode(), state.getErrorMessage());
	}

	public static ProcessingStateValue mapFrom(ProcessingState state) {
		if (state == null) {
			return null;
		}
		ProcessingStateValue s = new ProcessingStateValue();
		s.setStatus(EnumUtils.mapToString(state.getStatus()));
		s.setTimestamp(state.getTimestamp());
		s.setErrorCode(state.getErrorCode());
		s.setErrorMessage(state.getErrorMessage());
		return s;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
