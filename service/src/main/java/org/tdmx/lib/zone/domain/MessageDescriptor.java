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
package org.tdmx.lib.zone.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * An MessageDescriptor describes a Message.
 * 
 * @author Peter Klauser
 * 
 */
public class MessageDescriptor implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	private String msgId;
	private String txId;
	private long payloadSize;
	private Date sentTimestamp;
	private Date ttlTimestamp;

	private String domainName;
	private String sourceFingerprint;
	private String targetFingerprint;
	private String serviceName;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public MessageDescriptor() {

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MessageDescriptor [");
		builder.append(" msgId=").append(msgId);
		builder.append(" txId=").append(txId);
		builder.append(" sentTimestamp=").append(sentTimestamp);
		builder.append(" ttlTimestamp=").append(ttlTimestamp);
		builder.append(" domainName=").append(domainName);
		builder.append(" sourceFingerprint=").append(sourceFingerprint);
		builder.append(" targetFingerprint=").append(targetFingerprint);
		builder.append(" serviceName=").append(serviceName);
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

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public long getPayloadSize() {
		return payloadSize;
	}

	public void setPayloadSize(long payloadSize) {
		this.payloadSize = payloadSize;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getSourceFingerprint() {
		return sourceFingerprint;
	}

	public void setSourceFingerprint(String sourceFingerprint) {
		this.sourceFingerprint = sourceFingerprint;
	}

	public String getTargetFingerprint() {
		return targetFingerprint;
	}

	public void setTargetFingerprint(String targetFingerprint) {
		this.targetFingerprint = targetFingerprint;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Date getSentTimestamp() {
		return sentTimestamp;
	}

	public void setSentTimestamp(Date sentTimestamp) {
		this.sentTimestamp = sentTimestamp;
	}

	public Date getTtlTimestamp() {
		return ttlTimestamp;
	}

	public void setTtlTimestamp(Date ttlTimestamp) {
		this.ttlTimestamp = ttlTimestamp;
	}

}
