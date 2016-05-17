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

import static org.tdmx.lib.control.domain.QMaxValue.maxValue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.tdmx.lib.control.domain.MaxValue;

import com.mysema.query.jpa.impl.JPAQuery;

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
		JPAQuery query = new JPAQuery(em).from(maxValue).where(maxValue.key.eq(id));
		query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		return query.uniqueResult(maxValue);
	}

	@Override
	public List<MaxValue> loadAll() {
		return new JPAQuery(em).from(maxValue).list(maxValue);
	}

}
