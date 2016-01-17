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
package org.tdmx.server.ros;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;
import org.tdmx.lib.zone.service.ChannelService;
import org.tdmx.lib.zone.service.DomainService;
import org.tdmx.lib.zone.service.ZoneService;

/**
 * Handles relay data.
 * 
 * @author Peter
 *
 */
public class RelayDataServiceImpl implements RelayDataService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(RelayDataServiceImpl.class);

	private AccountZoneService accountZoneService;
	private ThreadLocalPartitionIdProvider partitionIdProvider;

	private ZoneService zoneService;
	private DomainService domainService;
	private ChannelService channelService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public AccountZone getAccountZone(Long accountZoneId) {
		return accountZoneId != null ? accountZoneService.findById(accountZoneId) : null;
	}

	@Override
	public Zone getZone(AccountZone az, Long zoneId) {
		if (az == null) {
			return null;
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			return zoneId != null ? zoneService.findById(zoneId) : null;
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public Domain getDomain(AccountZone az, Zone z, Long domainId) {
		if (az == null || z == null) {
			return null;
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			return domainId != null ? domainService.findById(domainId) : null;
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public Channel getChannel(AccountZone az, Zone z, Domain d, Long channelId) {
		if (az == null || z == null || d == null) {
			return null;
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			return channelId != null ? channelService.findById(channelId, true, true) : null;
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public ChannelMessage getMessage(AccountZone az, Zone z, Domain d, Channel channel, Long msgId) {
		if (az == null || z == null || d == null || channel == null) {
			return null;
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			return msgId != null ? channelService.findByMessageId(msgId) : null;
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public List<ChannelMessage> getRelayMessages(AccountZone az, Zone z, Domain d, Channel channel, int maxMsg) {
		// TODO #93 - fetch up to maxMsg pending channel messages.
		return null;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------
	protected void associateZoneDB(String zoneDbPartitionId) {
		partitionIdProvider.setPartitionId(zoneDbPartitionId);
	}

	protected void disassociateZoneDB() {
		partitionIdProvider.clearPartitionId();
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public AccountZoneService getAccountZoneService() {
		return accountZoneService;
	}

	public void setAccountZoneService(AccountZoneService accountZoneService) {
		this.accountZoneService = accountZoneService;
	}

	public ThreadLocalPartitionIdProvider getPartitionIdProvider() {
		return partitionIdProvider;
	}

	public void setPartitionIdProvider(ThreadLocalPartitionIdProvider partitionIdProvider) {
		this.partitionIdProvider = partitionIdProvider;
	}

	public ZoneService getZoneService() {
		return zoneService;
	}

	public void setZoneService(ZoneService zoneService) {
		this.zoneService = zoneService;
	}

	public DomainService getDomainService() {
		return domainService;
	}

	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}

	public ChannelService getChannelService() {
		return channelService;
	}

	public void setChannelService(ChannelService channelService) {
		this.channelService = channelService;
	}

}
