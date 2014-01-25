package plugon.lib.system;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Creates a static encryption utility.
 */
public class ObfuscationSupport {
	
	
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	
	public static final String CONFIGURATION_PASSPHRASE = "plugon.passphrase";
	public static final String STANDARD_PASSPHRASE = "Man this configuration stuff really gets on my nerves!";
	
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
			String passphrase = EnvironmentSupport.getProperty(CONFIGURATION_PASSPHRASE);
			if ( passphrase == null ) {
				log.warn("Creating Encrypter with standard passphrase.");
				passphrase = STANDARD_PASSPHRASE;
			} else {
				log.info("Creating Encrypter with environment variable "+CONFIGURATION_PASSPHRASE+" passphrase.");
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

