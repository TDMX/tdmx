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
import static org.tdmx.lib.zone.domain.QChannelMessage.channelMessage;
import static org.tdmx.lib.zone.domain.QDomain.domain;
import static org.tdmx.lib.zone.domain.QFlowQuota.flowQuota;
import static org.tdmx.lib.zone.domain.QTemporaryChannel.temporaryChannel;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.ProcessingState;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelMessageSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelSearchCriteria;
import org.tdmx.lib.zone.domain.FlowQuota;
import org.tdmx.lib.zone.domain.TemporaryChannel;
import org.tdmx.lib.zone.domain.TemporaryChannelSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
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
	public void persist(TemporaryChannel value) {
		em.persist(value);
	}

	@Override
	public void delete(Channel value) {
		em.remove(value);
	}

	@Override
	public Channel merge(Channel value) {
		return em.merge(value);
	}

	@Override
	public void persist(ChannelMessage value) {
		em.persist(value);
	}

	@Override
	public void delete(ChannelMessage value) {
		em.remove(value);
	}

	@Override
	public void delete(TemporaryChannel value) {
		em.remove(value);
	}

	@Override
	public Channel loadById(Long id, boolean includeFlowQuota, boolean includeAuth) {
		JPAQuery q = new JPAQuery(em).from(channel);
		if (includeFlowQuota) {
			q = q.innerJoin(channel.quota, flowQuota).fetch();
		}
		if (includeAuth) {
			q = q.innerJoin(channel.authorization, channelAuthorization).fetch();
		}
		return q.where(channel.id.eq(id)).uniqueResult(channel);
	}

	@Override
	public TemporaryChannel loadByTempId(Long tempChannelId) {
		return new JPAQuery(em).from(temporaryChannel).where(temporaryChannel.id.eq(tempChannelId))
				.uniqueResult(temporaryChannel);
	}

	@Override
	public FlowQuota lock(Long quotaId) {
		return new JPAQuery(em).setLockMode(LockModeType.PESSIMISTIC_WRITE).from(flowQuota)
				.where(flowQuota.id.eq(quotaId)).uniqueResult(flowQuota);
	}

	@Override
	public List<Channel> search(Zone zone, ChannelAuthorizationSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(channel).innerJoin(channel.authorization, channelAuthorization).fetch()
				.innerJoin(channel.domain, domain).fetch().innerJoin(channel.quota, flowQuota).fetch();

		BooleanExpression where = domain.zone.eq(zone);

		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(domain.domainName.eq(criteria.getDomainName()));
		}
		if (criteria.getDomain() != null) {
			where = where.and(domain.eq(criteria.getDomain()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getLocalName())) {
			where = where.and(channel.origin.localName.eq(criteria.getOrigin().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getDomainName())) {
			where = where.and(channel.origin.domainName.eq(criteria.getOrigin().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(channel.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(channel.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(channel.destination.serviceName.eq(criteria.getDestination().getServiceName()));
		}
		if (criteria.getUnconfirmed() != null) {
			if (criteria.getUnconfirmed()) {
				BooleanExpression orCondition = channelAuthorization.reqRecvAuthorization.grant.isNotNull()
						.or(channelAuthorization.reqSendAuthorization.grant.isNotNull());
				where = where.and(orCondition);
			} else {
				BooleanExpression orCondition = channelAuthorization.reqRecvAuthorization.grant.isNull()
						.or(channelAuthorization.reqSendAuthorization.grant.isNull());
				where = where.and(orCondition);
			}
		}
		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(),
				(long) criteria.getPageSpecifier().getFirstResult()));
		return query.list(channel);
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
			where = where.and(domain.eq(criteria.getDomain()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getLocalName())) {
			where = where.and(channel.origin.localName.eq(criteria.getOrigin().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getDomainName())) {
			where = where.and(channel.origin.domainName.eq(criteria.getOrigin().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(channel.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(channel.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(channel.destination.serviceName.eq(criteria.getDestination().getServiceName()));
		}
		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(),
				(long) criteria.getPageSpecifier().getFirstResult()));
		return query.list(channel);
	}

	@Override
	public List<TemporaryChannel> search(Zone zone, TemporaryChannelSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(temporaryChannel).innerJoin(temporaryChannel.domain, domain).fetch();

		BooleanExpression where = domain.zone.eq(zone);

		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(domain.domainName.eq(criteria.getDomainName()));
		}
		if (criteria.getDomain() != null) {
			where = where.and(domain.eq(criteria.getDomain()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getLocalName())) {
			where = where.and(temporaryChannel.origin.localName.eq(criteria.getOrigin().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getDomainName())) {
			where = where.and(temporaryChannel.origin.domainName.eq(criteria.getOrigin().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(temporaryChannel.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(temporaryChannel.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(temporaryChannel.destination.serviceName.eq(criteria.getDestination().getServiceName()));
		}
		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(),
				(long) criteria.getPageSpecifier().getFirstResult()));
		return query.list(temporaryChannel);
	}

	@Override
	public ChannelMessage loadChannelMessageByMessageId(Long msgId) {
		if (msgId == null) {
			throw new IllegalArgumentException("missing msgId");
		}
		JPAQuery query = new JPAQuery(em).from(channelMessage).where(channelMessage.id.eq(msgId));
		return query.uniqueResult(channelMessage);
	}

	@Override
	public void updateChannelMessageProcessingState(Long msgId, ProcessingState ps) {
		if (msgId == null) {
			throw new IllegalArgumentException("missing msgId");
		}
		// TODO LATER - HIBERNATE/JPA updating PS as a single item doesnt work - try later.
		JPAUpdateClause update = new JPAUpdateClause(em, channelMessage).where(channelMessage.id.eq(msgId))
				.set(channelMessage.processingState.status, ps.getStatus())
				.set(channelMessage.processingState.timestamp, ps.getTimestamp())
				.set(channelMessage.processingState.errorCode, ps.getErrorCode())
				.set(channelMessage.processingState.errorMessage, ps.getErrorMessage());
		update.execute();
	}

	@Override
	public List<ChannelMessage> search(Zone zone, ChannelMessageSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(channelMessage).innerJoin(channelMessage.channel, channel).fetch()
				.innerJoin(channel.domain, domain).fetch();

		BooleanExpression where = domain.zone.eq(zone);

		if (criteria.getChannel() != null) {
			where = where.and(channel.eq(criteria.getChannel()));
		}
		if (StringUtils.hasText(criteria.getMsgId())) {
			where = where.and(channelMessage.msgId.eq(criteria.getMsgId()));
		}
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
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(channel.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(channel.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(channel.destination.serviceName.eq(criteria.getDestination().getServiceName()));
		}
		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(),
				(long) criteria.getPageSpecifier().getFirstResult()));
		return query.list(channelMessage);
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
