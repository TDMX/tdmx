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
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobSearchCriteria;

public class ControlJobDaoImpl implements ControlJobDao {

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
	public void persist(ControlJob value) {
		em.persist(value);
	}

	@Override
	public void delete(ControlJob value) {
		em.remove(value);
	}

	@Override
	public void lock(ControlJob value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public ControlJob merge(ControlJob value) {
		return em.merge(value);
	}

	@Override
	public ControlJob loadById(String id) {
		Query query = em.createQuery("from ControlJob as cje where cje.jobId = :id");
		query.setParameter("id", id);
		try {
			return (ControlJob) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ControlJob> fetch(ControlJobSearchCriteria criteria, LockModeType lockMode) {
		Map<String, Object> parameters = new TreeMap<String, Object>();
		StringBuilder whereClause = new StringBuilder();
		boolean isFirstClause = true;
		if (criteria.getStatus() != null) {
			isFirstClause = andClause(isFirstClause, "cje.status = :s", "s", criteria.getStatus(), whereClause,
					parameters);
		}
		if (criteria.getScheduledTimeBefore() != null) {
			isFirstClause = andClause(isFirstClause, "cje.scheduledTime <= :t", "t", criteria.getScheduledTimeBefore(),
					whereClause, parameters);
		}
		if (StringUtils.hasText(criteria.getJobType())) {
			isFirstClause = andClause(isFirstClause, "cje.job.type = :jt", "jt", criteria.getJobType(), whereClause,
					parameters);
		}
		StringBuilder sql = new StringBuilder();
		sql.append("from ControlJob as cje");
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
		query.setLockMode(lockMode);
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
