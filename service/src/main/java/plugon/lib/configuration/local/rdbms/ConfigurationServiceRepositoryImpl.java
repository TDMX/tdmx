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
package plugon.lib.configuration.local.rdbms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import plugon.lib.configuration.ConfigurationApi.GetPropertiesRequest;
import plugon.lib.configuration.ConfigurationApi.GetPropertiesResponse;
import plugon.lib.configuration.ConfigurationApi.GetPropertyFilesRequest;
import plugon.lib.configuration.ConfigurationApi.GetPropertyFilesResponse;
import plugon.lib.configuration.ConfigurationApi.Property;
import plugon.lib.configuration.ConfigurationApi.SetPropertiesRequest;
import plugon.lib.configuration.ConfigurationApi.SetPropertiesResponse;
import plugon.lib.configuration.ConfigurationServiceProxy;
import plugon.lib.configuration.local.dao.ConfigurationDao;
import plugon.lib.configuration.local.domain.ConfigurationValue;

/**
 * @author Peter Klauser
 *
 */
public class ConfigurationServiceRepositoryImpl implements ConfigurationServiceProxy {

	private static Logger log = LoggerFactory.getLogger(ConfigurationServiceRepositoryImpl.class);
	 
	private ConfigurationDao configurationDao;
	
	/* (non-Javadoc)
	 * @see plugon.lib.configuration.ConfigurationServiceProxy#getPropertyFilenames(plugon.lib.configuration.ConfigurationApi.GetPropertyFilesRequest)
	 */
	@Override
	@Transactional(value="ConfigurationDB", readOnly=true)
	public GetPropertyFilesResponse getPropertyFilenames(GetPropertyFilesRequest request) {
		
		List<String> list = getConfigurationDao().listFilenames();
		GetPropertyFilesResponse.Builder response = GetPropertyFilesResponse.newBuilder().setErrorCode(GetPropertyFilesResponse.ErrorCode.SUCCESS);
		for( String filename : list ) {
			response.addFilename(filename);
		}
		return response.build();
	}


	/* (non-Javadoc)
	 * @see plugon.lib.configuration.ConfigurationServiceProxy#getProperties(plugon.lib.configuration.ConfigurationApi.GetPropertiesRequest)
	 */
	@Override
	@Transactional(value="ConfigurationDB", readOnly=true)
	public GetPropertiesResponse getProperties(GetPropertiesRequest request) {
		GetPropertiesResponse.Builder response = GetPropertiesResponse.newBuilder().setErrorCode(GetPropertiesResponse.ErrorCode.SUCCESS);
		if ( request.getFilename() == null ) {
			response.setErrorCode(GetPropertiesResponse.ErrorCode.FAILURE_MISSING_INFO);
			return response.build();
		}
		
		String filename = request.getFilename();
		
		List<ConfigurationValue> result = getConfigurationDao().loadByFilename(filename);
		
		for( ConfigurationValue value : result ) {
			Property prop = ConfigurationValue.mapFrom(filename, value).build();
			response.addProperties(prop);
		}
		return response.build();
	}

	/* (non-Javadoc)
	 * @see plugon.lib.configuration.ConfigurationServiceProxy#setProperties(plugon.lib.configuration.ConfigurationApi.SetPropertiesRequest)
	 */
	@Override
	@Transactional("ConfigurationDB")
	public SetPropertiesResponse setProperties(SetPropertiesRequest request) {
		SetPropertiesResponse.Builder response = SetPropertiesResponse.newBuilder().setErrorCode(SetPropertiesResponse.ErrorCode.SUCCESS);

		String filename = request.getFilename();
		if ( filename == null ) {
			response.setErrorCode(SetPropertiesResponse.ErrorCode.FAILURE_MISSING_INFO);
			return response.build();
		}
		
		List<ConfigurationValue> passedList = new ArrayList<ConfigurationValue>();
		if ( request.getPropertiesCount() > 0 ) {
			for( Property p : request.getPropertiesList() ) {
				passedList.add(ConfigurationValue.mapFrom(filename, p));
			}
		}
		// passedList contains properties
		
		List<ConfigurationValue> persistedList = getConfigurationDao().loadByFilename(filename);
		
		for( ConfigurationValue value : passedList ) {
			ConfigurationValue existingValue = findByName( persistedList, value.getPropertyName() );
			if ( existingValue != null ) {
				getConfigurationDao().merge(value); // modify
			} else {
				getConfigurationDao().persist(value); // add
				persistedList.add(value);
			}
		}
		
		for( ConfigurationValue value : persistedList ) {
			ConfigurationValue passedValue = findByName( passedList, value.getPropertyName() );
			if ( passedValue == null ) {
				getConfigurationDao().delete(value); // delete
			}
		}
		return response.build();
	}

	private ConfigurationValue findByName( List<ConfigurationValue> list, String propertyName ) {
		for( ConfigurationValue value : list ) {
			if ( propertyName.equals(value.getPropertyName())) {
				return value;
			}
		}
		return null;
	}
	
	/**
	 * @return the configurationDao
	 */
	public ConfigurationDao getConfigurationDao() {
		return configurationDao;
	}

	/**
	 * @param configurationDao the configurationDao to set
	 */
	public void setConfigurationDao(ConfigurationDao configurationDao) {
		this.configurationDao = configurationDao;
	}

}
