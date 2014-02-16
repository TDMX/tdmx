package org.tdmx.lib.control.dao;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.lib.control.domain.AccountZone;


public class AccountZoneDaoImpl implements AccountZoneDao {
	
	@PersistenceContext(unitName="ControlDB")
	private EntityManager em;

	@Override
	public void persist( AccountZone value ) {
		em.persist(value);
	}
	
	@Override
	public void delete( AccountZone value ) {
		em.remove(value);
	}
	
	@Override
	public void lock( AccountZone value ) {
		em.lock(value, LockModeType.WRITE);
	}
	
	@Override
	public AccountZone merge( AccountZone value ) {
		return em.merge(value);
	}
	

	@Override
	public AccountZone loadById(String id) {
		Query query = em.createQuery("from AccountZone as az where az.zoneApex = :id");
		query.setParameter("id", id);
		try {
			return (AccountZone)query.getSingleResult();
		} catch ( NoResultException e ) {
			return null;
		}
	}

}
