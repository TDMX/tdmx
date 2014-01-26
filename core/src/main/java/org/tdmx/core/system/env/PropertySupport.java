package org.tdmx.core.system.env;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Loads configuration properties.
 * 
 * NOTE: only unencrypted property values are logged fully.
 */
public class PropertySupport {
	
	
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	
	public static final String CONFIGURATION_PROPERTY_NAME = "org.tdmx.core.system.env.configurationfile";
	public static final String STANDARD_FILENAME = "tdmx-configuration.properties";
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
    private static Logger log = LoggerFactory.getLogger(PropertySupport.class);

	private static PropertySupport instance;
	
	private Properties properties;
	
	public static final String ENCRYPTED_TAG = "!!!ENCRYPTED!!!";
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	private PropertySupport() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	public static final String getProperty(String key) {
		return getInstance().getProperties().getProperty(key);
	}
	
	public static final synchronized PropertySupport getInstance() throws RuntimeException {
		if(instance == null) {
			instance = new PropertySupport();
			instance.load(null);
		}
		return instance;
	}
	
	public static final synchronized PropertySupport getInstance(String filename) throws RuntimeException {
		if(instance == null) {
			instance = new PropertySupport();
			instance.load(filename);
		}
		return instance;
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------
	private void load( String filename ) throws RuntimeException {
		Properties props = new Properties();
		
		if ( filename == null ) {
			filename = EnvironmentSupport.getProperty(CONFIGURATION_PROPERTY_NAME);
			if ( filename == null ) {
				filename = STANDARD_FILENAME;
			}
		}
		log.info("Loading configuration from " + filename);
		
		InputStream is = null;
		try {
			try {
				is = new FileInputStream(filename);
			} catch ( FileNotFoundException fnfe ) {
				log.info("Standard configuration file ["+filename+"] not found. Trying classpath resource.");
				is = getClass().getClassLoader().getResourceAsStream(filename);
			}
			
			if ( is != null ) {
				try {
					props.load(is);
				} catch (IOException e) {
					throw new RuntimeException("cannot load properties from ["+filename+"]", e);
				}
				
				for(Map.Entry<Object, Object> entry : props.entrySet()) {
					String value = EnvironmentSupport.expandVars(entry.getValue().toString());
					
					log.info("Property " + entry.getKey() + "=" + value);
					
					if ( value.startsWith(ENCRYPTED_TAG)) {
						log.info("Decrypting encrypted Property " + entry.getKey());
						value = ObfuscationSupport.getInstance().getEncrypter().decrypt(value.substring(ENCRYPTED_TAG.length()));
					}
					entry.setValue(value);
				}
			} else {
				log.error("No configuration properties [" + filename + "] found.");
			}
		} finally {
			if ( is != null ) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		
		properties = props;
	}

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}
}

