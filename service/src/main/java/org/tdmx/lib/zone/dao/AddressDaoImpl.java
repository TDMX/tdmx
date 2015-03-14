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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.AddressSearchCriteria;

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
	public void lock(Address value) {
		em.lock(value, LockModeType.WRITE);
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
	public List<Address> search(ZoneReference zone, AddressSearchCriteria criteria) {
		if (zone.getTenantId() == null) {
			throw new IllegalArgumentException("missing tenantId");
		}
		if (!StringUtils.hasText(zone.getZoneApex())) {
			throw new IllegalArgumentException("missing zoneApex");
		}
		JPAQuery query = new JPAQuery(em).from(address);

		BooleanExpression where = address.tenantId.eq(zone.getTenantId()).and(address.zoneApex.eq(zone.getZoneApex()));

		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(address.domainName.eq(criteria.getDomainName()));
		}
		if (StringUtils.hasText(criteria.getLocalName())) {
			where = where.and(address.localName.eq(criteria.getLocalName()));
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(address);
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
