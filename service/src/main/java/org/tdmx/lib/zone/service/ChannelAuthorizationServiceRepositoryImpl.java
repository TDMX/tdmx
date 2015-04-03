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
