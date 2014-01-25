package org.tdmx.lib.control.dao;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tdmx.lib.control.domain.AuthorizedAgent;


public class AuthorizedAgentDaoImpl implements AuthorizedAgentDao {
	
	@PersistenceContext(unitName="ConfigurationRepository")
	private EntityManager em;

	@Override
	public void persist( AuthorizedAgent value ) {
		em.persist(value);
	}
	
	@Override
	public void delete( AuthorizedAgent value ) {
		em.remove(value);
	}
	
	@Override
	public void lock( AuthorizedAgent value ) {
		em.lock(value, LockModeType.WRITE);
	}
	
	@Override
	public AuthorizedAgent merge( AuthorizedAgent value ) {
		return em.merge(value);
	}
	

	@Override
	public AuthorizedAgent loadByFingerprint(String fingerprint) {
		Query query = em.createQuery("from AuthorizedAgent as cv where cv.sha1fingerprint = :fingerprint");
		query.setParameter("fingerprint", fingerprint);
		try {
			return (AuthorizedAgent)query.getSingleResult();
		} catch ( NoResultException e ) {
			return null;
		}
	}

}
