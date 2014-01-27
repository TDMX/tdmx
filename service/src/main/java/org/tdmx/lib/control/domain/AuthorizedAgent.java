package org.tdmx.lib.control.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A AuthorizedAgent can be a ZoneAdministrator, DomainAdministrator or User public key.
 *
 * The AuthorizedAgent is identified by it's SHA1 fingerprint of the public certificate.
 * 
 * @author Peter Klauser
 *
 */
@Entity
@Table(name="AuthorizedAgent")
public class AuthorizedAgent implements Serializable {

	public static final int MAX_SHA1FINGERPRINT_LEN = 64;
	public static final int MAX_CERTIFICATE_LEN = 4000;
	
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@Column(length = MAX_SHA1FINGERPRINT_LEN)
	private String sha1fingerprint;
	
	@Enumerated(EnumType.STRING)
	@Column(length = AuthorizationStatus.MAX_AUTHORIZATIONSTATUS_LEN, nullable = false)
	private AuthorizationStatus authorizationStatus;
	
	@Column(length = AccountZone.MAX_ZONEAPEX_LEN, nullable = false)
	private String zoneApex;
	
	@Column(length = MAX_CERTIFICATE_LEN, nullable = false)
	private String certificatePem;
	
	
	
	
	
	public String getSha1fingerprint() {
		return sha1fingerprint;
	}

	public void setSha1fingerprint(String sha1fingerprint) {
		this.sha1fingerprint = sha1fingerprint;
	}

	public AuthorizationStatus getAuthorizationStatus() {
		return authorizationStatus;
	}

	public void setAuthorizationStatus(AuthorizationStatus authorizationStatus) {
		this.authorizationStatus = authorizationStatus;
	}

	public String getZoneApex() {
		return zoneApex;
	}

	public void setZoneApex(String zoneApex) {
		this.zoneApex = zoneApex;
	}

	public String getCertificatePem() {
		return certificatePem;
	}

	public void setCertificatePem(String certificatePem) {
		this.certificatePem = certificatePem;
	}

}
