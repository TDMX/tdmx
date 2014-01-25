/**
 *   Copyright 2010 Peter Klauser
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
*/
package plugon.lib.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plugon.lib.configuration.ConfigurationApi.GetPropertiesRequest;
import plugon.lib.configuration.ConfigurationApi.GetPropertiesResponse;
import plugon.lib.configuration.ConfigurationApi.GetPropertyFilesRequest;
import plugon.lib.configuration.ConfigurationApi.GetPropertyFilesResponse;
import plugon.lib.configuration.ConfigurationApi.SetPropertiesRequest;
import plugon.lib.configuration.ConfigurationApi.SetPropertiesResponse;


/**
 * @author Peter Klauser
 *
 */
public class ConfigurationServiceProxyImpl implements ConfigurationServiceProxy {
	private static Logger log = LoggerFactory.getLogger(ConfigurationServiceProxyImpl.class);
	
	private ConfigurationServiceProxy delegate;

	/* (non-Javadoc)
	 * @see plugon.lib.configuration.ConfigurationServiceProxy#getPropertyFilenames()
	 */
	@Override
	public GetPropertyFilesResponse getPropertyFilenames(GetPropertyFilesRequest request) {
		try {
			return delegate.getPropertyFilenames(request);
		} catch ( Exception e ) {
			log.warn("Unable to getPropertyFilenames.", e );
			GetPropertyFilesResponse.Builder response = GetPropertyFilesResponse.newBuilder();
			response.setErrorCode(GetPropertyFilesResponse.ErrorCode.FAILURE_TECHNICAL);
			return response.build();
		}
	}

	/* (non-Javadoc)
	 * @see plugon.lib.configuration.ConfigurationServiceProxy#getProperties(java.lang.String)
	 */
	@Override
	public GetPropertiesResponse getProperties(GetPropertiesRequest request) {
		try {
			return delegate.getProperties(request);
		} catch ( Exception e ) {
			log.warn("Unable to getProperties.", e );
			GetPropertiesResponse.Builder response = GetPropertiesResponse.newBuilder();
			response.setErrorCode(GetPropertiesResponse.ErrorCode.FAILURE_TECHNICAL);
			return response.build();
		}
	}

	/* (non-Javadoc)
	 * @see plugon.lib.configuration.ConfigurationServiceProxy#setProperties(java.lang.String, java.util.List)
	 */
	@Override
	public SetPropertiesResponse setProperties(SetPropertiesRequest request) {
		try {
			return delegate.setProperties(request);
		} catch ( Exception e ) {
			log.warn("Unable to setProperties.", e );
			SetPropertiesResponse.Builder response = SetPropertiesResponse.newBuilder();
			response.setErrorCode(SetPropertiesResponse.ErrorCode.FAILURE_TECHNICAL);
			return response.build();
		}
	}
	
	/**
	 * @return the delegate
	 */
	public ConfigurationServiceProxy getDelegate() {
		return delegate;
	}

	/**
	 * @param delegate the delegate to set
	 */
	public void setDelegate(ConfigurationServiceProxy delegate) {
		this.delegate = delegate;
	}

}
