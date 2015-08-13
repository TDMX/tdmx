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
 * An ChannelName describes a Channel.
 * 
 * @author Peter Klauser
 * 
 */
public class ChannelName implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// HEADER FIELDS
	// -------------------------------------------------------------------------

	private final ChannelOrigin origin;
	private final ChannelDestination destination;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ChannelName(ChannelOrigin origin, ChannelDestination destination) {
		this.origin = origin;
		this.destination = destination;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelDescriptor [");
		builder.append(" origin=").append(origin);
		builder.append(" destination=").append(destination);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ChannelName)) {
			return false;
		}
		ChannelName other = (ChannelName) obj;
		if (destination == null) {
			if (other.destination != null) {
				return false;
			}
		} else if (!destination.equals(other.destination)) {
			return false;
		}
		if (origin == null) {
			if (other.origin != null) {
				return false;
			}
		} else if (!origin.equals(other.origin)) {
			return false;
		}
		return true;
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

}
