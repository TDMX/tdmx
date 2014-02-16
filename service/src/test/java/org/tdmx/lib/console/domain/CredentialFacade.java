package org.tdmx.lib.console.domain;

import java.util.Calendar;
import java.util.Date;

import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.DomainAdministrationCredentialSpecifier;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.TdmxZoneInfo;
import org.tdmx.client.crypto.certificate.UserCredentialSpecifier;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;

public class CredentialFacade {

	public static PKIXCredential createZAC( String zoneRoot ) throws Exception  {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 10);
		later.set(Calendar.MILLISECOND, 0);

		TdmxZoneInfo zi = new TdmxZoneInfo(1, zoneRoot, "https://mrsUrl/api");
		
		ZoneAdministrationCredentialSpecifier req = new ZoneAdministrationCredentialSpecifier();
		req.setZoneInfo(zi);
		
		req.setCn("name");
		req.setTelephoneNumber("0417100000");
		req.setEmailAddress("pjk@gmail.com");
		req.setOrgUnit("IT");
		req.setOrg("mycompany");
		req.setLocation("Zug");
		req.setCountry("CH");
		req.setNotBefore(now);
		req.setNotAfter(later);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CredentialUtils.createZoneAdministratorCredential(req);

		return cred;
	}

	public static PKIXCredential createDAC( PKIXCredential zac ) throws Exception {
		PKIXCertificate issuer = zac.getPublicCert();

		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 2);
		later.set(Calendar.MILLISECOND, 0);

		DomainAdministrationCredentialSpecifier req = new DomainAdministrationCredentialSpecifier();
		req.setZoneAdministratorCredential(zac);
		req.setDomainName("subdomain."+issuer.getTdmxZoneInfo().getZoneRoot());
		req.setNotBefore(now);
		req.setNotAfter(later);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CredentialUtils.createDomainAdministratorCredential(req);
		
		return cred;
	}
	
	public static PKIXCredential createUC( PKIXCredential dac ) throws Exception {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 1);
		later.set(Calendar.MILLISECOND, 0);

		UserCredentialSpecifier req = new UserCredentialSpecifier();
		req.setDomainAdministratorCredential(dac);
		req.setName("username123");
		req.setNotBefore(now);
		req.setNotAfter(later);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CredentialUtils.createUserCredential(req);
		
		return cred;
	}
	
}
