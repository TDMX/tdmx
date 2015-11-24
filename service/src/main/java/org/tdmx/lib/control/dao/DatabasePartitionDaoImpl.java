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

import static org.tdmx.lib.control.domain.QAccount.account;
import static org.tdmx.lib.control.domain.QDatabasePartition.databasePartition;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabasePartitionSearchCriteria;

import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

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
	public DatabasePartition merge(DatabasePartition value) {
		return em.merge(value);
	}

	@Override
	public DatabasePartition loadById(Long id) {
		return new JPAQuery(em).from(databasePartition).where(databasePartition.id.eq(id))
				.uniqueResult(databasePartition);
	}

	@Override
	public DatabasePartition loadByPartitionId(String partitionId) {
		return new JPAQuery(em).from(databasePartition).where(databasePartition.partitionId.eq(partitionId))
				.uniqueResult(databasePartition);
	}

	@Override
	public List<DatabasePartition> loadAll() {
		return new JPAQuery(em).from(databasePartition).list(databasePartition);
	}

	@Override
	public List<DatabasePartition> search(DatabasePartitionSearchCriteria criteria) {
		JPAQuery query = new JPAQuery(em).from(databasePartition);

		BooleanExpression where = null;
		if (StringUtils.hasText(criteria.getPartitionId())) {
			BooleanExpression e = databasePartition.partitionId.eq(criteria.getPartitionId());
			where = where != null ? where.and(e) : e;
		}
		if (StringUtils.hasText(criteria.getSegment())) {
			BooleanExpression e = databasePartition.segment.eq(criteria.getSegment());
			where = where != null ? where.and(e) : e;
		}
		if (criteria.getDbType() != null) {
			BooleanExpression e = databasePartition.dbType.eq(criteria.getDbType());
			where = where != null ? where.and(e) : e;
		}

		query.where(where);
		query.restrict(new QueryModifiers((long) criteria.getPageSpecifier().getMaxResults(), (long) criteria
				.getPageSpecifier().getFirstResult()));
		return query.list(databasePartition);
	}

}
