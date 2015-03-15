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
package org.tdmx.lib.zone.dao;

import static org.tdmx.lib.zone.domain.QChannelAuthorization.channelAuthorization;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

public class ChannelAuthorizationDaoImpl implements ChannelAuthorizationDao {
	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@PersistenceContext(unitName = "ZoneDB")
	private EntityManager em;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void persist(ChannelAuthorization value) {
		em.persist(value);
	}

	@Override
	public void delete(ChannelAuthorization value) {
		em.remove(value);
	}

	@Override
	public void lock(ChannelAuthorization value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public ChannelAuthorization merge(ChannelAuthorization value) {
		return em.merge(value);
	}

	@Override
	public ChannelAuthorization loadById(Long id) {
		return new JPAQuery(em).from(channelAuthorization).where(channelAuthorization.id.eq(id))
				.uniqueResult(channelAuthorization);
	}

	@Override
	public List<ChannelAuthorization> search(ZoneReference zone, ChannelAuthorizationSearchCriteria criteria) {
		if (zone.getTenantId() == null) {
			throw new IllegalArgumentException("missing tenantId");
		}
		if (!StringUtils.hasText(zone.getZoneApex())) {
			throw new IllegalArgumentException("missing zoneApex");
		}
		JPAQuery query = new JPAQuery(em).from(channelAuthorization);

		BooleanExpression where = channelAuthorization.tenantId.eq(zone.getTenantId()).and(
				channelAuthorization.zoneApex.eq(zone.getZoneApex()));

		if (StringUtils.hasText(criteria.getOrigin().getLocalName())) {
			where = where.and(channelAuthorization.origin.localName.eq(criteria.getOrigin().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getDomainName())) {
			where = where.and(channelAuthorization.origin.domainName.eq(criteria.getOrigin().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getServiceProvider())) {
			where = where
					.and(channelAuthorization.origin.serviceProvider.eq(criteria.getOrigin().getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(channelAuthorization.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where
					.and(channelAuthorization.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceProvider())) {
			where = where.and(channelAuthorization.destination.serviceProvider.eq(criteria.getDestination()
					.getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(channelAuthorization.destination.serviceName.eq(criteria.getDestination()
					.getServiceName()));
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(channelAuthorization);
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

}
