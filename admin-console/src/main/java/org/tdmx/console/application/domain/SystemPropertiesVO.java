package org.tdmx.console.application.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdmx.console.application.util.StringUtils;



/**
 * An object representing the System's properties.
 * 
 * @author Peter
 *
 */
public class SystemPropertiesVO implements ValueObject {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Map<String,String> properties = new HashMap<>();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public SystemPropertiesVO() {
	}
	
	public SystemPropertiesVO( List<String> propertyNames ) {
		if ( propertyNames != null ) {
			for( String name : propertyNames ) {
				try {
					String value = System.getProperty(name);
					if ( StringUtils.hasText(value) ) {
						add(name,value);
					}
				} catch ( SecurityException e ) {
					add(name,"<unknown>");
				}
			}
		}
	}
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public void add( String key, String value) {
		properties.put(key,value);
	}
	
	public String get( String key ) {
		return properties.get(key);
	}
	
	public Map<String,String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
	public Set<String> getDeletedKeys( Map<String,String> other ) {
		Set<String> deletes = new HashSet<>();
		
		//TODO
		return deletes;
	}
	
	public Set<String> getNewKeys( Map<String,String> other ) {
		Set<String> added = new HashSet<>();
		//TODO
		return added;
	}
	
	public Set<String> getModifiedKeys( Map<String,String> other ) {
		Set<String> modified = new HashSet<>();
		//TODO
		return modified;
	}
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

}
