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

import static org.tdmx.lib.control.domain.QAccountZoneAdministrationCredential.accountZoneAdministrationCredential;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredential;
import org.tdmx.lib.control.domain.AccountZoneAdministrationCredentialSearchCriteria;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

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
	public AccountZoneAdministrationCredential merge(AccountZoneAdministrationCredential value) {
		return em.merge(value);
	}

	@Override
	public AccountZoneAdministrationCredential loadById(Long id) {
		return new JPAQuery(em).from(accountZoneAdministrationCredential)
				.where(accountZoneAdministrationCredential.id.eq(id)).uniqueResult(accountZoneAdministrationCredential);
	}

	@Override
	public List<AccountZoneAdministrationCredential> search(
			AccountZoneAdministrationCredentialSearchCriteria criteria) {
		JPAQuery query = new JPAQuery(em).from(accountZoneAdministrationCredential);

		BooleanExpression where = null;
		if (StringUtils.hasText(criteria.getAccountId())) {
			BooleanExpression e = accountZoneAdministrationCredential.accountId.eq(criteria.getAccountId());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getZoneApex())) {
			BooleanExpression e = accountZoneAdministrationCredential.zoneApex.eq(criteria.getZoneApex());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getFingerprint())) {
			BooleanExpression e = accountZoneAdministrationCredential.fingerprint.eq(criteria.getFingerprint());
			where = where != null ? where.and(e) : e;
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(),
				(long) criteria.getPageSpecifier().getFirstResult()));
		return query.list(accountZoneAdministrationCredential);
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
