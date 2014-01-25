package plugon.lib.configuration.local.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import plugon.lib.configuration.local.domain.ConfigurationValue;

public class ConfigurationDaoImpl implements ConfigurationDao {
	
	@PersistenceContext(unitName="ConfigurationRepository")
	private EntityManager em;

	@Override
	public void persist( ConfigurationValue value ) {
		em.persist(value);
	}
	
	@Override
	public void delete( ConfigurationValue value ) {
		em.remove(value);
	}
	
	@Override
	public void lock( ConfigurationValue value ) {
		em.lock(value, LockModeType.WRITE);
	}
	
	@Override
	public ConfigurationValue merge( ConfigurationValue value ) {
		return em.merge(value);
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public List<ConfigurationValue> loadByFilename(String filename) {
		Query query = em.createQuery("from ConfigurationValue as cv where cv.filename = :filename");
		query.setParameter("filename", filename);
		return (List<ConfigurationValue>)query.getResultList();
	}

	/* (non-Javadoc)
	 * @see plugon.lib.configuration.local.dao.ConfigurationDao#listFilenames()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> listFilenames() {
		Query query = em.createQuery("select distinct cv.filename from ConfigurationValue as cv");
		return query.getResultList();
	}


}
