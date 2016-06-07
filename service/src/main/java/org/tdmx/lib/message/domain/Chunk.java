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

/**
 * A Chunk (part of a Message).
 * 
 * @author Peter Klauser
 * 
 */
public class Chunk implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_MSGID_LEN = 64;

	public static final int MAX_SHA1_MAC_LEN = 40;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	// TODO #107 denormalize ttl from msg, partitioned datasource

	// -------------------------------------------------------------------------
	// HEADER FIELDS
	// -------------------------------------------------------------------------
	// @Column(length = MAX_MSGID_LEN, nullable = false)
	private String msgId;

	// @Column(nullable = false)
	private int pos;

	// @Column(nullable = false)
	// @Temporal(TemporalType.TIMESTAMP)
	private Date ttlTimestamp; // max time to keep the data (un relayed)

	// Column(length=MAX_SHA1_MAC_LEN, nullable = false)
	private String mac;

	// @Basic(fetch = FetchType.LAZY)
	// @Column(nullable = false)
	// @Lob
	private byte[] data;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	Chunk() {
	}

	public Chunk(String msgId, int pos) {
		this.msgId = msgId;
		this.pos = pos;
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

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getMsgId() {
		return msgId;
	}

	public int getPos() {
		return pos;
	}

	public Date getTtlTimestamp() {
		return ttlTimestamp;
	}

	public void setTtlTimestamp(Date ttlTimestamp) {
		this.ttlTimestamp = ttlTimestamp;
	}

}
