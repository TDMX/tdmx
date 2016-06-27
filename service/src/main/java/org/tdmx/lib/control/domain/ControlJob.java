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
package org.tdmx.lib.control.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.job.JobPropertyName;

/**
 * An ControlJob is a Job scheduled for execution at some time in a specific segment.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "ControlJob")
public class ControlJob implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final int MAX_DATA_LEN = 16000;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -128859602084626282L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * The segment is a secondary partitioning criteria like "premium" or "free" tier.
	 */
	@Column(length = Segment.MAX_SEGMENT_LEN, nullable = false)
	private String segment;

	// TODO DB: index on segment, scheduledTime
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date scheduledTime;

	@Enumerated(EnumType.STRING)
	@Column(length = ControlJobStatus.MAX_JOBSTATUS_LEN, nullable = false)
	private ControlJobStatus status;

	@Enumerated(EnumType.STRING)
	@Column(length = ControlJobType.MAX_JOBTYPE_LEN, nullable = false)
	private ControlJobType type;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTimestamp;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTimestamp;

	/**
	 * A CSV or name,value pairs.
	 */
	@Column(length = MAX_DATA_LEN)
	private String data;

	@Column(length = MAX_DATA_LEN)
	private String exception;

	@Column
	private Long parentJobId; // job to be continued when this one finishes.

	// TODO index type + owningEntityId
	@Column
	private Long owningEntityId; // entityId which owns the job

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public static ControlJob createWaitJob(String segment) {
		ControlJob j = new ControlJob();
		j.setType(ControlJobType.WAIT);
		j.setSegment(segment);
		j.setStatus(ControlJobStatus.NEW);
		return j;
	}

	public static ControlJob createZoneTransferJob(String segment, Long accountZoneId, String newZonePartitionId) {
		ControlJob j = new ControlJob();
		j.setType(ControlJobType.TRANSFER_ZONE);
		j.setSegment(segment);
		j.setStatus(ControlJobStatus.NEW);
		j.setLongProperty(JobPropertyName.ACCOUNT_ZONE_ID, accountZoneId);
		j.setStringProperty(JobPropertyName.NEW_ZONE_PARTITION_ID, newZonePartitionId);
		return j;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ControlJob [id=");
		builder.append(id);
		builder.append(", status=").append(status);
		builder.append(", segment=").append(segment);
		builder.append(", scheduledTime=").append(scheduledTime);
		builder.append(", parentJobId=").append(parentJobId);
		builder.append(", owningEntityId=").append(owningEntityId);
		builder.append("]");
		return builder.toString();
	}

	public void setException(Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append(t.getMessage());
		sb.append(StringUtils.getLF());
		sb.append(t.getClass().getName());
		sb.append(StringUtils.getLF());
		StackTraceElement[] st = t.getStackTrace();
		for (StackTraceElement e : st) {
			sb.append(e.toString()).append(StringUtils.getLF());
		}
		setException(sb.toString());
	}

	public ControlJob withParentJob(ControlJob parent) {
		if (parent == null || parent.getId() == null) {
			throw new IllegalArgumentException("parent must have an id");
		}
		setParentJobId(parent.getId());
		return this;
	}

	public ControlJob withOwningEntityId(Long entityId) {
		if (entityId == null) {
			throw new IllegalArgumentException("entityId not be null");
		}
		setOwningEntityId(entityId);
		return this;
	}

	public ControlJob scheduleNow() {
		setScheduledTime(new Date());
		return this;
	}

	public ControlJob scheduledAt(Date scheduledDate) {
		setScheduledTime(scheduledDate);
		return this;
	}

	public Long getLongProperty(String propertyName) {
		Map<String, String> map = StringUtils.getPropertyMap(data);
		String value = map.get(propertyName);
		return value != null ? Long.parseLong(value) : null;
	}

	public void setLongProperty(String propertyName, Long value) {
		Map<String, String> map = StringUtils.getPropertyMap(data);
		map.put(propertyName, value.toString());
		data = StringUtils.convertPropertyMapToCSV(map);
	}

	public String getStringProperty(String propertyName) {
		Map<String, String> map = StringUtils.getPropertyMap(data);
		return map.get(propertyName);
	}

	public void setStringProperty(String propertyName, String value) {
		Map<String, String> map = StringUtils.getPropertyMap(data);
		map.put(propertyName, value);
		data = StringUtils.convertPropertyMapToCSV(map);
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

	public Date getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(Date scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public ControlJobStatus getStatus() {
		return status;
	}

	public void setStatus(ControlJobStatus status) {
		this.status = status;
	}

	public ControlJobType getType() {
		return type;
	}

	public void setType(ControlJobType type) {
		this.type = type;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public Long getParentJobId() {
		return parentJobId;
	}

	public void setParentJobId(Long parentJobId) {
		this.parentJobId = parentJobId;
	}

	public Long getOwningEntityId() {
		return owningEntityId;
	}

	public void setOwningEntityId(Long owningEntityId) {
		this.owningEntityId = owningEntityId;
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
		this.data = StringUtils.truncateToMaxLen(data, MAX_DATA_LEN);
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = StringUtils.truncateToMaxLen(exception, MAX_DATA_LEN);
	}

}
