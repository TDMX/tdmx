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
import static org.tdmx.lib.zone.domain.QChannelMessage.channelMessage;
import static org.tdmx.lib.zone.domain.QDomain.domain;
import static org.tdmx.lib.zone.domain.QFlowQuota.flowQuota;
import static org.tdmx.lib.zone.domain.QMessageState.messageState;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.zone.domain.ChannelDestination;
import org.tdmx.lib.zone.domain.ChannelMessage;
import org.tdmx.lib.zone.domain.ChannelMessageSearchCriteria;
import org.tdmx.lib.zone.domain.ChannelOrigin;
import org.tdmx.lib.zone.domain.MessageState;
import org.tdmx.lib.zone.domain.MessageStatus;
import org.tdmx.lib.zone.domain.MessageStatusSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

public class MessageDaoImpl implements MessageDao {
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
	public void persist(ChannelMessage value) {
		em.persist(value);
	}

	@Override
	public void delete(ChannelMessage value) {
		em.remove(value);
	}

	@Override
	public ChannelMessage loadById(Long msgId, boolean fetchState) {
		if (msgId == null) {
			throw new IllegalArgumentException("missing msgId");
		}
		JPAQuery query = new JPAQuery(em).from(channelMessage);
		if (fetchState) {
			query = query.innerJoin(channelMessage.state, messageState).fetch();
		}
		query.where(channelMessage.id.eq(msgId));
		return query.uniqueResult(channelMessage);
	}

	@Override
	public MessageState loadStateById(Long stateId, boolean fetchMsg, boolean fetchChannel) {
		if (stateId == null) {
			throw new IllegalArgumentException("stateId msgId");
		}
		if (fetchChannel && !fetchMsg) {
			throw new IllegalArgumentException("fetchChannel only if fetchMsg");
		}
		JPAQuery query = new JPAQuery(em).from(messageState);
		if (fetchMsg) {
			query = query.innerJoin(messageState.msg, channelMessage).fetch();
		}
		if (fetchChannel) {
			query = query.innerJoin(channelMessage.channel, channel).fetch().innerJoin(channel.domain, domain).fetch()
					.innerJoin(channel.quota, flowQuota).fetch();
		}
		query.where(messageState.id.eq(stateId));
		return query.uniqueResult(messageState);
	}

	@Override
	public List<String> getPreparedSendTransactions(Zone zone, ChannelOrigin origin, int originSerialNr) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		if (origin == null) {
			throw new IllegalArgumentException("missing origin");
		}
		JPAQuery query = new JPAQuery(em).from(messageState);

		BooleanExpression where = messageState.zone.eq(zone).and(messageState.txId.isNotNull())
				.and(messageState.status.eq(MessageStatus.UPLOADED))
				.and(messageState.origin.localName.eq(origin.getLocalName()))
				.and(messageState.origin.domainName.eq(origin.getDomainName()))
				.and(messageState.originSerialNr.eq(originSerialNr));

		query.where(where);
		query.distinct();
		return query.list(messageState.txId);
	}

	@Override
	public List<String> getPreparedReceiveTransactions(Zone zone, ChannelDestination destination,
			int destinationSerialNr) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		if (destination == null) {
			throw new IllegalArgumentException("missing destination");
		}
		JPAQuery query = new JPAQuery(em).from(messageState);

		BooleanExpression where = messageState.zone.eq(zone).and(messageState.txId.isNotNull())
				.and(messageState.status.eq(MessageStatus.UPLOADED))
				.and(messageState.destination.localName.eq(destination.getLocalName()))
				.and(messageState.destination.domainName.eq(destination.getDomainName()))
				.and(messageState.destination.serviceName.eq(destination.getServiceName()))
				.and(messageState.destinationSerialNr.eq(destinationSerialNr));

		query.where(where);
		query.distinct();
		return query.list(messageState.txId);
	}

	@Override
	public List<ChannelMessage> search(Zone zone, ChannelMessageSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(channelMessage);

		BooleanExpression where = null;

		if (criteria.getChannel() != null) {
			where = channelMessage.channel.eq(criteria.getChannel());
		} else {
			// we are not looking for messages of a specific channel, so we need to fetch the channel and it's domain
			query = query.innerJoin(channelMessage.channel, channel).fetch().innerJoin(channel.domain, domain).fetch();

			where = domain.zone.eq(zone);
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
		}
		if (StringUtils.hasText(criteria.getMsgId())) {
			where = where.and(channelMessage.msgId.eq(criteria.getMsgId()));
		}
		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(),
				(long) criteria.getPageSpecifier().getFirstResult()));
		return query.list(channelMessage);
	}

	@Override
	public List<MessageState> search(Zone zone, MessageStatusSearchCriteria criteria, boolean fetchMsg) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(messageState);

		// we are not looking for messages of a specific channel, so we need to fetch the channel and it's domain
		if (fetchMsg) {
			query = query.innerJoin(messageState.msg, channelMessage).fetch();
		}

		query.where(buildCriteria(zone, criteria));

		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(),
				(long) criteria.getPageSpecifier().getFirstResult()));
		return query.list(messageState);
	}

	@Override
	public List<Long> getReferences(Zone zone, MessageStatusSearchCriteria criteria, int maxResults) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(messageState);

		query.where(buildCriteria(zone, criteria));

		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(),
				(long) criteria.getPageSpecifier().getFirstResult()));
		return query.list(messageState.id);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private BooleanExpression buildCriteria(Zone zone, MessageStatusSearchCriteria criteria) {
		BooleanExpression where = messageState.zone.eq(zone);

		if (criteria.getMessageStatus() != null) {
			where = where.and(messageState.status.eq(criteria.getMessageStatus()));
		}
		if (StringUtils.hasText(criteria.getXid())) {
			where = where.and(messageState.txId.eq(criteria.getXid()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getLocalName())) {
			where = where.and(messageState.origin.localName.eq(criteria.getOrigin().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getOrigin().getDomainName())) {
			where = where.and(messageState.origin.domainName.eq(criteria.getOrigin().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(messageState.destination.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(messageState.destination.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(messageState.destination.serviceName.eq(criteria.getDestination().getServiceName()));
		}
		if (criteria.getDestinationSerialNr() != null) {
			where = where.and(messageState.destinationSerialNr.eq(criteria.getDestinationSerialNr()));
		}
		if (criteria.getOriginSerialNr() != null) {
			where = where.and(messageState.originSerialNr.eq(criteria.getOriginSerialNr()));
		}
		if (criteria.getProcessingStatus() != null) {
			where = where.and(messageState.processingState.status.eq(criteria.getProcessingStatus()));
		}
		return where;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
