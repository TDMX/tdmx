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
package org.tdmx.lib.message.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdmx.lib.zone.domain.FlowSession;

/**
 * A Message.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "Message")
public class Message implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_MSGID_LEN = 64;

	public static final int MAX_EXTREF_LEN = 2048;

	public static final int MAX_SIGNATURE_LEN = 128;

	public static final int MAX_CRCMANIFEST_LEN = 8000;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "MessageIdGen")
	@TableGenerator(name = "MessageIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "messageObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	// -------------------------------------------------------------------------
	// HEADER FIELDS
	// -------------------------------------------------------------------------
	@Column(length = MAX_MSGID_LEN, nullable = false)
	private String msgId;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date txTS;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date liveUntilTS;

	@Column(nullable = false)
	private Long flowId;

	@Column(length = FlowSession.MAX_IDENTIFIER_LEN, nullable = false)
	private String flowSessionId;

	@Column(length = MAX_SIGNATURE_LEN, nullable = false)
	private String payloadSignature;

	@Column(length = MAX_EXTREF_LEN)
	private String externalReference;

	@Column(length = MAX_SIGNATURE_LEN, nullable = false)
	private String headerSignature;

	// -------------------------------------------------------------------------
	// PAYLOAD FIELDS
	// -------------------------------------------------------------------------
	@Column(nullable = false)
	private int chunkSizeFactor; // chunkSizeBytes = 2^chunkSizeFactor

	@Column(nullable = false)
	private long payloadLength; // total encrypted length = SUM length chunks

	@Basic(fetch = FetchType.EAGER)
	@Column(nullable = false)
	@Lob
	private byte[] encryptionContext; // sender input to encryption scheme

	@Column(nullable = false)
	private long plaintextLength; // total length of plaintext ( unencrypted, unzipped )

	@Column(length = MAX_CRCMANIFEST_LEN, nullable = false)
	private String chunksCRC; // manifest of checksums for each chunk.

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	Message() {
	}

	public Message(String msgId, Date txTS) {
		this.msgId = msgId;
		this.txTS = txTS;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMsgId() {
		return msgId;
	}

	public Date getTxTS() {
		return txTS;
	}

	public Date getLiveUntilTS() {
		return liveUntilTS;
	}

	public void setLiveUntilTS(Date liveUntilTS) {
		this.liveUntilTS = liveUntilTS;
	}

	public Long getFlowId() {
		return flowId;
	}

	public void setFlowId(Long flowId) {
		this.flowId = flowId;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public void setExternalReference(String externalReference) {
		this.externalReference = externalReference;
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
