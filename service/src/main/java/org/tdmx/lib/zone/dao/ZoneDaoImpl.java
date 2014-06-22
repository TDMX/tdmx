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

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.lib.zone.domain.Zone;

public class ZoneDaoImpl implements ZoneDao {

	@PersistenceContext(unitName = "ZoneDB")
	private EntityManager em;

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
	public Zone loadById(String id) {
		Query query = em.createQuery("from Zone as z where z.zoneApex = :id");
		query.setParameter("id", id);
		try {
			return (Zone) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
