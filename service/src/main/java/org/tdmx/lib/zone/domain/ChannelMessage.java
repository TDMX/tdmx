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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.core.api.v01.mos.ws.MOS;
import org.tdmx.core.api.v01.mrs.ws.MRS;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;

/**
 * An ChannelMessage is a message in a Flow. The ChannelMessage "includes" the DeliveryReceipt data @see
 * {@link #getReceipt()}
 * 
 * ChannelMessages are created at by the {@link MOS#submit(org.tdmx.core.api.v01.mos.Submit)} when the Channel's
 * FlowControl permits sending on the originating side, and when relayed in with
 * {@link MRS#relay(org.tdmx.core.api.v01.mrs.Relay)} at the destination side .
 * 
 * The origin and destination UserCertificate's serial numbers are denormalized here to allow more efficient selecting
 * messages or delivery receipts for specific users at either end.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "ChannelMessage")
public class ChannelMessage implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_MSGID_LEN = 64;
	public static final int MAX_SHA256_MAC_LEN = 80;
	public static final int MAX_EXTREF_LEN = 256;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelMessageIdGen")
	@TableGenerator(name = "ChannelMessageIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "channelmessageObjectId", valueColumnName = "value", allocationSize = 10)
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
	private Date ttlTimestamp;

	/**
	 * The delivery receipt double's as the receiver cert.
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "signatureDate", column = @Column(name = "receiverSignatureDate", nullable = true) ),
			@AttributeOverride(name = "certificateChainPem", column = @Column(name = "receiverPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN, nullable = false) ),
			@AttributeOverride(name = "value", column = @Column(name = "receiverSignature", length = AgentSignature.MAX_SIGNATURE_LEN, nullable = true) ),
			@AttributeOverride(name = "algorithm", column = @Column(name = "receiverSignatureAlgorithm", length = AgentSignature.MAX_SIG_ALG_LEN, nullable = true) ) })
	private AgentSignature receipt;

	@Column(length = DestinationSession.MAX_IDENTIFIER_LEN, nullable = false)
	private String encryptionContextId;

	@Enumerated(EnumType.STRING)
	@Column(length = DestinationSession.MAX_SCHEME_LEN, nullable = false)
	private IntegratedCryptoScheme scheme;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "signatureDate", column = @Column(name = "senderSignatureDate", nullable = false) ),
			@AttributeOverride(name = "certificateChainPem", column = @Column(name = "senderPem", length = AgentCredential.MAX_CERTIFICATECHAIN_LEN, nullable = false) ),
			@AttributeOverride(name = "value", column = @Column(name = "senderSignature", length = AgentSignature.MAX_SIGNATURE_LEN, nullable = false) ),
			@AttributeOverride(name = "algorithm", column = @Column(name = "senderSignatureAlgorithm", length = AgentSignature.MAX_SIG_ALG_LEN, nullable = false) ) })
	private AgentSignature signature;

	@Column(length = MAX_EXTREF_LEN)
	private String externalReference;

	// -------------------------------------------------------------------------
	// PAYLOAD FIELDS
	// -------------------------------------------------------------------------

	/**
	 * Total encrypted length = SUM length chunks
	 */
	@Column(nullable = false)
	private long payloadLength;

	/**
	 * Sender input to encryption scheme.
	 */
	@Basic(fetch = FetchType.EAGER)
	@Column(nullable = false)
	@Lob
	private byte[] encryptionContext;

	/**
	 * Total length of plaintext ( unencrypted, unzipped )
	 */
	@Column(nullable = false)
	private long plaintextLength;

	/**
	 * The SHA256 MAC of all chunk MACs in order.
	 */
	@Column(length = MAX_SHA256_MAC_LEN, nullable = false)
	private String macOfMacs;

	// -------------------------------------------------------------------------
	// CONTROL FIELDS
	// -------------------------------------------------------------------------

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "status", column = @Column(name = "deliveryStatus", length = DeliveryStatus.MAX_DELIVERYSTATUS_LEN, nullable = false) ),
			@AttributeOverride(name = "timestamp", column = @Column(name = "deliveryTimestamp", nullable = false) ),
			@AttributeOverride(name = "errorCode", column = @Column(name = "deliveryErrorCode") ),
			@AttributeOverride(name = "errorMessage", column = @Column(name = "deliveryErrorMessage", length = ProcessingState.MAX_ERRORMESSAGE_LEN) ) })
	private DeliveryState deliveryState = DeliveryState.ready();

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "txId", column = @Column(name = "txId", length = TransactionState.MAX_XA_TXID_LEN, nullable = true) ),
			@AttributeOverride(name = "txTimeoutTimestamp", column = @Column(name = "txTimeoutTimestamp", nullable = true) ),
			@AttributeOverride(name = "deliveryCount", column = @Column(name = "deliveryCount", nullable = false) ) })
	private TransactionState txState = TransactionState.none();

	// TODO #97: rename to "relayState"
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "status", column = @Column(name = "processingStatus", length = ProcessingStatus.MAX_PROCESSINGSTATUS_LEN, nullable = false) ),
			@AttributeOverride(name = "timestamp", column = @Column(name = "processingTimestamp", nullable = false) ),
			@AttributeOverride(name = "errorCode", column = @Column(name = "processingErrorCode", nullable = true) ),
			@AttributeOverride(name = "errorMessage", column = @Column(name = "processingErrorMessage", length = ProcessingState.MAX_ERRORMESSAGE_LEN) ) })
	private ProcessingState processingState = ProcessingState.none();

	@Column(nullable = false)
	private int originSerialNr = -1;

	@Column(nullable = false)
	private int destinationSerialNr = -1;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ChannelMessage() {
	}

	public ChannelMessage(Channel channel, ChannelMessage other) {
		this.channel = channel;
		// header fields
		this.msgId = other.getMsgId();
		this.ttlTimestamp = other.getTtlTimestamp();
		this.receipt = other.getReceipt();
		this.encryptionContextId = other.getEncryptionContextId();
		this.scheme = other.getScheme();
		this.signature = other.getSignature();
		this.externalReference = other.getExternalReference();
		// payload fields
		this.payloadLength = other.getPayloadLength();
		this.encryptionContext = other.getEncryptionContext();
		this.plaintextLength = other.getPlaintextLength();
		this.macOfMacs = other.getMacOfMacs();
		// control fields
		this.processingState = other.getProcessingState();
		this.originSerialNr = other.getOriginSerialNr();
		this.destinationSerialNr = other.getDestinationSerialNr();
		this.deliveryState = other.getDeliveryState();
		this.txState = other.getTxState();
	}
	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelMessage [id=");
		builder.append(id);
		builder.append(" msgId=").append(msgId);
		builder.append(" ttlTimestamp=").append(ttlTimestamp);
		builder.append(" scheme=").append(scheme);
		if (signature != null) {
			builder.append(" sentAt=").append(signature.getSignatureDate());
		}
		builder.append(" payloadLength=").append(payloadLength);
		if (receipt != null) {
			builder.append(" receivedAt=").append(receipt.getSignatureDate());
		}
		builder.append(" deliveryState=").append(deliveryState);
		builder.append(" txState=").append(txState);
		builder.append(" processingState=").append(processingState);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Return the DR contained in this ChannelMessage.
	 * 
	 * @return the DR contained in this ChannelMessage.
	 */
	public DeliveryReceipt getDeliveryReport() {
		DeliveryReceipt dr = new DeliveryReceipt();
		dr.setMsgId(msgId);
		dr.setReceiverSignature(receipt);
		dr.setSenderSignature(signature);
		// TODO #97: error
		dr.setExternalReference(externalReference);
		return dr;
	}

	/**
	 * Return the number of Chunks = ( payloadSize / chunkSize ) + 1
	 * 
	 * @return the number of Chunks = ( payloadSize / chunkSize ) + 1
	 */
	public int getNumberOfChunks() {
		return (int) (1 + (payloadLength / scheme.getChunkSize()));
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

	public Date getTtlTimestamp() {
		return ttlTimestamp;
	}

	public void setTtlTimestamp(Date ttlTimestamp) {
		this.ttlTimestamp = ttlTimestamp;
	}

	public IntegratedCryptoScheme getScheme() {
		return scheme;
	}

	public void setScheme(IntegratedCryptoScheme scheme) {
		this.scheme = scheme;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public void setExternalReference(String externalReference) {
		this.externalReference = externalReference;
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

	public String getEncryptionContextId() {
		return encryptionContextId;
	}

	public void setEncryptionContextId(String encryptionContextId) {
		this.encryptionContextId = encryptionContextId;
	}

	public AgentSignature getSignature() {
		return signature;
	}

	public void setSignature(AgentSignature signature) {
		this.signature = signature;
	}

	public AgentSignature getReceipt() {
		return receipt;
	}

	public void setReceipt(AgentSignature receipt) {
		this.receipt = receipt;
	}

	public String getMacOfMacs() {
		return macOfMacs;
	}

	public void setMacOfMacs(String macOfMacs) {
		this.macOfMacs = macOfMacs;
	}

	public DeliveryState getDeliveryState() {
		return deliveryState;
	}

	public void setDeliveryState(DeliveryState deliveryState) {
		this.deliveryState = deliveryState;
	}

	public TransactionState getTxState() {
		if (txState == null) {
			// needed because we don't have a mandatory field - and it's nulled by hibernate
			txState = TransactionState.none();
		}
		return txState;
	}

	public void setTxState(TransactionState txState) {
		this.txState = txState;
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}

	public void setProcessingState(ProcessingState processingState) {
		this.processingState = processingState;
	}

	public int getOriginSerialNr() {
		return originSerialNr;
	}

	public void setOriginSerialNr(int originSerialNr) {
		this.originSerialNr = originSerialNr;
	}

	public int getDestinationSerialNr() {
		return destinationSerialNr;
	}

	public void setDestinationSerialNr(int destinationSerialNr) {
		this.destinationSerialNr = destinationSerialNr;
	}

}
