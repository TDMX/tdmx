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

import static org.tdmx.lib.control.domain.QAccountZone.accountZone;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneSearchCriteria;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

public class AccountZoneDaoImpl implements AccountZoneDao {

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
	public void persist(AccountZone value) {
		em.persist(value);
	}

	@Override
	public void delete(AccountZone value) {
		em.remove(value);
	}

	@Override
	public void lock(AccountZone value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public AccountZone merge(AccountZone value) {
		return em.merge(value);
	}

	@Override
	public AccountZone loadById(Long id) {
		return new JPAQuery(em).from(accountZone).where(accountZone.id.eq(id)).uniqueResult(accountZone);
	}

	@Override
	public List<AccountZone> search(AccountZoneSearchCriteria criteria) {
		JPAQuery query = new JPAQuery(em).from(accountZone);

		BooleanExpression where = null;
		if (StringUtils.hasText(criteria.getAccountId())) {
			BooleanExpression e = accountZone.accountId.eq(criteria.getAccountId());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getZoneApex())) {
			BooleanExpression e = accountZone.zoneApex.eq(criteria.getZoneApex());
			where = where != null ? where.and(e) : e;
		}
		if (criteria.getStatus() != null) {
			BooleanExpression e = accountZone.status.eq(criteria.getStatus());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getSegment())) {
			BooleanExpression e = accountZone.segment.eq(criteria.getSegment());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getZonePartitionId())) {
			BooleanExpression e = accountZone.zonePartitionId.eq(criteria.getZonePartitionId());
			where = where != null ? where.and(e) : e;
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(accountZone);
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
