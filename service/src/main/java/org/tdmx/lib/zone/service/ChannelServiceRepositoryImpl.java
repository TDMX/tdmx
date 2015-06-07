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
import org.tdmx.lib.zone.dao.ChannelDao;
import org.tdmx.lib.zone.dao.FlowTargetDao;
import org.tdmx.lib.zone.domain.AgentSignature;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelFlowTarget;
import org.tdmx.lib.zone.domain.ChannelFlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.ChannelSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.EndpointPermission;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.FlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.FlowTargetSession;
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
	private FlowTargetDao flowTargetDao;

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
			existingCA.setProcessingState(new ProcessingState(ProcessingStatus.NONE));
		} else {
			existingChannel = existingCA.getChannel();
		}

		if (existingChannel.isSend() && existingChannel.isRecv()) {
			// 1) setting send&recvAuth on same domain channel
			// - no requested send/recv allowed in existing ca.
			existingCA.setReqRecvAuthorization(null);
			existingCA.setReqSendAuthorization(null);
			if (auth.getRecvAuthorization() == null) {
				resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING;
				return resultHolder;
			}
			if (auth.getSendAuthorization() == null) {
				resultHolder.status = SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_MISSING;
				return resultHolder;
			}
			existingCA.setSendAuthorization(auth.getSendAuthorization());
			existingCA.setRecvAuthorization(auth.getRecvAuthorization());
			existingCA.setProcessingState(new ProcessingState(ProcessingStatus.SUCCESS)); // no need to relay

		} else if (existingChannel.isSend()) {
			// we are sender and there shall be no pending send authorization
			existingCA.setReqSendAuthorization(null);

			// we must confirm any requested recvAuth if there is one, but not invent one
			if (existingCA.getReqRecvAuthorization() != null) {
				if (auth.getRecvAuthorization() == null) {
					resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING;
					return resultHolder;
				} else if (auth.getRecvAuthorization().equals(existingCA.getReqRecvAuthorization())) {
					resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_MISMATCH;
					return resultHolder;
				}
				existingCA.setReqRecvAuthorization(null);
			} else if (auth.getRecvAuthorization() != null) {
				// and if there isn't a requestedRecvAuth we cannot provide one either
				resultHolder.status = SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_PROVIDED;
				return resultHolder;
			}
			existingCA.setRecvAuthorization(auth.getRecvAuthorization());

			// change of sendAuth vs existing sendAuth forces transfer
			if (!auth.getSendAuthorization().equals(existingCA.getSendAuthorization())
					|| auth.getProcessingState().getStatus() != ProcessingStatus.SUCCESS) {
				auth.setProcessingState(new ProcessingState(ProcessingStatus.PENDING));
			}
			existingCA.setSendAuthorization(auth.getSendAuthorization());
		} else {
			// 3) recvAuth(+confirming requested sendAuth)
			// - no reqRecvAuth allowed in existing ca.
			// we are receiver and there shall be no pending recv authorization
			existingCA.setReqRecvAuthorization(null);

			// we must confirm any requested sendAuth if there is one, but not invent one
			if (existingCA.getReqSendAuthorization() != null) {
				if (auth.getSendAuthorization() == null) {
					resultHolder.status = SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_MISSING;
					return resultHolder;
				} else if (auth.getSendAuthorization().equals(existingCA.getReqSendAuthorization())) {
					resultHolder.status = SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_MISMATCH;
					return resultHolder;
				}
				existingCA.setReqSendAuthorization(null);
			} else if (auth.getSendAuthorization() != null) {
				// and if there isn't a requestedRecvAuth we cannot provide one either
				resultHolder.status = SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_PROVIDED;
				return resultHolder;
			}
			existingCA.setSendAuthorization(auth.getSendAuthorization());

			// change of recvAuth vs existing recvAuth forces transfer
			if (!auth.getRecvAuthorization().equals(existingCA.getRecvAuthorization())
					|| auth.getProcessingState().getStatus() != ProcessingStatus.SUCCESS) {
				auth.setProcessingState(new ProcessingState(ProcessingStatus.PENDING));
			}
			existingCA.setRecvAuthorization(auth.getRecvAuthorization());
		}

		// if the channel is "OPEN" and we are a receiving channel, then initialize any ChannelFlowTargets from the
		// existing FlowTargets of receivers.
		if (existingChannel.isOpen() && existingChannel.isRecv()) {
			FlowTargetSearchCriteria ftsc = new FlowTargetSearchCriteria(new PageSpecifier(0, 100)); // TODO global hard
																										// limit
			ftsc.getTarget().setAddressName(existingChannel.getDestination().getLocalName());
			ftsc.getTarget().setDomainName(existingChannel.getDestination().getDomainName());
			ftsc.setServiceName(existingChannel.getDestination().getServiceName());
			// suspended agents don't have FlowTargets, nor ChannelFlowTargets, so we don't need to restrict here on
			// "active" agents

			List<FlowTarget> flowTargets = flowTargetDao.search(zone, ftsc);
			for (FlowTarget ft : flowTargets) {
				setChannelFlowTarget(existingChannel, ft);
			}
		} else if (!existingChannel.isOpen()) {
			// if the channel is "CLOSED" we don't allow any ChannelFlowTargets ( nor Flows nor Messages )
			existingChannel.getChannelFlowTargets().clear();
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
	public void relayAuthorization(Zone zone, Domain domain, ChannelOrigin origin, ChannelDestination dest,
			EndpointPermission otherPerm) {
		// TODO Auto-generated method stub

	}

	@Override
	@Transactional(value = "ZoneDB")
	public void setChannelFlowTarget(Long id, FlowTarget flowTarget) {
		Channel channel = findById(id);
		setChannelFlowTarget(channel, flowTarget);
		// persist should not be necessary
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void createOrUpdate(Channel channel) {
		if (channel.getId() != null) {
			Channel storedAuth = getChannelDao().loadById(channel.getId());
			if (storedAuth != null) {
				getChannelDao().merge(channel);
			} else {
				log.warn("Unable to find Channel with id " + channel.getId());
			}
		} else {
			getChannelDao().persist(channel);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(Channel channel) {
		Channel storedAuth = getChannelDao().loadById(channel.getId());
		if (storedAuth != null) {
			getChannelDao().delete(storedAuth);
		} else {
			log.warn("Unable to find Channel to delete with id " + channel.getId());
		}
	}

	@Override
	public void delete(ChannelFlowTarget channelFlowTarget) {
		// TODO Auto-generated method stub

	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<ChannelAuthorization> search(Zone zone, ChannelAuthorizationSearchCriteria criteria) {
		return getChannelDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<Channel> search(Zone zone, ChannelSearchCriteria criteria) {
		return getChannelDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<ChannelFlowTarget> search(Zone zone, ChannelFlowTargetSearchCriteria criteria) {
		return getChannelDao().search(zone, criteria);
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
		if (!StringUtils.hasText(origin.getServiceProvider())) {
			throw new IllegalArgumentException("missing origin serviceProvider");
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
		if (!StringUtils.hasText(dest.getServiceProvider())) {
			throw new IllegalArgumentException("missing dest serviceProvider");
		}
		if (!StringUtils.hasText(dest.getServiceName())) {
			throw new IllegalArgumentException("missing dest serviceName");
		}
		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 1));
		criteria.setDomain(domain);
		criteria.getOrigin().setLocalName(origin.getLocalName());
		criteria.getOrigin().setDomainName(origin.getDomainName());
		criteria.getOrigin().setServiceProvider(origin.getServiceProvider());
		criteria.getDestination().setLocalName(dest.getLocalName());
		criteria.getDestination().setDomainName(dest.getDomainName());
		criteria.getDestination().setServiceProvider(dest.getServiceProvider());
		criteria.getDestination().setServiceName(dest.getServiceName());
		List<ChannelAuthorization> auths = getChannelDao().search(zone, criteria);

		return auths.isEmpty() ? null : auths.get(0);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public Channel findById(Long id) {
		return getChannelDao().loadById(id);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void setChannelFlowTarget(Channel channel, FlowTarget flowTarget) {
		ChannelFlowTarget foundFlowTarget = null;
		for (ChannelFlowTarget cft : channel.getChannelFlowTargets()) {
			if (cft.getTargetFingerprint().equals(flowTarget.getTarget().getFingerprint())) {
				foundFlowTarget = cft;
				break;
			}
		}
		if (foundFlowTarget == null) {
			ChannelFlowTarget cft = new ChannelFlowTarget(channel);
			channel.getChannelFlowTargets().add(cft);
			if (channel.isSend()) {
				// TODO initialize the ChannelFlowOrigins for ALL known Agents
			}

			foundFlowTarget = cft;
		}

		foundFlowTarget.setTargetFingerprint(flowTarget.getTarget().getFingerprint());

		FlowTargetSession fts = new FlowTargetSession();
		fts.setPrimary(flowTarget.getPrimary());
		fts.setSecondary(flowTarget.getSecondary());

		AgentSignature sig = new AgentSignature();
		sig.setAlgorithm(flowTarget.getSignatureAlgorithm());
		sig.setCertificateChainPem(flowTarget.getTarget().getCertificateChainPem());
		sig.setSignatureDate(flowTarget.getSignatureDate());
		sig.setValue(flowTarget.getSignatureValue());
		fts.setSignature(sig);
		foundFlowTarget.setFlowTargetSession(fts);
		// on the receiving side, we need to relay new ChannelFlowTargets to the sending side, except if we are both
		// sender and receiver
		if (!channel.isSend() && channel.isRecv()) {
			foundFlowTarget.setProcessingState(new ProcessingState(ProcessingStatus.PENDING));
		} else {
			foundFlowTarget.setProcessingState(new ProcessingState(ProcessingStatus.SUCCESS));
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

	public FlowTargetDao getFlowTargetDao() {
		return flowTargetDao;
	}

	public void setFlowTargetDao(FlowTargetDao flowTargetDao) {
		this.flowTargetDao = flowTargetDao;
	}

}
