package org.tdmx.lib.zone.service;

import org.tdmx.lib.control.datasource.PartitionIdProvider;

public class FixedZonePartitionIdProvider implements PartitionIdProvider {

	private String zonePartitionId;

	public void setZonePartitionId(String zonePartitionId) {
		this.zonePartitionId = zonePartitionId;
	}

	@Override
	public String getPartitionId() {
		return zonePartitionId;
	}

}
