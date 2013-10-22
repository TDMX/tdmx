package org.tdmx.console.application.domain;


/**
 * A ServiceProvider.
 * 
 * @author Peter
 *
 */
public class ServiceProviderDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	public static final int STATUS_IRRELEVANT = -2;
	public static final int STATUS_UNCHECKED = -1;
	public static final int STATUS_OK = 0;
	public static final int STATUS_UNKNOWN_HOST = 1;
	public static final int STATUS_UNREACHABLE_HOST = 2;
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String subjectIdentifier;
	private int version;
	
	private String mrsHostname;
	private int mrsPort;
	private int mrsStatus;
	
	private String masHostname;
	private int masPort;
	private int masStatus;

	private String mosHostname;
	private int mosPort;
	private int mosStatus;

	private String mdsHostname;
	private int mdsPort;
	private int mdsStatus;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((subjectIdentifier == null) ? 0 : subjectIdentifier
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceProviderDO other = (ServiceProviderDO) obj;
		if (subjectIdentifier == null) {
			if (other.subjectIdentifier != null)
				return false;
		} else if (!subjectIdentifier.equals(other.subjectIdentifier))
			return false;
		return true;
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

	public String getSubjectIdentifier() {
		return subjectIdentifier;
	}

	public void setSubjectIdentifier(String subjectIdentifier) {
		this.subjectIdentifier = subjectIdentifier;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getMrsHostname() {
		return mrsHostname;
	}

	public void setMrsHostname(String mrsHostname) {
		this.mrsHostname = mrsHostname;
	}

	public int getMrsPort() {
		return mrsPort;
	}

	public void setMrsPort(int mrsPort) {
		this.mrsPort = mrsPort;
	}

	public int getMrsStatus() {
		return mrsStatus;
	}

	public void setMrsStatus(int mrsStatus) {
		this.mrsStatus = mrsStatus;
	}

	public String getMasHostname() {
		return masHostname;
	}

	public void setMasHostname(String masHostname) {
		this.masHostname = masHostname;
	}

	public int getMasPort() {
		return masPort;
	}

	public void setMasPort(int masPort) {
		this.masPort = masPort;
	}


	public int getMasStatus() {
		return masStatus;
	}

	public void setMasStatus(int masStatus) {
		this.masStatus = masStatus;
	}

	public String getMosHostname() {
		return mosHostname;
	}

	public void setMosHostname(String mosHostname) {
		this.mosHostname = mosHostname;
	}

	public int getMosPort() {
		return mosPort;
	}

	public void setMosPort(int mosPort) {
		this.mosPort = mosPort;
	}


	public int getMosStatus() {
		return mosStatus;
	}

	public void setMosStatus(int mosStatus) {
		this.mosStatus = mosStatus;
	}

	public String getMdsHostname() {
		return mdsHostname;
	}

	public void setMdsHostname(String mdsHostname) {
		this.mdsHostname = mdsHostname;
	}

	public int getMdsPort() {
		return mdsPort;
	}

	public void setMdsPort(int mdsPort) {
		this.mdsPort = mdsPort;
	}

	public int getMdsStatus() {
		return mdsStatus;
	}

	public void setMdsStatus(int mdsStatus) {
		this.mdsStatus = mdsStatus;
	}

}
