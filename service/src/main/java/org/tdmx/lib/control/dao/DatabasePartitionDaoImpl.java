package org.tdmx.lib.control.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.lib.control.domain.DatabasePartition;


public class DatabasePartitionDaoImpl implements DatabasePartitionDao {
	
	@PersistenceContext(unitName="ControlDB")
	private EntityManager em;

	@Override
	public void persist( DatabasePartition value ) {
		em.persist(value);
	}
	
	@Override
	public void delete( DatabasePartition value ) {
		em.remove(value);
	}
	
	@Override
	public void lock( DatabasePartition value ) {
		em.lock(value, LockModeType.WRITE);
	}
	
	@Override
	public DatabasePartition merge( DatabasePartition value ) {
		return em.merge(value);
	}
	

	@Override
	public DatabasePartition loadById(String id) {
		Query query = em.createQuery("from DatabasePartition as dp where dp.partitionId = :id");
		query.setParameter("id", id);
		try {
			return (DatabasePartition)query.getSingleResult();
		} catch ( NoResultException e ) {
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
