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
 * The SearchCriteria for ChannelMessageState.
 * 
 * @author Peter Klauser
 * 
 */
public class MessageStatusSearchCriteria {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final PageSpecifier pageSpecifier;

	/**
	 * Specify the each individual field of of the ChannelOrigin to search for.
	 */
	private final ChannelOrigin origin = new ChannelOrigin();

	/**
	 * Specify the each individual field of of the ChannelDestination to search for.
	 */
	private final ChannelDestination destination = new ChannelDestination();

	/**
	 * The message status.
	 */
	private MessageStatus messageStatus;

	/**
	 * The processing state.
	 */
	private ProcessingStatus processingStatus;

	/**
	 * The XA xid.
	 */
	private String xid;

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
	public MessageStatusSearchCriteria(PageSpecifier pageSpecifier) {
		this.pageSpecifier = pageSpecifier;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public PageSpecifier getPageSpecifier() {
		return pageSpecifier;
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

	public ChannelOrigin getOrigin() {
		return origin;
	}

	public ChannelDestination getDestination() {
		return destination;
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

	public MessageStatus getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(MessageStatus messageStatus) {
		this.messageStatus = messageStatus;
	}

	public String getXid() {
		return xid;
	}

	public void setXid(String xid) {
		this.xid = xid;
	}

}
