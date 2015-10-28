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

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "accountzone")
@XmlType(name = "AccountZone")
public class AccountZoneResource {

	public enum FIELD {
		ID("id"),
		ACCOUNTID("accountId"),
		ZONEAPEX("zoneApex"),
		SEGMENT("segment"),
		ZONEPARTITIONID("zonePartitionId"),
		ACCESSSTATUS("accessStatus"),
		JOBID("jobId"), ;

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
	private String accountId;
	private String zoneApex;
	private String segment;
	private String zonePartitionId;
	private String accessStatus;
	private Long jobId;

	public static AccountZone mapTo(AccountZoneResource az) {
		if (az == null) {
			return null;
		}
		AccountZone a = new AccountZone();
		a.setId(az.getId());
		a.setAccountId(az.getAccountId());
		a.setZoneApex(az.getZoneApex());

		a.setSegment(az.getSegment());
		a.setZonePartitionId(az.getZonePartitionId());

		a.setJobId(az.getJobId());

		if (StringUtils.hasText(az.getAccessStatus())) {
			a.setStatus(AccountZoneStatus.valueOf(az.getAccessStatus()));
		}
		return a;
	}

	public static AccountZoneResource mapTo(AccountZone az) {
		if (az == null) {
			return null;
		}
		AccountZoneResource a = new AccountZoneResource();
		a.setId(az.getId());
		a.setAccountId(az.getAccountId());
		a.setZoneApex(az.getZoneApex());

		a.setSegment(az.getSegment());
		a.setZonePartitionId(az.getZonePartitionId());

		a.setJobId(az.getJobId());
		if (az.getStatus() != null) {
			a.setAccessStatus(az.getStatus().toString());
		}
		return a;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getZoneApex() {
		return zoneApex;
	}

	public void setZoneApex(String zoneApex) {
		this.zoneApex = zoneApex;
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

	public String getAccessStatus() {
		return accessStatus;
	}

	public void setAccessStatus(String accessStatus) {
		this.accessStatus = accessStatus;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

}
