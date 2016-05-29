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

/**
 * A DeliveryReceipt of a ChannelMessage.
 * 
 * @author Peter Klauser
 * 
 */
public class DeliveryReceipt implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	private String msgId;

	private String externalReference;

	private AgentSignature senderSignature;

	private AgentSignature receiverSignature;

	private Integer deliveryErrorCode;

	private String deliveryErrorMessage;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public DeliveryReceipt() {

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeliveryReport [");
		builder.append("msgId=").append(msgId);
		builder.append(", externalReference=").append(externalReference);
		if (senderSignature != null && senderSignature.getSignatureDate() != null) {
			builder.append(", sentAt=").append(senderSignature.getSignatureDate());
		}
		if (receiverSignature != null && receiverSignature.getSignatureDate() != null) {
			builder.append(", receivedAt=").append(receiverSignature.getSignatureDate());
		}
		if (deliveryErrorCode != null) {
			builder.append(", deliveryErrorCode=").append(deliveryErrorCode);
		}
		if (deliveryErrorMessage != null) {
			builder.append(", deliveryErrorMessage=").append(deliveryErrorMessage);
		}
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

	public String getExternalReference() {
		return externalReference;
	}

	public void setExternalReference(String externalReference) {
		this.externalReference = externalReference;
	}

	public AgentSignature getSenderSignature() {
		return senderSignature;
	}

	public void setSenderSignature(AgentSignature senderSignature) {
		this.senderSignature = senderSignature;
	}

	public AgentSignature getReceiverSignature() {
		return receiverSignature;
	}

	public void setReceiverSignature(AgentSignature receiverSignature) {
		this.receiverSignature = receiverSignature;
	}

	public Integer getDeliveryErrorCode() {
		return deliveryErrorCode;
	}

	public void setDeliveryErrorCode(Integer deliveryErrorCode) {
		this.deliveryErrorCode = deliveryErrorCode;
	}

	public String getDeliveryErrorMessage() {
		return deliveryErrorMessage;
	}

	public void setDeliveryErrorMessage(String deliveryErrorMessage) {
		this.deliveryErrorMessage = deliveryErrorMessage;
	}

}
