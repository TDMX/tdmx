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
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;

public class AddressDaoImpl implements AddressDao {
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
	public void persist(Address value) {
		em.persist(value);
	}

	@Override
	public void delete(Address value) {
		em.remove(value);
	}

	@Override
	public void lock(Address value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public Address merge(Address value) {
		return em.merge(value);
	}

	@Override
	public Address loadById(Long id) {
		Query query = em.createQuery("from Address as a where a.id = :d");
		query.setParameter("d", id);
		try {
			return (Address) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Address> search(ZoneReference zone, AddressSearchCriteria criteria) {
		Map<String, Object> parameters = new TreeMap<String, Object>();
		StringBuilder whereClause = new StringBuilder();
		boolean isFirstClause = true;
		if (zone.getTenantId() != null) {
			isFirstClause = andClause(isFirstClause, "a.tenantId = :t", "t", zone.getTenantId(), whereClause,
					parameters);
		}
		if (StringUtils.hasText(zone.getZoneApex())) {
			isFirstClause = andClause(isFirstClause, "a.zoneApex = :z", "z", zone.getZoneApex(), whereClause,
					parameters);
		}
		if (StringUtils.hasText(criteria.getDomainName())) {
			isFirstClause = andClause(isFirstClause, "a.domainName = :d", "d", criteria.getDomainName(), whereClause,
					parameters);
		}
		if (StringUtils.hasText(criteria.getLocalName())) {
			isFirstClause = andClause(isFirstClause, "a.localName = :r", "r", criteria.getLocalName(), whereClause,
					parameters);
		}
		StringBuilder sql = new StringBuilder();
		sql.append("from Address as a");
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
