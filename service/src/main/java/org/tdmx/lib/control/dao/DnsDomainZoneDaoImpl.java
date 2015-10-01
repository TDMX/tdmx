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

import static org.tdmx.lib.control.domain.QDnsDomainZone.dnsDomainZone;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.lib.control.domain.DnsDomainZone;

import com.mysema.query.jpa.impl.JPAQuery;

public class DnsDomainZoneDaoImpl implements DnsDomainZoneDao {

	@PersistenceContext(unitName = "ControlDB")
	private EntityManager em;

	@Override
	public void persist(DnsDomainZone value) {
		em.persist(value);
	}

	@Override
	public void delete(DnsDomainZone value) {
		em.remove(value);
	}

	@Override
	public DnsDomainZone merge(DnsDomainZone value) {
		return em.merge(value);
	}

	@Override
	public DnsDomainZone loadById(Long id) {
		return new JPAQuery(em).from(dnsDomainZone).where(dnsDomainZone.id.eq(id)).uniqueResult(dnsDomainZone);
	}

	@Override
	public DnsDomainZone loadByCurrentDomain(String dn) {
		Date now = new Date();
		return new JPAQuery(em).from(dnsDomainZone).where(dnsDomainZone.domainName.eq(dn)
				.and(dnsDomainZone.validFromTime.lt(now)).and(dnsDomainZone.validUntilTime.after(now)))
				.uniqueResult(dnsDomainZone);
	}

	@Override
	public List<DnsDomainZone> loadByDomain(String dn) {
		return new JPAQuery(em).from(dnsDomainZone).where(dnsDomainZone.domainName.eq(dn))
				.orderBy(dnsDomainZone.validUntilTime.desc()).list(dnsDomainZone);
	}

}
