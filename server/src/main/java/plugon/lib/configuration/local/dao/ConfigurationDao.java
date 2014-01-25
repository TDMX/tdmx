package plugon.lib.configuration.local.dao;

import java.util.List;

import plugon.lib.configuration.local.domain.ConfigurationValue;


public interface ConfigurationDao {
	
	public void persist( ConfigurationValue value );
	
	public void delete( ConfigurationValue value );
	
	public void lock( ConfigurationValue value );
	
	public ConfigurationValue merge( ConfigurationValue value );

	public List<ConfigurationValue> loadByFilename(String filename);
		
	public List<String> listFilenames();
}
