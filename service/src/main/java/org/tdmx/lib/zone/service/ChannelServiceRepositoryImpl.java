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

package org.tdmx.lib.zone.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.common.domain.ProcessingStatus;
import org.tdmx.lib.zone.dao.AgentCredentialDao;
import org.tdmx.lib.zone.dao.ChannelDao;
import org.tdmx.lib.zone.dao.DestinationDao;
import org.tdmx.lib.zone.dao.ServiceDao;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelAuthorizationStatus;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelMessageSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.DestinationSession;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.EndpointPermissionGrant;
import org.tdmx.lib.zone.domain.FlowControlStatus;
import org.tdmx.lib.zone.domain.FlowQuota;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.TemporaryChannelSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Transactional CRUD Services for Channel Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class ChannelServiceRepositoryImpl implements ChannelService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ChannelServiceRepositoryImpl.class);

	private ChannelDao channelDao;
	private DestinationDao destinationDao;
	private ServiceDao serviceDao;
	private AgentCredentialDao agentCredentialDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ZoneDB")
	public SetAuthorizationResultHolder setAuthorization(Zone zone, Domain domain, ChannelOrigin origin,
			ChannelDestination dest, ChannelAuthorization auth) {
		SetAuthorizationResultHolder resultHolder = new SetAuthorizationResultHolder();

		// lookup any existing ChannelAuthorization in the domain given the provided channel(origin+destination).
		Channel existingChannel = null;
		boolean newChannel = false;
		ChannelAuthorization existingCA = findByChannel(zone, domain, origin, dest);
		if (existingCA == null) {
			// If no existing ca - then create one with empty data.
			existingChannel = new Channel(domain, origin, dest);
			newChannel = true;
			existingCA = new ChannelAuthorization(existingChannel);
		} else {
			existingChannel = existingCA.getChannel();
		}

		if (existingChannel.isSend() && existingChannel.isRecv()) {
			// 1) setting send&recvAuth on same domain channel
			// - no requested send/recv allowed in existing ca.
			if (auth.getRecvAuthorization() == null) {
				resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING;
				return resultHolder;
			}
			if (auth.getSendAuthorization() == null) {
				resultHolder.status = SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_MISSING;
				return resultHolder;
			}
			if (auth.getRecvAuthorization().getGrant() == EndpointPermissionGrant.ALLOW) {
				// check that the Service exists
				Service service = serviceDao.loadByName(domain, existingChannel.getDestination().getServiceName());
				if (service == null) {
					resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_SERVICE_NOT_FOUND;
					return resultHolder;
				}
			}
			existingCA.setReqRecvAuthorization(null);
			existingCA.setReqSendAuthorization(null);
			existingCA.setSendAuthorization(auth.getSendAuthorization());
			existingCA.setRecvAuthorization(auth.getRecvAuthorization());
			existingCA.setLimit(auth.getLimit());
			existingCA.setProcessingState(ProcessingState.none()); // no need to relay

		} else if (existingChannel.isSend()) {
			// we must confirm any requested recvAuth if there is one, but not invent one
			if (existingCA.getReqRecvAuthorization() != null) {
				if (auth.getRecvAuthorization() == null) {
					resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING;
					return resultHolder;
				} else if (!auth.getRecvAuthorization().getSignature().getValue()
						.equals(existingCA.getReqRecvAuthorization().getSignature().getValue())) {
					resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_MISMATCH;
					return resultHolder;
				}
			} else if (auth.getRecvAuthorization() != null) {
				// and if there isn't a requestedRecvAuth we cannot provide one either
				resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_PROVIDED;
				return resultHolder;
			}
			// we are sender and there shall be no pending send authorization
			existingCA.setReqRecvAuthorization(null);
			existingCA.setReqSendAuthorization(null);
			existingCA.setRecvAuthorization(auth.getRecvAuthorization());

			// change of sendAuth vs existing sendAuth forces transfer
			if (existingCA.getSendAuthorization() == null
					|| !existingCA.getSendAuthorization().getSignature().getValue()
							.equals(auth.getSendAuthorization().getSignature().getValue())
					|| auth.getProcessingState().getStatus() != ProcessingStatus.NONE) {
				existingCA.setProcessingState(ProcessingState.pending());
			}
			existingCA.setSendAuthorization(auth.getSendAuthorization());
			existingCA.setLimit(auth.getLimit());
		} else {
			// 3) recvAuth(+confirming requested sendAuth)
			// - no reqRecvAuth allowed in existing ca.
			// we are receiver and there shall be no pending recv authorization
			// we must confirm any requested sendAuth if there is one, but not invent one
			if (auth.getRecvAuthorization().getGrant() == EndpointPermissionGrant.ALLOW) {
				// check that the Service exists if we're opening up the receiving end.
				Service service = serviceDao.loadByName(domain, existingChannel.getDestination().getServiceName());
				if (service == null) {
					resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_SERVICE_NOT_FOUND;
					return resultHolder;
				}
			}
			if (existingCA.getReqSendAuthorization() != null) {
				if (auth.getSendAuthorization() == null) {
					resultHolder.status = SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_MISSING;
					return resultHolder;
				} else if (!auth.getSendAuthorization().getSignature().getValue()
						.equals(existingCA.getReqSendAuthorization().getSignature().getValue())) {
					resultHolder.status = SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_MISMATCH;
					return resultHolder;
				}
			} else if (auth.getSendAuthorization() != null) {
				// and if there isn't a requestedRecvAuth we cannot provide one either
				resultHolder.status = SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_PROVIDED;
				return resultHolder;
			}
			existingCA.setReqSendAuthorization(null);
			existingCA.setReqRecvAuthorization(null);
			existingCA.setSendAuthorization(auth.getSendAuthorization());

			// change of recvAuth vs existing recvAuth forces transfer
			if (existingCA.getRecvAuthorization() == null
					|| !existingCA.getRecvAuthorization().getSignature().getValue()
							.equals(auth.getRecvAuthorization().getSignature().getValue())
					|| auth.getProcessingState().getStatus() != ProcessingStatus.NONE) {
				existingCA.setProcessingState(ProcessingState.pending());
			}
			existingCA.setRecvAuthorization(auth.getRecvAuthorization());
			existingCA.setLimit(auth.getLimit());
		}

		// replicate the CA data into the flowQuota
		existingChannel.getQuota().updateAuthorizationInfo();

		// if the channel is "OPEN" and we are a receiving channel, then initialize any ChannelDestinationSessions from
		// any existing Destination.
		if (existingChannel.isOpen() && existingChannel.isRecv()) {
			Destination d = destinationDao.loadByChannelDestination(zone, existingChannel.getDestination());
			if (d != null) {
				setChannelDestinationSession(zone, existingChannel, d.getDestinationSession());
				// we don't relay this DS - because the ROS will automatically relay CA and then DS
			}
		} else if (!existingChannel.isOpen()) {
			// if the channel is "CLOSED" we don't allow any DestinationSession
			existingChannel.setSession(null);
		}

		// persist the new ca
		if (newChannel) {
			getChannelDao().persist(existingChannel);
		}

		resultHolder.channelAuthorization = existingCA;
		return resultHolder;
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void relayAuthorization(Zone zone, Long channelId, EndpointPermission otherPerm) {
		// lookup any existing ChannelAuthorization in the domain given the provided channel(origin+destination).
		Channel existingChannel = findById(channelId, false, true);
		// lookup or create a new ChannelAuthorization to hold the relayed in Permission
		ChannelAuthorization existingCA = existingChannel.getAuthorization();
		if (existingChannel.isSend()) {
			// we are sender and we got relayed in a requested recv authorization
			existingCA.setReqRecvAuthorization(otherPerm);
			existingCA.setReqSendAuthorization(null);
		} else {
			// we are receiver and we received a requested send authorization
			existingCA.setReqRecvAuthorization(null);
			existingCA.setReqSendAuthorization(otherPerm);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public Channel relayInitialAuthorization(Zone zone, Long tempChannelId, EndpointPermission otherPerm) {
		// lookup any existing ChannelAuthorization in the domain given the provided channel(origin+destination).
		TemporaryChannel tempChannel = findByTempChannelId(tempChannelId);

		Channel newChannel = new Channel(tempChannel.getDomain(), tempChannel.getOrigin(),
				tempChannel.getDestination());
		ChannelAuthorization newCA = new ChannelAuthorization(newChannel);

		// lookup or create a new ChannelAuthorization to hold the relayed in Permission
		if (newChannel.isSend()) {
			// we are sender and we got relayed in a requested recv authorization
			newCA.setReqRecvAuthorization(otherPerm);
			newCA.setReqSendAuthorization(null);
		} else {
			// we are receiver and we received a requested send authorization
			newCA.setReqRecvAuthorization(null);
			newCA.setReqSendAuthorization(otherPerm);
		}
		// update the denormalized CA data in flowquota
		newChannel.getQuota().updateAuthorizationInfo();

		channelDao.persist(newChannel);
		channelDao.delete(tempChannel);
		return newChannel;
	}

	@Override
	@Transactional(value = "ZoneDB")
	public Channel setChannelDestinationSession(Zone zone, Long channelId, DestinationSession destinationSession) {
		Channel channel = findById(channelId, false, false);
		setChannelDestinationSession(zone, channel, destinationSession);
		return channel;
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void relayChannelDestinationSession(Zone zone, Long channelId, DestinationSession destinationSession) {
		Channel channel = findById(channelId, false, false);
		setChannelDestinationSession(zone, channel, destinationSession);
		// persist should not be necessary
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void create(Channel channel) {
		if (channel.getId() != null) {
			log.warn("Unable to persist Channel with id " + channel.getId());
		} else {
			getChannelDao().persist(channel);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(Channel channel) {
		Channel storedChannel = getChannelDao().loadById(channel.getId(), false, false);
		if (storedChannel != null) {
			getChannelDao().delete(storedChannel);
		} else {
			log.warn("Unable to find Channel to delete with id " + channel.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void create(TemporaryChannel channel) {
		getChannelDao().persist(channel);
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(TemporaryChannel tempChannel) {
		TemporaryChannel storedTempChannel = getChannelDao().loadByTempId(tempChannel.getId());
		if (storedTempChannel != null) {
			getChannelDao().delete(storedTempChannel);
		} else {
			log.warn("Unable to find TemporaryChannel to delete with id " + tempChannel.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void create(ChannelMessage message) {
		if (message.getId() != null) {
			log.warn("Unable to persist ChannelMessage with id " + message.getId());
		} else {
			getChannelDao().persist(message);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(ChannelMessage message) {
		ChannelMessage storedMessage = getChannelDao().loadChannelMessageByMessageId(message.getId());
		if (storedMessage != null) {
			getChannelDao().delete(storedMessage);
		} else {
			log.warn("Unable to find ChannelMessage to delete with id " + message.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<Channel> search(Zone zone, ChannelAuthorizationSearchCriteria criteria) {
		return getChannelDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<ChannelMessage> search(Zone zone, ChannelMessageSearchCriteria criteria) {
		return getChannelDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<TemporaryChannel> search(Zone zone, TemporaryChannelSearchCriteria criteria) {
		return getChannelDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB")
	public SubmitMessageResultHolder preSubmitMessage(Zone zone, ChannelMessage msg) {
		SubmitMessageResultHolder result = new SubmitMessageResultHolder();

		// get and lock quota and check we can send
		FlowQuota quota = getChannelDao().lock(msg.getChannel().getQuota().getId());
		if (ChannelAuthorizationStatus.CLOSED == quota.getAuthorizationStatus()) {
			// we are not currently authorized to send out.
			result.status = SubmitMessageOperationStatus.CHANNEL_CLOSED;
			return result;
		}
		if (FlowControlStatus.CLOSED == quota.getFlowStatus()) {
			// we are already closed for sending - opening is only changed by the relaying out creating capacity.
			result.status = SubmitMessageOperationStatus.FLOW_CONTROL_CLOSED;
			return result;
		}
		// we can exceed the high mark but if we do then we set flow control to closed.
		quota.incrementBufferOnSend(msg.getPayloadLength());

		return result;
	}

	@Override
	@Transactional(value = "ZoneDB")
	public SubmitMessageResultHolder preRelayMessage(Zone zone, ChannelMessage msg) {
		SubmitMessageResultHolder result = new SubmitMessageResultHolder();
		// get and lock quota and check we can send
		FlowQuota quota = getChannelDao().lock(msg.getChannel().getQuota().getId());
		if (ChannelAuthorizationStatus.CLOSED == quota.getAuthorizationStatus()) {
			// we are not currently authorized to receive
			result.status = SubmitMessageOperationStatus.CHANNEL_CLOSED;
			return result;
		}
		if (FlowControlStatus.CLOSED == quota.getFlowStatus()) {
			// quota remains exceeded and closed to relaying in - receiving consuming messages creates capacity which
			// changes this status eventually
			result.status = SubmitMessageOperationStatus.FLOW_CONTROL_CLOSED;
			return result;
		}
		// we can exceed the high mark but if we do then we set flow control to closed.
		quota.incrementBufferOnRelay(msg.getPayloadLength());

		return result;
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public ChannelAuthorization findByChannel(Zone zone, Domain domain, ChannelOrigin origin, ChannelDestination dest) {
		if (domain == null) {
			throw new IllegalArgumentException("missing domain");
		}
		if (origin == null) {
			throw new IllegalArgumentException("missing origin");
		}
		if (!StringUtils.hasText(origin.getLocalName())) {
			throw new IllegalArgumentException("missing origin localName");
		}
		if (!StringUtils.hasText(origin.getDomainName())) {
			throw new IllegalArgumentException("missing origin domainName");
		}
		if (dest == null) {
			throw new IllegalArgumentException("missing dest");
		}
		if (!StringUtils.hasText(dest.getLocalName())) {
			throw new IllegalArgumentException("missing dest localName");
		}
		if (!StringUtils.hasText(dest.getDomainName())) {
			throw new IllegalArgumentException("missing dest domainName");
		}
		if (!StringUtils.hasText(dest.getServiceName())) {
			throw new IllegalArgumentException("missing dest serviceName");
		}
		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
		criteria.setDomain(domain);
		criteria.getOrigin().setLocalName(origin.getLocalName());
		criteria.getOrigin().setDomainName(origin.getDomainName());
		criteria.getDestination().setLocalName(dest.getLocalName());
		criteria.getDestination().setDomainName(dest.getDomainName());
		criteria.getDestination().setServiceName(dest.getServiceName());
		List<Channel> auths = getChannelDao().search(zone, criteria);

		return auths.isEmpty() ? null : auths.get(0).getAuthorization();
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public TemporaryChannel findByTemporaryChannel(Zone zone, Domain domain, ChannelOrigin origin,
			ChannelDestination dest) {
		if (domain == null) {
			throw new IllegalArgumentException("missing domain");
		}
		if (origin == null) {
			throw new IllegalArgumentException("missing origin");
		}
		if (!StringUtils.hasText(origin.getLocalName())) {
			throw new IllegalArgumentException("missing origin localName");
		}
		if (!StringUtils.hasText(origin.getDomainName())) {
			throw new IllegalArgumentException("missing origin domainName");
		}
		if (dest == null) {
			throw new IllegalArgumentException("missing dest");
		}
		if (!StringUtils.hasText(dest.getLocalName())) {
			throw new IllegalArgumentException("missing dest localName");
		}
		if (!StringUtils.hasText(dest.getDomainName())) {
			throw new IllegalArgumentException("missing dest domainName");
		}
		if (!StringUtils.hasText(dest.getServiceName())) {
			throw new IllegalArgumentException("missing dest serviceName");
		}
		TemporaryChannelSearchCriteria criteria = new TemporaryChannelSearchCriteria(new PageSpecifier(0, 1));
		criteria.setDomain(domain);
		criteria.getOrigin().setLocalName(origin.getLocalName());
		criteria.getOrigin().setDomainName(origin.getDomainName());
		criteria.getDestination().setLocalName(dest.getLocalName());
		criteria.getDestination().setDomainName(dest.getDomainName());
		criteria.getDestination().setServiceName(dest.getServiceName());
		List<TemporaryChannel> tempChannels = getChannelDao().search(zone, criteria);

		return tempChannels.isEmpty() ? null : tempChannels.get(0);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Channel findById(Long id, boolean includeFlowQuota, boolean includeAuth) {
		return getChannelDao().loadById(id, includeFlowQuota, includeAuth);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public TemporaryChannel findByTempChannelId(Long tempChannelId) {
		return getChannelDao().loadByTempId(tempChannelId);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public ChannelMessage findByMessageId(Long msgId) {
		return getChannelDao().loadChannelMessageByMessageId(msgId);
	}

	@Override
	@Transactional(value = "ZoneDB")
	public Channel updateStatusDestinationSession(Long channelId, ProcessingState newState) {
		Channel c = getChannelDao().loadById(channelId, false, false);
		c.setProcessingState(newState);
		return c;
	}

	@Override
	@Transactional(value = "ZoneDB")
	public Channel updateStatusChannelAuthorization(Long channelId, ProcessingState newState) {
		Channel c = getChannelDao().loadById(channelId, false, true);
		c.getAuthorization().setProcessingState(newState);
		return c;
	}

	@Override
	@Transactional(value = "ZoneDB")
	public FlowQuota updateStatusFlowQuota(Long quotaId, ProcessingState newState) {
		FlowQuota fc = getChannelDao().lock(quotaId);
		fc.setProcessingState(newState);
		return fc;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setChannelDestinationSession(Zone zone, Channel channel, DestinationSession destinationSession) {
		channel.setSession(destinationSession);
		// on the receiving side, we need to relay new Channel DestinationSessions to the sending side, except if we are
		// both
		// sender and receiver
		if (!channel.isSend() && channel.isRecv()) {
			channel.setProcessingState(ProcessingState.pending());
		} else {
			channel.setProcessingState(ProcessingState.none());
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public ChannelDao getChannelDao() {
		return channelDao;
	}

	public void setChannelDao(ChannelDao channelDao) {
		this.channelDao = channelDao;
	}

	public DestinationDao getDestinationDao() {
		return destinationDao;
	}

	public void setDestinationDao(DestinationDao destinationDao) {
		this.destinationDao = destinationDao;
	}

	public ServiceDao getServiceDao() {
		return serviceDao;
	}

	public void setServiceDao(ServiceDao serviceDao) {
		this.serviceDao = serviceDao;
	}

	public AgentCredentialDao getAgentCredentialDao() {
		return agentCredentialDao;
	}

	public void setAgentCredentialDao(AgentCredentialDao agentCredentialDao) {
		this.agentCredentialDao = agentCredentialDao;
	}

}
