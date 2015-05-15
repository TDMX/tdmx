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
import org.tdmx.lib.zone.dao.ChannelAuthorizationDao;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.Zone;

/**
 * Transactional CRUD Services for ChannelAuthorization Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class ChannelAuthorizationServiceRepositoryImpl implements ChannelAuthorizationService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ChannelAuthorizationServiceRepositoryImpl.class);

	private ChannelAuthorizationDao channelAuthorizationDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	@Transactional(value = "ZoneDB")
	public SetAuthorizationOperationStatus setAuthorization(Zone zone, ChannelAuthorization auth) {
		SetAuthorizationOperationStatus result = SetAuthorizationOperationStatus.SUCCESS;
		//
		String domainName = auth.getDomain().getDomainName();

		// lookup any existing ChannelAuthorization in the domain given the provided channel(origin+destination).
		ChannelAuthorization existingCA = findByChannel(zone, domainName, auth.getOrigin(), auth.getDestination());
		if (existingCA == null) {
			// If no existing ca - then create one with empty data.
			existingCA = new ChannelAuthorization(auth.getDomain());
		}

		// handle sendAuth(+confirm requested recvAuth)
		// - no reqSendAuth allowed in existing ca.
		// - change of sendAuth vs existing sendAuth forces transfer
		boolean sendAuthChanged = false;
		boolean recvAuthChanged = false;

		if (domainName.equals(auth.getOrigin().getDomainName())
				&& domainName.equals(auth.getDestination().getDomainName())) {
			// 1) setting send&recvAuth on same domain channel
			// - no requested send/recv allowed in existing ca.
			existingCA.setReqRecvAuthorization(null);
			existingCA.setReqSendAuthorization(null);
			if (auth.getRecvAuthorization() == null) {
				return SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING;
			}
			if (auth.getSendAuthorization() == null) {
				return SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_MISSING;
			}
			existingCA.setSendAuthorization(auth.getSendAuthorization());
			existingCA.setRecvAuthorization(auth.getRecvAuthorization());

		} else if (domainName.equals(auth.getOrigin().getDomainName())) {
			// we are sender and there shall be no pending send authorization
			existingCA.setReqSendAuthorization(null);

			// we must confirm any requested recvAuth if there is one, but not invent one
			if (existingCA.getReqRecvAuthorization() != null) {
				if (auth.getRecvAuthorization() == null) {
					return SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_MISSING;
				} else if (auth.getRecvAuthorization().equals(existingCA.getReqRecvAuthorization())) {
					return SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_MISMATCH;
				}
				existingCA.setReqRecvAuthorization(null);
			} else if (auth.getRecvAuthorization() != null) {
				// and if there isn't a requestedRecvAuth we cannot provide one either
				return SetAuthorizationOperationStatus.RECEIVER_AUTHORIZATION_CONFIRMATION_PROVIDED;
			}
			existingCA.setRecvAuthorization(auth.getRecvAuthorization());

			if (!auth.getSendAuthorization().equals(existingCA.getSendAuthorization())) {
				sendAuthChanged = true;
			}
			existingCA.setSendAuthorization(auth.getSendAuthorization());
		} else {
			// 3) recvAuth(+confirming requested sendAuth)
			// - no reqRecvAuth allowed in existing ca.
			// - change of recvAuth vs existing sendAuth forces transfer
			// we are receiver and there shall be no pending recv authorization
			existingCA.setReqRecvAuthorization(null);

			// we must confirm any requested sendAuth if there is one, but not invent one
			if (existingCA.getReqSendAuthorization() != null) {
				if (auth.getSendAuthorization() == null) {
					return SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_MISSING;
				} else if (auth.getSendAuthorization().equals(existingCA.getReqSendAuthorization())) {
					return SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_MISMATCH;
				}
				existingCA.setReqSendAuthorization(null);
			} else if (auth.getSendAuthorization() != null) {
				// and if there isn't a requestedRecvAuth we cannot provide one either
				return SetAuthorizationOperationStatus.SENDER_AUTHORIZATION_CONFIRMATION_PROVIDED;
			}
			existingCA.setSendAuthorization(auth.getSendAuthorization());

			if (!auth.getRecvAuthorization().equals(existingCA.getRecvAuthorization())) {
				recvAuthChanged = true;
			}
			existingCA.setRecvAuthorization(auth.getRecvAuthorization());
		}

		// persist the new or updated ca.
		getChannelAuthorizationDao().persist(existingCA);
		return result;
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void createOrUpdate(ChannelAuthorization auth) {
		if (auth.getId() != null) {
			ChannelAuthorization storedAuth = getChannelAuthorizationDao().loadById(auth.getId());
			if (storedAuth != null) {
				getChannelAuthorizationDao().merge(auth);
			} else {
				log.warn("Unable to find ChannelAuthorization with id " + auth.getId());
			}
		} else {
			getChannelAuthorizationDao().persist(auth);
		}
	}

	@Override
	@Transactional(value = "ZoneDB")
	public void delete(ChannelAuthorization auth) {
		ChannelAuthorization storedAuth = getChannelAuthorizationDao().loadById(auth.getId());
		if (storedAuth != null) {
			getChannelAuthorizationDao().delete(storedAuth);
		} else {
			log.warn("Unable to find ChannelAuthorization to delete with id " + auth.getId());
		}
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public List<ChannelAuthorization> search(Zone zone, ChannelAuthorizationSearchCriteria criteria) {
		return getChannelAuthorizationDao().search(zone, criteria);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public ChannelAuthorization findByChannel(Zone zone, String domainName, ChannelOrigin origin,
			ChannelDestination dest) {
		if (domainName == null) {
			throw new IllegalArgumentException("missing domainName");
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
		criteria.setDomainName(domainName);
		criteria.getOrigin().setLocalName(origin.getLocalName());
		criteria.getOrigin().setDomainName(origin.getDomainName());
		criteria.getOrigin().setServiceProvider(origin.getServiceProvider());
		criteria.getDestination().setLocalName(dest.getLocalName());
		criteria.getDestination().setDomainName(dest.getDomainName());
		criteria.getDestination().setServiceProvider(dest.getServiceProvider());
		criteria.getDestination().setServiceName(dest.getServiceName());
		List<ChannelAuthorization> auths = getChannelAuthorizationDao().search(zone, criteria);

		return auths.isEmpty() ? null : auths.get(0);
	}

	@Override
	@Transactional(value = "ZoneDB", readOnly = true)
	public ChannelAuthorization findById(Long id) {
		return getChannelAuthorizationDao().loadById(id);
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

	public ChannelAuthorizationDao getChannelAuthorizationDao() {
		return channelAuthorizationDao;
	}

	public void setChannelAuthorizationDao(ChannelAuthorizationDao channelAuthorizationDao) {
		this.channelAuthorizationDao = channelAuthorizationDao;
	}

}
