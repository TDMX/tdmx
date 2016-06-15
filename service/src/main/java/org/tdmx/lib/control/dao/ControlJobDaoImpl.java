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

import static org.tdmx.lib.control.domain.QControlJob.controlJob;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.ControlJob;
import org.tdmx.lib.control.domain.ControlJobSearchCriteria;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

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
	public ControlJob merge(ControlJob value) {
		return em.merge(value);
	}

	@Override
	public ControlJob loadById(Long id) {
		return new JPAQuery(em).from(controlJob).where(controlJob.id.eq(id)).uniqueResult(controlJob);
	}

	@Override
	public List<ControlJob> fetch(ControlJobSearchCriteria criteria, LockModeType lockMode) {
		JPAQuery query = new JPAQuery(em).from(controlJob);

		BooleanExpression where = null;
		if (criteria.getStatus() != null) {
			BooleanExpression e = controlJob.status.eq(criteria.getStatus());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getSegment())) {
			BooleanExpression e = controlJob.segment.eq(criteria.getSegment());
			where = where != null ? where.and(e) : e;
		}
		if (criteria.getScheduledTimeBefore() != null) { // <=
			BooleanExpression e = controlJob.scheduledTime.loe(criteria.getScheduledTimeBefore());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getJobType())) {
			BooleanExpression e = controlJob.job.type.eq(criteria.getJobType());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getJobId())) {
			BooleanExpression e = controlJob.job.jobId.eq(criteria.getJobId());
			where = where != null ? where.and(e) : e;
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(),
				(long) criteria.getPageSpecifier().getFirstResult()));
		return query.list(controlJob);
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
