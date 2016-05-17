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

import static org.tdmx.lib.zone.domain.QAddress.address;
import static org.tdmx.lib.zone.domain.QDomain.domain;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Zone;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

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
	public Address merge(Address value) {
		return em.merge(value);
	}

	@Override
	public Address loadById(Long id) {
		return new JPAQuery(em).from(address).where(address.id.eq(id)).uniqueResult(address);
	}

	@Override
	public List<Address> search(Zone zone, AddressSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(address).innerJoin(address.domain, domain).fetch();

		BooleanExpression where = domain.zone.eq(zone);

		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(domain.domainName.eq(criteria.getDomainName()));
		}
		if (StringUtils.hasText(criteria.getLocalName())) {
			where = where.and(address.localName.eq(criteria.getLocalName()));
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(address);
	}

	@Override
	public Address loadByName(Domain domain, String localName) {
		if (domain == null) {
			throw new IllegalArgumentException("missing zone");
		}
		if (!StringUtils.hasText(localName)) {
			throw new IllegalArgumentException("missing localName");
		}
		JPAQuery query = new JPAQuery(em).from(address).innerJoin(address.domain).fetch();

		BooleanExpression where = address.domain.eq(domain).and(address.localName.eq(localName));

		query.where(where);
		return query.uniqueResult(address);
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
