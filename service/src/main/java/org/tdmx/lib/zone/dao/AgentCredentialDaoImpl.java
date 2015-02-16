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
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.AgentCredentialID;
import org.tdmx.lib.zone.domain.AgentCredentialSearchCriteria;

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
	public AgentCredential loadById(AgentCredentialID id) {
		Query query = em.createQuery("from AgentCredential as ac where ac.id = :id");
		query.setParameter("id", id);
		try {
			return (AgentCredential) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AgentCredential> search(String zoneApex, AgentCredentialSearchCriteria criteria) {
		Map<String, Object> parameters = new TreeMap<String, Object>();
		StringBuilder whereClause = new StringBuilder();

		if (StringUtils.hasText(criteria.getDomainName())) {
			whereClause.append(" and ac.domainName = :d");
			parameters.put("d", criteria.getDomainName());
		}
		if (StringUtils.hasText(criteria.getAddressName())) {
			whereClause.append(" and ac.addressName = :l");
			parameters.put("l", criteria.getAddressName());
		}
		if (criteria.getStatus() != null) {
			whereClause.append(" and ac.credentialStatus = :s");
			parameters.put("s", criteria.getStatus());
		}
		if (criteria.getType() != null) {
			whereClause.append(" and ac.credentialType = :t");
			parameters.put("t", criteria.getType());
		}
		Query query = em.createQuery("from AgentCredential as ac where ac.id.zoneApex = :z" + whereClause.toString());
		for (Entry<String, Object> param : parameters.entrySet()) {
			query.setParameter(param.getKey(), param.getValue());
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
