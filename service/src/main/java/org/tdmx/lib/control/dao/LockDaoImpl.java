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

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.lib.control.domain.Lock;

public class LockDaoImpl implements LockDao {

	@PersistenceContext(unitName = "ControlDB")
	private EntityManager em;

	@Override
	public void persist(Lock value) {
		em.persist(value);
	}

	@Override
	public void delete(Lock value) {
		em.remove(value);
	}

	@Override
	public void lock(Lock value) {
		em.lock(value, LockModeType.WRITE);
	}

	@Override
	public Lock merge(Lock value) {
		return em.merge(value);
	}

	@Override
	public Lock loadById(Long id) {
		Query query = em.createQuery("from Lock as l where l.id = :id");
		query.setParameter("id", id);
		try {
			return (Lock) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Lock loadByName(String lockName) {
		Query query = em.createQuery("from Lock as l where l.lockName = :n");
		query.setParameter("n", lockName);
		try {
			return (Lock) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Lock> loadAll() {
		Query query = em.createQuery("from Lock as l");
		return query.getResultList();
	}

	@Override
	public Lock conditionalLock(String lockName) {
		Date now = new Date();
		Query query = em
				.createQuery("from Lock as l where l.lockName = :n and l.lockedBy is null and ( l.lockedUntilTime is null or l.lockedUntilTime < :t )");
		query.setParameter("n", lockName);
		query.setParameter("t", now);
		query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		try {
			return (Lock) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
