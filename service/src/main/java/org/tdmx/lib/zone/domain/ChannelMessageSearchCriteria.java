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

import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ProcessingStatus;

/**
 * The SearchCriteria for ChannelMessages (including their delivery reports).
 * 
 * @author Peter Klauser
 * 
 */
public class ChannelMessageSearchCriteria extends ChannelSearchCriteria {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	/**
	 * The messageId
	 */
	private String msgId;

	/**
	 * Find by owning Channel
	 */
	private Channel channel;

	private Boolean received;

	/**
	 * Origin side, processingStatus relates to relaying out the message or delivery of the receipt depending on whether
	 * the message has a receipt timestamp or not.
	 * 
	 * Destination side, processingStatus relates to delivery of the message or relay out of the recept depending on
	 * whether the message has a recept timestamp or not.
	 */
	private ProcessingStatus processingStatus;

	/**
	 * To select delivery reports for delivery to a specific origin.
	 */
	private Integer originSerialNr;

	/**
	 * To select messages for delivery to a specific destination.
	 */
	private Integer destinationSerialNr;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public ChannelMessageSearchCriteria(PageSpecifier pageSpecifier) {
		super(pageSpecifier);
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

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public Boolean getReceived() {
		return received;
	}

	public void setReceived(Boolean received) {
		this.received = received;
	}

	public ProcessingStatus getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(ProcessingStatus processingStatus) {
		this.processingStatus = processingStatus;
	}

	public Integer getOriginSerialNr() {
		return originSerialNr;
	}

	public void setOriginSerialNr(Integer originSerialNr) {
		this.originSerialNr = originSerialNr;
	}

	public Integer getDestinationSerialNr() {
		return destinationSerialNr;
	}

	public void setDestinationSerialNr(Integer destinationSerialNr) {
		this.destinationSerialNr = destinationSerialNr;
	}

}
