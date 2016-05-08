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
package org.tdmx.server.rs.sas.resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tdmx.core.cli.display.annotation.CliAttribute;
import org.tdmx.core.cli.display.annotation.CliRepresentation;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.server.cache.CacheInvalidationInstruction;

@CliRepresentation(name = "SegmentCacheInvalidationStatus")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "cacheInvalidationStatus")
@XmlType(name = "SegmentCacheInvalidationStatus")
public class SegmentCacheInvalidationStatusValue {

	public enum FIELD {
		SEGMENTREF("segmentRef"),
		INSTRUCTION("intruction"),
		PROCESSING_STATE("ps");

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	@CliAttribute(order = 0)
	private SegmentReference segmentRef;
	@CliAttribute(order = 1, verbose = true)
	private CacheInvalidationInstructionValue instruction;
	@CliAttribute(order = 2)
	private ProcessingStateValue ps;

	public static SegmentCacheInvalidationStatusValue mapFrom(Segment otherSeg,
			CacheInvalidationInstruction otherInstruction, ProcessingState otherPS) {
		if (otherSeg == null || otherInstruction == null || otherPS == null) {
			return null;
		}
		SegmentCacheInvalidationStatusValue r = new SegmentCacheInvalidationStatusValue();
		r.setSegmentRef(SegmentReference.referenceFrom(otherSeg));
		r.setInstruction(CacheInvalidationInstructionValue.mapFrom(otherInstruction));
		r.setPs(ProcessingStateValue.mapFrom(otherPS));
		return r;
	}

	public SegmentReference getSegmentRef() {
		return segmentRef;
	}

	public void setSegmentRef(SegmentReference segmentRef) {
		this.segmentRef = segmentRef;
	}

	public ProcessingStateValue getPs() {
		return ps;
	}

	public void setPs(ProcessingStateValue ps) {
		this.ps = ps;
	}

	public CacheInvalidationInstructionValue getInstruction() {
		return instruction;
	}

	public void setInstruction(CacheInvalidationInstructionValue instruction) {
		this.instruction = instruction;
	}

}
