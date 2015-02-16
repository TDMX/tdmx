package org.tdmx.server.rs.sas;

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
