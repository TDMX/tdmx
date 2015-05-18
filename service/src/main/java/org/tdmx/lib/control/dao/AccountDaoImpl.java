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

import static org.tdmx.lib.control.domain.QAccount.account;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.Account;
import org.tdmx.lib.control.domain.AccountSearchCriteria;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

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
	public Account merge(Account value) {
		return em.merge(value);
	}

	@Override
	public Account loadById(Long id) {
		return new JPAQuery(em).from(account).where(account.id.eq(id)).uniqueResult(account);
	}

	@Override
	public Account loadByAccountId(String accountId) {
		return new JPAQuery(em).from(account).where(account.accountId.eq(accountId)).uniqueResult(account);
	}

	@Override
	public List<Account> search(AccountSearchCriteria criteria) {
		JPAQuery query = new JPAQuery(em).from(account);

		BooleanExpression where = null;
		if (StringUtils.hasText(criteria.getAccountId())) {
			BooleanExpression e = account.accountId.eq(criteria.getAccountId());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getFirstName())) {
			BooleanExpression e = account.firstName.eq(criteria.getFirstName());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getLastName())) {
			BooleanExpression e = account.lastName.eq(criteria.getLastName());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getEmail())) {
			BooleanExpression e = account.email.eq(criteria.getEmail());
			where = where != null ? where.and(e) : e;
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(account);
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
