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

import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.core.system.lang.JaxbMarshaller;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.Job;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "controlJob")
@XmlType(name = "ControlJob")
public class ControlJobResource {

	public enum FIELD {
		ID("id"),
		SCHEDULEDTIME("scheduledTime"),
		STATUS("status"),
		JOBID("jobId"),
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

	private Long id;
	private Date scheduledTime;
	private String status;
	private String jobId;
	private String type;
	private Date startTimestamp;
	private Date endTimestamp;
	private String data;
	private String exception;

	public String getCliRepresentation() {
		StringBuilder buf = new StringBuilder();
		buf.append("ControlJob");
		buf.append("; ").append(id);
		buf.append("; ").append(scheduledTime);
		buf.append("; ").append(status);
		buf.append("; ").append(jobId);
		buf.append("; ").append(type);
		buf.append("; ").append(startTimestamp);
		buf.append("; ").append(endTimestamp);
		buf.append("; ").append(data);
		buf.append("; ").append(exception);
		return buf.toString();
	}

	public static ControlJob mapTo(ControlJobResource controlJob) {
		if (controlJob == null) {
			return null;
		}
		Job job = new Job();
		job.setJobId(controlJob.getJobId());
		job.setType(controlJob.getType());
		job.setData(StringUtils.asBytes(controlJob.getData(), JaxbMarshaller.DEFAULT_ENCODING));
		job.setException(StringUtils.asBytes(controlJob.getException(), JaxbMarshaller.DEFAULT_ENCODING));
		job.setStartTimestamp(controlJob.getStartTimestamp());
		job.setEndTimestamp(controlJob.getEndTimestamp());

		ControlJob j = new ControlJob();
		j.setId(controlJob.getId());
		j.setStatus(EnumUtils.mapTo(ControlJobStatus.class, controlJob.getStatus()));
		j.setJob(job);

		return j;
	}

	public static ControlJobResource mapTo(ControlJob controlJob) {
		if (controlJob == null) {
			return null;
		}
		ControlJobResource j = new ControlJobResource();
		j.setId(controlJob.getId());
		j.setStatus(EnumUtils.mapToString(controlJob.getStatus()));

		Job job = controlJob.getJob();

		j.setJobId(job.getJobId());
		j.setType(job.getType());
		j.setData(StringUtils.asString(job.getData(), JaxbMarshaller.DEFAULT_ENCODING));
		j.setException(StringUtils.asString(job.getException(), JaxbMarshaller.DEFAULT_ENCODING));
		j.setStartTimestamp(job.getStartTimestamp());
		j.setEndTimestamp(job.getEndTimestamp());
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

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
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
