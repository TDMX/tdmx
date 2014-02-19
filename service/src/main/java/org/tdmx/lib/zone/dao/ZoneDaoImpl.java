package org.tdmx.lib.zone.dao;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.lib.zone.domain.Zone;


public class ZoneDaoImpl implements ZoneDao {
	
	@PersistenceContext(unitName="ZoneDB")
	private EntityManager em;

	@Override
	public void persist( Zone value ) {
		em.persist(value);
	}
	
	@Override
	public void delete( Zone value ) {
		em.remove(value);
	}
	
	@Override
	public void lock( Zone value ) {
		em.lock(value, LockModeType.WRITE);
	}
	
	@Override
	public Zone merge( Zone value ) {
		return em.merge(value);
	}
	

	@Override
	public Zone loadById(String id) {
		Query query = em.createQuery("from Zone as z where z.zoneApex = :id");
		query.setParameter("id", id);
		try {
			return (Zone)query.getSingleResult();
		} catch ( NoResultException e ) {
			return null;
		}
	}

}
