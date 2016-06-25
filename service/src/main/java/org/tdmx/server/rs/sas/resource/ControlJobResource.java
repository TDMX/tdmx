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

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tdmx.core.cli.display.annotation.CliAttribute;
import org.tdmx.core.cli.display.annotation.CliRepresentation;
import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobStatus;
import org.tdmx.lib.control.domain.ControlJobType;

@CliRepresentation(name = "ControlJob")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "controlJob")
@XmlType(name = "ControlJob")
public class ControlJobResource {

	public enum FIELD {
		ID("id"),
		SCHEDULEDTIME("scheduledTime"),
		STATUS("status"),
		TYPE("type"),
		STARTTIMESTAMP("startTimestamp"),
		ENDTIMESTAMP("endTimestamp"),
		DATA("data"),
		EXCEPTION("exception");

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	@CliAttribute(order = 0, verbose = true)
	private Long id;
	@CliAttribute(order = 2)
	private Date scheduledTime;
	@CliAttribute(order = 3)
	private String status;
	@CliAttribute(order = 1)
	private String type;
	@CliAttribute(order = 4)
	private Date startTimestamp;
	@CliAttribute(order = 5)
	private Date endTimestamp;
	@CliAttribute(order = 6, verbose = true)
	private String data;
	@CliAttribute(order = 7)
	private String exception;

	public static ControlJob mapTo(ControlJobResource controlJob) {
		if (controlJob == null) {
			return null;
		}

		ControlJob j = new ControlJob();
		j.setId(controlJob.getId());
		j.setStatus(EnumUtils.mapTo(ControlJobStatus.class, controlJob.getStatus()));
		j.setType(EnumUtils.mapTo(ControlJobType.class, controlJob.getType()));
		j.setData(controlJob.getData());
		j.setException(controlJob.getException());
		j.setStartTimestamp(controlJob.getStartTimestamp());
		j.setEndTimestamp(controlJob.getEndTimestamp());

		return j;
	}

	public static ControlJobResource mapTo(ControlJob controlJob) {
		if (controlJob == null) {
			return null;
		}
		ControlJobResource j = new ControlJobResource();
		j.setId(controlJob.getId());
		j.setStatus(EnumUtils.mapToString(controlJob.getStatus()));
		j.setType(EnumUtils.mapToString(controlJob.getType()));
		j.setData(controlJob.getData());
		j.setException(controlJob.getException());
		j.setStartTimestamp(controlJob.getStartTimestamp());
		j.setEndTimestamp(controlJob.getEndTimestamp());
		return j;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(Date scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public Date getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

}
