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

import static org.tdmx.lib.zone.domain.QAgentCredential.agentCredential;
import static org.tdmx.lib.zone.domain.QFlowTarget.flowTarget;
import static org.tdmx.lib.zone.domain.QService.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.FlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

public class FlowTargetDaoImpl implements FlowTargetDao {
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
	public void persist(FlowTarget value) {
		em.persist(value);
	}

	@Override
	public void delete(FlowTarget value) {
		em.remove(value);
	}

	@Override
	public void lock(FlowTarget value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public FlowTarget merge(FlowTarget value) {
		return em.merge(value);
	}

	@Override
	public FlowTarget loadById(Long id) {
		return new JPAQuery(em).from(flowTarget).innerJoin(flowTarget.concurrency).fetch().where(flowTarget.id.eq(id))
				.uniqueResult(flowTarget);
	}

	@Override
	public FlowTarget loadByTargetService(Zone zone, AgentCredential agent, Service s) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(flowTarget).innerJoin(flowTarget.concurrency).fetch()
				.innerJoin(flowTarget.target, agentCredential).fetch().innerJoin(flowTarget.service, service).fetch()
				.where(agentCredential.eq(agent).and(service.eq(s)));
		return query.uniqueResult(flowTarget);
	}

	@Override
	public List<FlowTarget> search(Zone zone, FlowTargetSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(flowTarget).innerJoin(flowTarget.concurrency).fetch()
				.innerJoin(flowTarget.target, agentCredential).fetch().innerJoin(flowTarget.service, service).fetch();

		BooleanExpression where = flowTarget.target.zone.eq(zone);

		if (criteria.getTarget().getAgent() != null) {
			where = where.and(agentCredential.eq(criteria.getTarget().getAgent()));
		}
		if (StringUtils.hasText(criteria.getTarget().getAddressName())) {
			where = where.and(agentCredential.addressName.eq(criteria.getTarget().getAddressName()));
		}
		if (StringUtils.hasText(criteria.getTarget().getDomainName())) {
			where = where.and(agentCredential.domainName.eq(criteria.getTarget().getDomainName()));
		}
		if (criteria.getTarget().getStatus() != null) {
			where = where.and(agentCredential.credentialStatus.eq(criteria.getTarget().getStatus()));
		}
		if (StringUtils.hasText(criteria.getServiceName())) {
			where = where.and(service.serviceName.eq(criteria.getServiceName()));
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(flowTarget);
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
