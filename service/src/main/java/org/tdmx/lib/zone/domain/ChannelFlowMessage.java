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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdmx.core.api.v01.mos.ws.MOS;
import org.tdmx.lib.message.domain.Message;

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

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	// TODO "Relay" Processingstatus of flowcontrolstatus

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ChannelFlowMessageIdGen")
	@TableGenerator(name = "ChannelFlowMessageIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "channelflowmessageObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private ChannelFlowOrigin flowOrigin;

	@Column(length = Message.MAX_MSGID_LEN, nullable = false)
	private String msgId;

	// TODO private String txId;

	@Column(nullable = false)
	private long payloadSize;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date sentTimestamp;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date ttlTimestamp;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	ChannelFlowMessage() {
	}

	public ChannelFlowMessage(ChannelFlowOrigin flow, MessageDescriptor md) {
		setFlowOrigin(flow);

		if (md != null) {
			setMsgId(md.getMsgId());
			setSentTimestamp(md.getSentTimestamp());
			setTtlTimestamp(md.getTtlTimestamp());
			setPayloadSize(md.getPayloadSize());
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public ChannelFlowOrigin getFlowOrigin() {
		return flowOrigin;
	}

	public void setFlowOrigin(ChannelFlowOrigin flowOrigin) {
		this.flowOrigin = flowOrigin;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelFlowMsg [id=");
		builder.append(id);
		builder.append(" msgId=").append(msgId);
		builder.append(" sentTimestamp=").append(sentTimestamp);
		builder.append(" ttlTimestamp=").append(ttlTimestamp);
		builder.append(" payloadSize=").append(payloadSize);
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

	public long getPayloadSize() {
		return payloadSize;
	}

	public void setPayloadSize(long payloadSize) {
		this.payloadSize = payloadSize;
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
