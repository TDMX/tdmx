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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * An FlowTargetConcurrency is separated as it's own instance from the FlowTarget so that it can be updated with a
 * higher frequency without incuring the penalty of the data of the FlowTarget not changing fast.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "FlowTargetConcurrency")
public class FlowTargetConcurrency implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -1L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "FlowTargetConcurrencyIdGen")
	@TableGenerator(name = "FlowTargetConcurrencyIdGen", table = "MaxValueEntry", pkColumnName = "NAME", pkColumnValue = "zoneObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	@OneToOne(optional = false, fetch = FetchType.LAZY, mappedBy = "concurrency")
	private FlowTarget flowTarget;

	@Column(nullable = false)
	private int concurrencyLimit;

	@Column(nullable = false)
	private int concurrencyLevel;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	FlowTargetConcurrency() {
	}

	public FlowTargetConcurrency(FlowTarget flowTarget, Service service) {
		setFlowTarget(flowTarget);
		this.concurrencyLimit = service.getConcurrencyLimit();
	}

	public FlowTargetConcurrency(FlowTarget flowTarget, FlowTargetConcurrency other) {
		setFlowTarget(flowTarget);
		setConcurrencyLimit(other.getConcurrencyLimit());
		setConcurrencyLevel(other.getConcurrencyLevel());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FlowTargetConcurrency [id=");
		builder.append(id);
		builder.append(", concurrencyLimit=").append(concurrencyLimit);
		builder.append(", concurrencyLevel=").append(concurrencyLevel);
		builder.append("]");
		return builder.toString();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setFlowTarget(FlowTarget flowTarget) {
		this.flowTarget = flowTarget;
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

	public int getConcurrencyLimit() {
		return concurrencyLimit;
	}

	public void setConcurrencyLimit(int concurrencyLimit) {
		this.concurrencyLimit = concurrencyLimit;
	}

	public int getConcurrencyLevel() {
		return concurrencyLevel;
	}

	public void setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}

	public FlowTarget getFlowTarget() {
		return flowTarget;
	}

}
