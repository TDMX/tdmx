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
package org.tdmx.lib.control.dao;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialID;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialSearchCriteria;

public class AccountZoneAdministrationCredentialDaoImpl implements AccountZoneAdministrationCredentialDao {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@PersistenceContext(unitName = "ControlDB")
	private EntityManager em;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void persist(AccountZoneAdministrationCredential value) {
		em.persist(value);
	}

	@Override
	public void delete(AccountZoneAdministrationCredential value) {
		em.remove(value);
	}

	@Override
	public void lock(AccountZoneAdministrationCredential value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public AccountZoneAdministrationCredential merge(AccountZoneAdministrationCredential value) {
		return em.merge(value);
	}

	@Override
	public AccountZoneAdministrationCredential loadById(AccountZoneAdministrationCredentialID id) {
		Query query = em.createQuery("from AccountZoneAdministrationCredential as ac where ac.id = :id");
		query.setParameter("id", id);
		try {
			return (AccountZoneAdministrationCredential) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AccountZoneAdministrationCredential> search(AccountZoneAdministrationCredentialSearchCriteria criteria) {
		Map<String, Object> parameters = new TreeMap<String, Object>();
		StringBuilder whereClause = new StringBuilder();

		if (StringUtils.hasText(criteria.getAccountId())) {
			whereClause.append(" and ac.id.accountId = :l");
			parameters.put("l", criteria.getAccountId());
		}
		if (criteria.getStatus() != null) {
			whereClause.append(" and ac.credentialStatus = :s");
			parameters.put("s", criteria.getStatus());
		}
		String where = whereClause.toString();
		if (StringUtils.hasText(where)) {
			where = " where " + where;
		}
		Query query = em.createQuery("from AccountZoneAdministrationCredential as ac" + where);
		for (String param : parameters.keySet()) {
			query.setParameter(param, parameters.get(param));
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

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
