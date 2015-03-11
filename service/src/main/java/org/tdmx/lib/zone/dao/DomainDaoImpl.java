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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainSearchCriteria;

public class DomainDaoImpl implements DomainDao {
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
	public void persist(Domain value) {
		em.persist(value);
	}

	@Override
	public void delete(Domain value) {
		em.remove(value);
	}

	@Override
	public void lock(Domain value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public Domain merge(Domain value) {
		return em.merge(value);
	}

	@Override
	public Domain loadById(Long id) {
		Query query = em.createQuery("from Domain as d where d.id = :d");
		query.setParameter("d", id);
		try {
			return (Domain) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Domain> search(ZoneReference zone, DomainSearchCriteria criteria) {
		if (zone.getTenantId() == null) {
			throw new IllegalArgumentException("missing tenantId");
		}
		if (!StringUtils.hasText(zone.getZoneApex())) {
			throw new IllegalArgumentException("missing zoneApex");
		}
		Map<String, Object> parameters = new TreeMap<String, Object>();
		StringBuilder whereClause = new StringBuilder();
		boolean isFirstClause = true;
		if (zone.getTenantId() != null) {
			isFirstClause = andClause(isFirstClause, "d.tenantId = :t", "t", zone.getTenantId(), whereClause,
					parameters);
		}
		if (StringUtils.hasText(zone.getZoneApex())) {
			isFirstClause = andClause(isFirstClause, "d.zoneApex = :z", "z", zone.getZoneApex(), whereClause,
					parameters);
		}
		if (StringUtils.hasText(criteria.getDomainName())) {
			isFirstClause = andClause(isFirstClause, "domainName = :d", "d", criteria.getDomainName(), whereClause,
					parameters);
		}
		StringBuilder sql = new StringBuilder();
		sql.append("from Domain as d");
		if (!isFirstClause) {
			sql.append(" where");
			sql.append(whereClause.toString());
		}
		Query query = em.createQuery(sql.toString());
		for (Entry<String, Object> param : parameters.entrySet()) {
			query.setParameter(param.getKey(), param.getValue());
		}
		query.setFirstResult(criteria.getPageSpecifier().getFirstResult());
		query.setMaxResults(criteria.getPageSpecifier().getMaxResults());
		return query.getResultList();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	private boolean andClause(boolean isFirstClause, String condition, String parameterName, Object parameter,
			StringBuilder whereClause, Map<String, Object> parameters) {
		if (!isFirstClause) {
			whereClause.append(" and");
		}
		whereClause.append(" ").append(condition);
		parameters.put(parameterName, parameter);
		return false;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
