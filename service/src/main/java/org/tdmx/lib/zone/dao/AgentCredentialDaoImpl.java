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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

public class AgentCredentialDaoImpl implements AgentCredentialDao {

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
	public void persist(AgentCredential value) {
		em.persist(value);
	}

	@Override
	public void delete(AgentCredential value) {
		em.remove(value);
	}

	@Override
	public void lock(AgentCredential value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public AgentCredential merge(AgentCredential value) {
		return em.merge(value);
	}

	@Override
	public AgentCredential loadById(Long id) {
		return new JPAQuery(em).from(agentCredential).where(agentCredential.id.eq(id)).uniqueResult(agentCredential);
	}

	@Override
	public List<AgentCredential> search(Zone zone, AgentCredentialSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		if (!StringUtils.hasText(zone.getZoneApex())) {
			throw new IllegalArgumentException("missing zoneApex");
		}
		JPAQuery query = new JPAQuery(em).from(agentCredential);

		BooleanExpression where = agentCredential.zone.eq(zone);

		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(agentCredential.domainName.eq(criteria.getDomainName()));
		}
		if (StringUtils.hasText(criteria.getAddressName())) {
			where = where.and(agentCredential.addressName.eq(criteria.getAddressName()));
		}
		if (criteria.getStatus() != null) {
			where = where.and(agentCredential.credentialStatus.eq(criteria.getStatus()));
		}
		if (criteria.getType() != null) {
			where = where.and(agentCredential.credentialType.eq(criteria.getType()));
		}
		if (StringUtils.hasText(criteria.getFingerprint())) {
			where = where.and(agentCredential.fingerprint.eq(criteria.getFingerprint()));
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(agentCredential);
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