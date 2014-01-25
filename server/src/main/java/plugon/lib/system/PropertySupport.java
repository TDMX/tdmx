package plugon.lib.system;
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
	
	public static final String CONFIGURATION_PROPERTY_NAME = "plugon.configuration";
	public static final String STANDARD_FILENAME = "configuration.properties";
	
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
			instance.load();
		}
		return instance;
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------
	private void load() throws RuntimeException {
		Properties props = new Properties();
		
		String filename = EnvironmentSupport.getProperty(CONFIGURATION_PROPERTY_NAME);
		if ( filename == null ) {
			filename = STANDARD_FILENAME;
		}
		
		InputStream is = null;
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
			log.error("No configuration properties found.");
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

