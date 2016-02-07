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

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.service.AccountZoneService;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelMessageSearchCriteria;
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
		if (az == null || zoneId == null) {
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
		if (az == null || z == null || domainId == null) {
			return null;
		}
		associateZoneDB(az.getZonePartitionId());
		try {

			Domain d = domainService.findById(domainId);
			d.setZone(z);
			return d;
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public Channel getChannel(AccountZone az, Zone z, Domain d, Long channelId) {
		if (az == null || z == null || d == null || channelId == null) {
			return null;
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			Channel c = channelService.findById(channelId, true, true);
			// the channel's domain is not fetched by findById
			c.setDomain(d);
			return c;
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public ChannelMessage getMessage(AccountZone az, Zone z, Domain d, Channel channel, Long msgId) {
		if (az == null || z == null || d == null || channel == null || msgId == null) {
			return null;
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			ChannelMessage msg = channelService.findByMessageId(msgId);
			msg.setChannel(channel);
			return msg;
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public void updateChannelAuthorizationProcessingState(AccountZone az, Zone z, Domain d, Long channelId,
			ProcessingState newState) {
		if (az == null || z == null || d == null || channelId == null || newState == null) {
			log.warn("Missing parameter.");
			return;
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			channelService.updateStatusChannelAuthorization(channelId, newState);
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public void updateChannelDestinationSessionProcessingState(AccountZone az, Zone z, Domain d, Long channelId,
			ProcessingState newState) {
		if (az == null || z == null || d == null || channelId == null || newState == null) {
			log.warn("Missing parameter.");
			return;
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			channelService.updateStatusDestinationSession(channelId, newState);
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public void updateChannelFlowControlProcessingState(AccountZone az, Zone z, Domain d, Long quotaId,
			ProcessingState newState) {
		if (az == null || z == null || d == null || quotaId == null || newState == null) {
			log.warn("Missing parameter.");
			return;
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			channelService.updateStatusFlowQuota(quotaId, newState);
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public List<ChannelMessage> getForwardRelayMessages(AccountZone az, Zone z, Domain d, Channel channel, int maxMsg) {
		if (az == null || z == null || d == null || channel == null) {
			log.warn("Missing parameter.");
			return Collections.emptyList();
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			ChannelMessageSearchCriteria criteria = new ChannelMessageSearchCriteria(new PageSpecifier(0, maxMsg));
			criteria.setChannel(channel);
			criteria.setReceived(false);
			criteria.setProcessingStatus(ProcessingStatus.PENDING);
			return channelService.search(z, criteria);
		} finally {
			disassociateZoneDB();
		}
	}

	@Override
	public List<ChannelMessage> getReverseRelayReceipts(AccountZone az, Zone z, Domain d, Channel channel, int maxMsg) {
		if (az == null || z == null || d == null || channel == null) {
			log.warn("Missing parameter.");
			return Collections.emptyList();
		}
		associateZoneDB(az.getZonePartitionId());
		try {
			ChannelMessageSearchCriteria criteria = new ChannelMessageSearchCriteria(new PageSpecifier(0, maxMsg));
			criteria.setChannel(channel);
			criteria.setReceived(true);
			criteria.setProcessingStatus(ProcessingStatus.PENDING);
			return channelService.search(z, criteria);
		} finally {
			disassociateZoneDB();
		}
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
