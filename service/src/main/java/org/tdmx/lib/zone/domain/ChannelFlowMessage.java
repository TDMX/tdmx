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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.core.api.SignatureUtils;
import org.tdmx.core.api.v01.mos.ws.MOS;

/**
 * An ChannelFlowMessage is a message in a Flow.
 * 
 * ChannelFlowMessages are created at by the {@link MOS#submit(org.tdmx.core.api.v01.mos.Submit)} when the Channel's
 * FlowControl permits sending.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "ChannelFlowMessage")
public class ChannelFlowMessage implements Serializable {

	// TODO rename ChannelMessage

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_MSGID_LEN = 64;

	public static final int MAX_ENTROPY_LEN = 8;

	public static final int MAX_SHA256_MAC_LEN = 80;

	public static final int MAX_EXTREF_LEN = 2048;

	public static final int MAX_SIGNATURE_LEN = 128;

	public static final int MAX_CRCMANIFEST_LEN = 8000;

	public static final int LEN_CONTINUATION_ID = 8;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	// TODO "Relay" Processingstatus of flowcontrolstatus

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelFlowMessageIdGen")
	@TableGenerator(name = "ChannelFlowMessageIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "channelflowmessageObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Channel channel;

	// -------------------------------------------------------------------------
	// HEADER FIELDS
	// -------------------------------------------------------------------------
	@Column(length = MAX_MSGID_LEN, nullable = false)
	private String msgId;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date sentTimestamp;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date ttlTimestamp;

	@Column(length = DestinationSession.MAX_IDENTIFIER_LEN, nullable = false)
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
	private long chunkSize; // chunkSize in Bytes

	@Column(nullable = false)
	private long payloadLength; // total encrypted length = SUM length chunks

	@Basic(fetch = FetchType.EAGER)
	@Column(nullable = false)
	@Lob
	private byte[] encryptionContext; // sender input to encryption scheme //TODO stipulate max len

	@Column(nullable = false)
	private long plaintextLength; // total length of plaintext ( unencrypted, unzipped )

	@Column(length = MAX_SHA256_MAC_LEN, nullable = false)
	private String chunksCRC; // manifest of checksums for each chunk. //TODO change to MACofMACs

	// -------------------------------------------------------------------------
	// CONTROL FIELDS
	// -------------------------------------------------------------------------

	@Column(length = MAX_ENTROPY_LEN, nullable = false)
	private byte[] entropy;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	ChannelFlowMessage() {
	}

	public ChannelFlowMessage(Channel channel, MessageDescriptor md) {
		setChannel(channel);
		// header fields
		setMsgId(md.getMsgId());
		setSentTimestamp(md.getSentTimestamp());
		setTtlTimestamp(md.getTtlTimestamp());
		setFlowSessionId(md.getEncryptionContextId());
		setPayloadSignature(md.getPayloadSignature());
		setExternalReference(md.getExternalReference());
		setHeaderSignature(md.getHeaderSignature());
		// payload fields
		setChunkSize(md.getChunkSize());
		setPayloadLength(md.getPayloadLength());
		setEncryptionContext(md.getEncryptionContext());
		setPlaintextLength(md.getPlaintextLength());
		setChunksCRC(md.getChunksCRC());

		// the entropy is read only once the cfm is instantiated.
		setEntropy(EntropySource.getRandomBytes(MAX_ENTROPY_LEN));
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Create the continuationId for the chunkPos. The continuationId is a truncated Hash of the msgId, chunkPos and the
	 * "secret" entropy which the client doesn't know so cannot create the continuationId themselves. This forces the
	 * client to have to receive the continuationId from the server before sending the next chunk.
	 * 
	 * @param chunkPos
	 * @return null if no chunk at the requested pos
	 */
	public String getContinuationId(int chunkPos) {
		// if the chunk requested starts after the end of the payload then the previous chunk is the last
		if ((getChunkSize() * chunkPos) > getPayloadLength()) {
			return null;
		}
		return SignatureUtils.createContinuationId(chunkPos, getEntropy(), getMsgId(), LEN_CONTINUATION_ID);
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelFlowMessage [id=");
		builder.append(id);
		builder.append(" msgId=").append(msgId);
		builder.append(" sentTimestamp=").append(sentTimestamp);
		builder.append(" ttlTimestamp=").append(ttlTimestamp);
		builder.append(" payloadLength=").append(payloadLength);
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setEntropy(byte[] entropy) {
		this.entropy = entropy;
	}

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

	public void setMsgId(String msgId) {
		this.msgId = msgId;
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

	public long getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(long chunkSize) {
		this.chunkSize = chunkSize;
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

	public byte[] getEntropy() {
		return entropy;
	}

}
