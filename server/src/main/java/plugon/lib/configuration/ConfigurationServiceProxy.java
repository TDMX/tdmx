package plugon.lib.configuration;

import plugon.lib.configuration.ConfigurationApi.GetPropertiesRequest;
import plugon.lib.configuration.ConfigurationApi.GetPropertiesResponse;
import plugon.lib.configuration.ConfigurationApi.GetPropertyFilesRequest;
import plugon.lib.configuration.ConfigurationApi.GetPropertyFilesResponse;
import plugon.lib.configuration.ConfigurationApi.SetPropertiesRequest;
import plugon.lib.configuration.ConfigurationApi.SetPropertiesResponse;



/**
 * A Runtime-Configuration Management Service. Properties which can be changed dynamically without rollout need
 * configuration via this service and not via property files included with the JVMs.
 * 
 * The ServiceProxy is a local interface, which can be used on-top of an RPC implementation or a Local
 * implementation. 
 * 
 * If used on top of an RPC implementation, a ServiceProxy will hide RPC timeout details from the client code.
 * The client code doesn't need to worry about RPC call cancellation.
 * 
 * @author Peter Klauser
 *
 */
public interface ConfigurationServiceProxy {

	/**
	 * Get a list of all Configuration files.
	 * 
	 * @return list of configuration filenames.
	 */
	public GetPropertyFilesResponse getPropertyFilenames( GetPropertyFilesRequest request);

	/**
	 * Get runtime configuration properties for a given property filename.
	 * 
	 * NOTE: clients should cache the properties according to their requirements. This 
	 * method will always retrieve the current configuration.
	 * 
	 * @param filename
	 * @return runtime properties.
	 */
	public GetPropertiesResponse getProperties( GetPropertiesRequest request );
	
	/**
	 * Set all properties in a configuration file.
	 * 
	 * @return list of configuration filenames.
	 */
	public SetPropertiesResponse setProperties( SetPropertiesRequest request );

}