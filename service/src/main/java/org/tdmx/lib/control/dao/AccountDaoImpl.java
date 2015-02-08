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
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountSearchCriteria;

public class AccountDaoImpl implements AccountDao {

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
	public void persist(Account value) {
		em.persist(value);
	}

	@Override
	public void delete(Account value) {
		em.remove(value);
	}

	@Override
	public void lock(Account value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public Account merge(Account value) {
		return em.merge(value);
	}

	@Override
	public Account loadById(Long id) {
		Query query = em.createQuery("from Account as a where a.id = :id");
		query.setParameter("id", id);
		try {
			return (Account) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Account> search(AccountSearchCriteria criteria) {
		Map<String, Object> parameters = new TreeMap<String, Object>();
		StringBuilder whereClause = new StringBuilder();
		boolean isFirstClause = true;
		if (StringUtils.hasText(criteria.getAccountId())) {
			isFirstClause = andClause(isFirstClause, "a.accountId = :s", "s", criteria.getAccountId(), whereClause,
					parameters);
		}
		if (StringUtils.hasText(criteria.getFirstName())) {
			isFirstClause = andClause(isFirstClause, "a.firstName = :fn", "fn", criteria.getFirstName(), whereClause,
					parameters);
		}
		if (StringUtils.hasText(criteria.getLastName())) {
			isFirstClause = andClause(isFirstClause, "a.lastName = :ln", "ln", criteria.getLastName(), whereClause,
					parameters);
		}
		if (StringUtils.hasText(criteria.getEmail())) {
			isFirstClause = andClause(isFirstClause, "a.email = :e", "e", criteria.getEmail(), whereClause, parameters);
		}
		StringBuilder sql = new StringBuilder();
		sql.append("from Account as a");
		if (!isFirstClause) {
			sql.append(" where");
			sql.append(whereClause.toString());
		}
		Query query = em.createQuery(sql.toString());
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
