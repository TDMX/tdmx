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

import java.text.ParseException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plugon.lib.configuration.ConfigurationApi.Property;
import plugon.lib.configuration.ConfigurationApi.PropertyType;
import plugon.lib.configuration.local.domain.ConfigurationValue;

/**
 * @author Peter Klauser
 *
 */
public class ConfigurationUtils {

	private static Logger log = LoggerFactory.getLogger(ConfigurationUtils.class);
	
	/**
	 * Return the properties as Java Properties.
	 * 
	 * @return the java properties.
	 */
	public static Properties getJavaProperties( List<Property> properties ) {
		Properties props = new Properties();
		for( Property value : properties ) {
			Object propertyValue = getPropertyValue( value.getType(), value.getValue() );
			if ( propertyValue != null ) {
				props.put(value.getName(), propertyValue);
			} else {
				log.warn("Ignoring ConfigurationValue with name " + value.getName());
			}
		}
		return props;
	}
	
	/**
	 * Return the Property value of this object.
	 * @return
	 */
	public static Object getPropertyValue( PropertyType valueType, String stringValue ) {
		Object result = null;
		switch ( valueType ) {
		case DATE:
			try {
				result = ConfigurationValue.dateFormat.parse(stringValue);
			} catch( ParseException e ) {
				log.warn("Unable to parse date "+ stringValue, e);
			}
			break;
		case DAY:
			try {
				result = ConfigurationValue.dayFormat.parse(stringValue);
			} catch( ParseException e ) {
				log.warn("Unable to parse day "+ stringValue, e);
			}
			break;
		case INTEGER:
			try {
				result = Integer.parseInt(stringValue);
			} catch ( NumberFormatException e ) {
				log.warn("Unable to parse integer "+ stringValue, e);
			}
			break;
		case LONG:
			try {
				result = Long.parseLong(stringValue);
			} catch ( NumberFormatException e ) {
				log.warn("Unable to parse long "+ stringValue, e);
			}
			break;
		case STRING:
			result = stringValue;
			break;
		default:
			log.warn("Unsupported ValueType "+ valueType);
		}
		return result;
	}
	
	
	
}
