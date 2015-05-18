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

import static org.tdmx.lib.control.domain.QLock.lock;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.lib.control.domain.Lock;

import com.mysema.query.jpa.impl.JPAQuery;

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
	public Lock merge(Lock value) {
		return em.merge(value);
	}

	@Override
	public Lock loadById(Long id) {
		return new JPAQuery(em).from(lock).where(lock.id.eq(id)).uniqueResult(lock);
	}

	@Override
	public Lock loadByName(String lockName) {
		return new JPAQuery(em).from(lock).where(lock.lockName.eq(lockName)).uniqueResult(lock);
	}

	@Override
	public List<Lock> loadAll() {
		return new JPAQuery(em).from(lock).list(lock);
	}

	@Override
	public Lock conditionalLock(String lockName) {

		Date now = new Date();

		JPAQuery query = new JPAQuery(em).from(lock).where(
				lock.lockName.eq(lockName).and(lock.lockedBy.isNull())
						.and(lock.lockedUntilTime.isNull().or(lock.lockedUntilTime.lt(now))));
		query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		return query.uniqueResult(lock);
	}

}
