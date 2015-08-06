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

import static org.tdmx.lib.zone.domain.QDomain.domain;
import static org.tdmx.lib.zone.domain.QService.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.zone.domain.Domain;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.ServiceSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

public class ServiceDaoImpl implements ServiceDao {
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
	public void persist(Service value) {
		em.persist(value);
	}

	@Override
	public void delete(Service value) {
		em.remove(value);
	}

	@Override
	public Service merge(Service value) {
		return em.merge(value);
	}

	@Override
	public Service loadById(Long id) {
		return new JPAQuery(em).from(service).where(service.id.eq(id)).uniqueResult(service);
	}

	@Override
	public List<Service> search(Zone zone, ServiceSearchCriteria criteria) {
		if (zone == null) {
			throw new IllegalArgumentException("missing zone");
		}
		JPAQuery query = new JPAQuery(em).from(service).innerJoin(service.domain, domain).fetch();

		BooleanExpression where = domain.zone.eq(zone);
		if (criteria.getDomain() != null) {
			where = where.and(domain.eq(criteria.getDomain()));
		}
		if (StringUtils.hasText(criteria.getDomainName())) {
			where = where.and(domain.domainName.eq(criteria.getDomainName()));
		}
		if (StringUtils.hasText(criteria.getServiceName())) {
			where = where.and(service.serviceName.eq(criteria.getServiceName()));
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(service);

	}

	@Override
	public Service loadByName(Domain domain, String serviceName) {
		if (domain == null) {
			throw new IllegalArgumentException("missing domain");
		}
		if (!StringUtils.hasText(serviceName)) {
			throw new IllegalArgumentException("missing serviceName");
		}
		JPAQuery query = new JPAQuery(em).from(service).innerJoin(service.domain).fetch();

		BooleanExpression where = service.domain.eq(domain).and(service.serviceName.eq(serviceName));

		query.where(where);
		return query.uniqueResult(service);
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
