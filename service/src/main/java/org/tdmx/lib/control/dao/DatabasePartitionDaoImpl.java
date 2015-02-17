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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.lib.control.domain.DatabasePartition;

public class DatabasePartitionDaoImpl implements DatabasePartitionDao {

	@PersistenceContext(unitName = "ControlDB")
	private EntityManager em;

	@Override
	public void persist(DatabasePartition value) {
		em.persist(value);
	}

	@Override
	public void delete(DatabasePartition value) {
		em.remove(value);
	}

	@Override
	public void lock(DatabasePartition value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public DatabasePartition merge(DatabasePartition value) {
		return em.merge(value);
	}

	@Override
	public DatabasePartition loadById(Long id) {
		Query query = em.createQuery("from DatabasePartition as dp where dp.id = :id");
		query.setParameter("id", id);
		try {
			return (DatabasePartition) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public DatabasePartition loadByPartitionId(String partitionId) {
		Query query = em.createQuery("from DatabasePartition as dp where dp.partitionId = :id");
		query.setParameter("id", partitionId);
		try {
			return (DatabasePartition) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DatabasePartition> loadAll() {
		Query query = em.createQuery("from DatabasePartition as dp");
		return query.getResultList();
	}

}
