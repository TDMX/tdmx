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

	// -------------------------------------------------------------------------
	// HEADER FIELDS
	// -------------------------------------------------------------------------

	private String msgId;
	private String txId;
	private Date sentTimestamp;
	private Date ttlTimestamp;
	private String flowSessionId;
	private String payloadSignature;
	private String externalReference;
	private String headerSignature;

	// -------------------------------------------------------------------------
	// PAYLOAD FIELDS
	// -------------------------------------------------------------------------
	private int chunkSizeFactor; // chunkSizeBytes = 2^chunkSizeFactor
	private long payloadLength; // total encrypted length = SUM length chunks
	private byte[] encryptionContext; // sender input to encryption scheme
	private long plaintextLength; // total length of plaintext ( unencrypted, unzipped )
	private String chunksCRC; // manifest of checksums for each chunk.

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

	public String getFlowSessionId() {
		return flowSessionId;
	}

	public void setFlowSessionId(String flowSessionId) {
		this.flowSessionId = flowSessionId;
	}

	public String getPayloadSignature() {
		return payloadSignature;
	}

	public void setPayloadSignature(String payloadSignature) {
		this.payloadSignature = payloadSignature;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public void setExternalReference(String externalReference) {
		this.externalReference = externalReference;
	}

	public String getHeaderSignature() {
		return headerSignature;
	}

	public void setHeaderSignature(String headerSignature) {
		this.headerSignature = headerSignature;
	}

	public int getChunkSizeFactor() {
		return chunkSizeFactor;
	}

	public void setChunkSizeFactor(int chunkSizeFactor) {
		this.chunkSizeFactor = chunkSizeFactor;
	}

	public long getPayloadLength() {
		return payloadLength;
	}

	public void setPayloadLength(long payloadLength) {
		this.payloadLength = payloadLength;
	}

	public byte[] getEncryptionContext() {
		return encryptionContext;
	}

	public void setEncryptionContext(byte[] encryptionContext) {
		this.encryptionContext = encryptionContext;
	}

	public long getPlaintextLength() {
		return plaintextLength;
	}

	public void setPlaintextLength(long plaintextLength) {
		this.plaintextLength = plaintextLength;
	}

	public String getChunksCRC() {
		return chunksCRC;
	}

	public void setChunksCRC(String chunksCRC) {
		this.chunksCRC = chunksCRC;
	}

}
