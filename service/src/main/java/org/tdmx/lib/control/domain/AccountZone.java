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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * An AccountZone describes a Zone at a ServiceProvider and points to the Zone's DatabasePartition.
 * 
 * The ServiceProvider may control the authorization state of a Zone independently of the Account's authorization state.
 * 
 * @author Peter Klauser
 * 
 */
@Entity
@Table(name = "AccountZone")
public class AccountZone implements Serializable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final int MAX_ZONEAPEX_LEN = 255;
	public static final int MAX_SEGMENT_LEN = 16;

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "AccountZoneIdGen")
	@TableGenerator(name = "AccountZoneIdGen", table = "PrimaryKeyGen", pkColumnName = "NAME", pkColumnValue = "controlObjectId", valueColumnName = "value", allocationSize = 10)
	private Long id;

	// TODO index zoneApex for agent authentication service
	@Column(length = MAX_ZONEAPEX_LEN, nullable = false, unique = true)
	private String zoneApex;

	// TODO separate status into AccessStatus and ProvisioningStatus
	@Enumerated(EnumType.STRING)
	@Column(length = AccountZoneStatus.MAX_ACCOUNTZONESTATUS_LEN, nullable = false)
	private AccountZoneStatus status;

	@Column(length = Account.MAX_ACCOUNTID_LEN, nullable = false)
	private String accountId;

	/**
	 * The segment is a secondary partitioning criteria like "premium" or "free" tier.
	 */
	@Column(length = MAX_SEGMENT_LEN, nullable = false)
	private String segment;

	/**
	 * Each zone is assigned into a DatabasePartition at creation time.
	 */
	@Column(length = DatabasePartition.MAX_PARTITIONID_LEN, nullable = false)
	private String zonePartitionId;

	/**
	 * If a Job is pending on the Account's Zone (like initialization, transfer ) then this is the link to the jobId.
	 * The Job is responsible to remove this record once it has completed.
	 */
	@Column
	private Long jobId;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AccountZone [id=");
		builder.append(id);
		builder.append(", accountId=");
		builder.append(accountId);
		builder.append(", zoneApex=");
		builder.append(zoneApex);
		builder.append(", segment=");
		builder.append(segment);
		builder.append(", zonePartitionId=");
		builder.append(zonePartitionId);
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

	public String getZoneApex() {
		return zoneApex;
	}

	public void setZoneApex(String zoneApex) {
		this.zoneApex = zoneApex;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public AccountZoneStatus getStatus() {
		return status;
	}

	public void setStatus(AccountZoneStatus status) {
		this.status = status;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public String getZonePartitionId() {
		return zonePartitionId;
	}

	public void setZonePartitionId(String zonePartitionId) {
		this.zonePartitionId = zonePartitionId;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

}
