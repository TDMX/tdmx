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

import static org.tdmx.lib.zone.domain.QZone.zone;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.common.domain.ZoneReference;
import org.tdmx.lib.zone.domain.Zone;

import com.mysema.query.jpa.impl.JPAQuery;

public class ZoneDaoImpl implements ZoneDao {

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
	public void persist(Zone value) {
		em.persist(value);
	}

	@Override
	public void delete(Zone value) {
		em.remove(value);
	}

	@Override
	public void lock(Zone value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public Zone merge(Zone value) {
		return em.merge(value);
	}

	@Override
	public Zone loadById(Long id) {
		return new JPAQuery(em).from(zone).where(zone.id.eq(id)).uniqueResult(zone);
	}

	@Override
	public Zone loadByZoneApex(ZoneReference zoneReference) {
		if (zoneReference.getTenantId() == null) {
			throw new IllegalArgumentException("missing tenantId");
		}
		if (!StringUtils.hasText(zoneReference.getZoneApex())) {
			throw new IllegalArgumentException("missing zoneApex");
		}
		return new JPAQuery(em)
				.from(zone)
				.where(zone.tenantId.eq(zoneReference.getTenantId()).and(zone.zoneApex.eq(zoneReference.getZoneApex())))
				.uniqueResult(zone);
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
