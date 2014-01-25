package org.tdmx.core.system.env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Creates a static encryption utility.
 */
public class ObfuscationSupport {
	
	
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	
	public static final String CONFIGURATION_PASSPHRASE_PROPERTY = "org.tdmx.core.system.env.obfuscation.passphrase";
	public static final String STANDARD_PASSPHRASE = "Man this obfuscated stuff is just hiding in plain sight!";
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
    private static Logger log = LoggerFactory.getLogger(ObfuscationSupport.class);

	private static ObfuscationSupport instance;
	
	private StringEncrypter encrypter;
	
	public StringEncrypter getEncrypter() {
		return encrypter;
	}

	public static final String ENCRYPTED_TAG = "!!!ENCRYPTED!!!";
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	private ObfuscationSupport() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	public static final synchronized ObfuscationSupport getInstance() throws RuntimeException {
		if(instance == null) {
			String passphrase = EnvironmentSupport.getProperty(CONFIGURATION_PASSPHRASE_PROPERTY);
			if ( passphrase == null ) {
				log.warn("Creating Encrypter with standard passphrase.");
				passphrase = STANDARD_PASSPHRASE;
			} else {
				log.info("Creating Encrypter with environment variable "+CONFIGURATION_PASSPHRASE_PROPERTY+" passphrase.");
			}
			StringEncrypter enc = new StringEncrypter(passphrase);
			instance = new ObfuscationSupport(enc);
		}
		return instance;
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private ObfuscationSupport( StringEncrypter e ) {
		encrypter = e;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

}

