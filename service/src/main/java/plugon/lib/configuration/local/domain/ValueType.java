package plugon.lib.configuration.local.domain;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plugon.lib.configuration.ConfigurationApi;

public enum ValueType {

	STRING, 
	INTEGER, 
	LONG, 
	DAY, // YYYY-MM-DD
	DATE, // YYYY-MM-DD HH:mm:ss z
	;
	
	private static Logger log = LoggerFactory.getLogger(ValueType.class);

	public static ValueType mapFrom( ConfigurationApi.PropertyType status ) {
		if ( status == null ) {
			return null;
		}
		switch( status ) {
		case STRING:
			return STRING;
		case INTEGER:
			return INTEGER;
		case LONG:
			return LONG;
		case DAY:
			return DAY;
		case DATE:
			return DATE;
		}
		log.warn("Unable to map ValueType from " + status);
		return null;
	}

	public static ConfigurationApi.PropertyType mapFrom( ValueType status ) {
		if ( status == null ) {
			return null;
		}
		switch( status ) {
		case STRING:
			return ConfigurationApi.PropertyType.STRING;
		case INTEGER:
			return ConfigurationApi.PropertyType.INTEGER;
		case LONG:
			return ConfigurationApi.PropertyType.LONG;
		case DAY:
			return ConfigurationApi.PropertyType.DAY;
		case DATE:
			return ConfigurationApi.PropertyType.DATE;
		}
		log.warn("Unable to map ValueType from " + status);
		return null;
	}
}
