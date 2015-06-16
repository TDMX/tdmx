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

import static org.tdmx.lib.zone.domain.QChannel.channel;
import static org.tdmx.lib.zone.domain.QChannelAuthorization.channelAuthorization;
import static org.tdmx.lib.zone.domain.QChannelFlowOrigin.channelFlowOrigin;
import static org.tdmx.lib.zone.domain.QChannelFlowTarget.channelFlowTarget;
import static org.tdmx.lib.zone.domain.QDomain.domain;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelFlowOrigin;
import org.tdmx.lib.zone.domain.ChannelFlowSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelFlowTarget;
import org.tdmx.lib.zone.domain.ChannelFlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

public class ChannelDaoImpl implements ChannelDao {
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
	public void persist(Channel value) {
		em.persist(value);
	}

	@Override
	public void delete(Channel value) {
		em.remove(value);
	}

	@Override
	public void delete(ChannelFlowTarget value) {
		em.remove(value);
	}

	@Override
	public void delete(ChannelFlowOrigin value) {
		em.remove(value);
	}

	@Override
	public Channel merge(Channel value) {
		return em.merge(value);
	}

	@Override
	public Channel loadById(Long id) {
		return new JPAQuery(em).from(channel).where(channel.id.eq(id)).uniqueResult(channel);
	}

	@Override
	public ChannelFlowTarget loadChannelFlowTargetById(Long id) {
		return new JPAQuery(em).from(channelFlowTarget).where(channelFlowTarget.id.eq(id))
				.uniqueResult(channelFlowTarget);
	}

	@Override
	public ChannelFlowOrigin loadChannelFlowOriginById(Long id) {
		return new JPAQuery(em).from(channelFlowOrigin).where(channelFlowOrigin.id.eq(id))
				.uniqueResult(channelFlowOrigin);
	}

	@Override
	public List<ChannelAuthorization> search(Zone zone, ChannelAuthorizationSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(channelAuthorization).innerJoin(channelAuthorization.channel, channel)
				.fetch().innerJoin(channel.domain, domain).fetch();

		BooleanExpression where = domain.zone.eq(zone);

		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(domain.domainName.eq(criteria.getDomainName()));
		}
		if (criteria.getDomain() != null) {
			where = where.and(channel.domain.eq(criteria.getDomain()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getLocalName())) {
			where = where.and(channel.origin.localName.eq(criteria.getOrigin().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getDomainName())) {
			where = where.and(channel.origin.domainName.eq(criteria.getOrigin().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getServiceProvider())) {
			where = where.and(channel.origin.serviceProvider.eq(criteria.getOrigin().getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(channel.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(channel.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceProvider())) {
			where = where.and(channel.destination.serviceProvider.eq(criteria.getDestination().getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(channel.destination.serviceName.eq(criteria.getDestination().getServiceName()));
		}
		if (criteria.getUnconfirmed() != null) {
			if (criteria.getUnconfirmed()) {
				BooleanExpression orCondition = channelAuthorization.reqRecvAuthorization.grant.isNotNull().or(
						channelAuthorization.reqSendAuthorization.grant.isNotNull());
				where = where.and(orCondition);
			} else {
				BooleanExpression orCondition = channelAuthorization.reqRecvAuthorization.grant.isNull().or(
						channelAuthorization.reqSendAuthorization.grant.isNull());
				where = where.and(orCondition);
			}
		}
		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(channelAuthorization);
	}

	@Override
	public List<ChannelFlowTarget> search(Zone zone, ChannelFlowTargetSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(channelFlowTarget).innerJoin(channelFlowTarget.channel, channel).fetch()
				.innerJoin(channel.domain, domain).fetch();

		BooleanExpression where = domain.zone.eq(zone);

		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(domain.domainName.eq(criteria.getDomainName()));
		}
		if (criteria.getDomain() != null) {
			where = where.and(channel.domain.eq(criteria.getDomain()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getLocalName())) {
			where = where.and(channel.origin.localName.eq(criteria.getOrigin().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getDomainName())) {
			where = where.and(channel.origin.domainName.eq(criteria.getOrigin().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getServiceProvider())) {
			where = where.and(channel.origin.serviceProvider.eq(criteria.getOrigin().getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(channel.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(channel.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceProvider())) {
			where = where.and(channel.destination.serviceProvider.eq(criteria.getDestination().getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(channel.destination.serviceName.eq(criteria.getDestination().getServiceName()));
		}
		if (StringUtils.hasText(criteria.getTargetFingerprint())) {
			where = where.and(channelFlowTarget.targetFingerprint.eq(criteria.getTargetFingerprint()));
		}
		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(channelFlowTarget);
	}

	@Override
	public List<Channel> search(Zone zone, ChannelSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(channel).innerJoin(channel.domain, domain).fetch();

		BooleanExpression where = domain.zone.eq(zone);

		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(domain.domainName.eq(criteria.getDomainName()));
		}
		if (criteria.getDomain() != null) {
			where = where.and(channel.domain.eq(criteria.getDomain()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getLocalName())) {
			where = where.and(channel.origin.localName.eq(criteria.getOrigin().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getDomainName())) {
			where = where.and(channel.origin.domainName.eq(criteria.getOrigin().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getServiceProvider())) {
			where = where.and(channel.origin.serviceProvider.eq(criteria.getOrigin().getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(channel.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(channel.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceProvider())) {
			where = where.and(channel.destination.serviceProvider.eq(criteria.getDestination().getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(channel.destination.serviceName.eq(criteria.getDestination().getServiceName()));
		}
		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(channel);
	}

	@Override
	public List<ChannelFlowOrigin> search(Zone zone, ChannelFlowSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(channelFlowOrigin)
				.innerJoin(channelFlowOrigin.flowTarget, channelFlowTarget).fetch()
				.innerJoin(channelFlowTarget.channel, channel).fetch().innerJoin(channel.domain, domain).fetch();

		BooleanExpression where = domain.zone.eq(zone);

		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(domain.domainName.eq(criteria.getDomainName()));
		}
		if (criteria.getDomain() != null) {
			where = where.and(channel.domain.eq(criteria.getDomain()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getLocalName())) {
			where = where.and(channel.origin.localName.eq(criteria.getOrigin().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getDomainName())) {
			where = where.and(channel.origin.domainName.eq(criteria.getOrigin().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getServiceProvider())) {
			where = where.and(channel.origin.serviceProvider.eq(criteria.getOrigin().getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(channel.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(channel.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceProvider())) {
			where = where.and(channel.destination.serviceProvider.eq(criteria.getDestination().getServiceProvider()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(channel.destination.serviceName.eq(criteria.getDestination().getServiceName()));
		}
		if (StringUtils.hasText(criteria.getTargetFingerprint())) {
			where = where.and(channelFlowTarget.targetFingerprint.eq(criteria.getTargetFingerprint()));
		}
		if (StringUtils.hasText(criteria.getSourceFingerprint())) {
			where = where.and(channelFlowOrigin.sourceFingerprint.eq(criteria.getSourceFingerprint()));
		}
		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(channelFlowOrigin);
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
