package org.tdmx.lib.zone.domain;



public enum CredentialType {

	ZAC, // ZoneAdministratorCredential 
	DAC, // DomainAdministratorCredential
	UC,  // UserCredential
	;
	
	public static final int MAX_CREDENTIALTYPE_LEN = 4;
}
