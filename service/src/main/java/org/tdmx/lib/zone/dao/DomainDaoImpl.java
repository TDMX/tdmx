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

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.DomainID;
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
	public Domain loadById(DomainID id) {
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
	public List<Domain> search(String zoneApex, DomainSearchCriteria criteria) {
		Query query = null;
		if (StringUtils.hasText(criteria.getDomainName())) {
			query = em.createQuery("from Domain as d where d.id.domainName = :d and d.id.zoneApex = :z");
			query.setParameter("d", criteria.getDomainName());
		} else {
			query = em.createQuery("from Domain as d where d.id.zoneApex = :z");
		}
		query.setParameter("z", zoneApex);
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

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
