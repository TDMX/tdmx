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

import org.tdmx.lib.control.domain.MaxValue;

// TODO querydsl
public class MaxValueDaoImpl implements MaxValueDao {

	@PersistenceContext(unitName = "ControlDB")
	private EntityManager em;

	@Override
	public void persist(MaxValue value) {
		em.persist(value);
	}

	@Override
	public void merge(MaxValue value) {
		em.merge(value);
	}

	@Override
	public void delete(MaxValue value) {
		em.remove(value);
	}

	@Override
	public MaxValue lockById(String id) {
		Query query = em.createQuery("from MaxValue as m where m.key = :id");
		query.setParameter("id", id);
		query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		try {
			return (MaxValue) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MaxValue> loadAll() {
		Query query = em.createQuery("from MaxValue as m");
		return query.getResultList();
	}

}
