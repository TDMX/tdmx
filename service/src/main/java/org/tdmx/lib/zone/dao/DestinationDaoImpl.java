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
import static org.tdmx.lib.zone.domain.QDestination.destination;
import static org.tdmx.lib.zone.domain.QDomain.domain;
import static org.tdmx.lib.zone.domain.QService.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.zone.domain.Address;
import org.tdmx.lib.zone.domain.Destination;
import org.tdmx.lib.zone.domain.DestinationSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

public class DestinationDaoImpl implements DestinationDao {
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
	public void persist(Destination value) {
		em.persist(value);
	}

	@Override
	public void delete(Destination value) {
		em.remove(value);
	}

	@Override
	public Destination merge(Destination value) {
		return em.merge(value);
	}

	@Override
	public Destination loadById(Long id) {
		return new JPAQuery(em).from(destination).where(destination.id.eq(id)).uniqueResult(destination);
	}

	@Override
	public Destination loadByDestination(Address a, Service s) {
		if (a == null) {
			throw new IllegalArgumentException("missing address");
		}
		if (s == null) {
			throw new IllegalArgumentException("missing service");
		}
		JPAQuery query = new JPAQuery(em).from(destination).innerJoin(destination.target, address).fetch()
				.innerJoin(destination.service, service).fetch().where(address.eq(a).and(service.eq(s)));
		return query.uniqueResult(destination);
	}

	@Override
	public List<Destination> search(Zone zone, DestinationSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(destination).innerJoin(destination.target, address).fetch()
				.innerJoin(address.domain, domain).fetch().innerJoin(destination.service, service).fetch();

		BooleanExpression where = domain.zone.eq(zone);

		if (StringUtils.hasText(criteria.getDestination().getLocalName())) {
			where = where.and(address.localName.eq(criteria.getDestination().getLocalName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getDomainName())) {
			where = where.and(domain.domainName.eq(criteria.getDestination().getDomainName()));
		}
		if (StringUtils.hasText(criteria.getDestination().getServiceName())) {
			where = where.and(service.serviceName.eq(criteria.getDestination().getServiceName()));
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(destination);
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
