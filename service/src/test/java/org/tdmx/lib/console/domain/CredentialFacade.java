/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.lib.console.domain;

import java.util.Calendar;
import java.util.Date;

import org.tdmx.client.crypto.algorithm.PublicKeyAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CredentialUtils;
import org.tdmx.client.crypto.certificate.DomainAdministrationCredentialSpecifier;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.UserCredentialSpecifier;
import org.tdmx.client.crypto.certificate.ZoneAdministrationCredentialSpecifier;

public class CredentialFacade {

	public static PKIXCredential createZAC(String zoneRoot) throws Exception {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 10);
		later.set(Calendar.MILLISECOND, 0);

		ZoneAdministrationCredentialSpecifier req = new ZoneAdministrationCredentialSpecifier(1, zoneRoot,
				"https://mrsUrl/api");
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

	public static PKIXCredential createDAC(PKIXCredential zac) throws Exception {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.set(Calendar.MILLISECOND, 0);

		Calendar later = Calendar.getInstance();
		later.setTime(new Date());
		later.add(Calendar.YEAR, 2);
		later.set(Calendar.MILLISECOND, 0);

		DomainAdministrationCredentialSpecifier req = new DomainAdministrationCredentialSpecifier("subdomain", zac);
		req.setNotBefore(now);
		req.setNotAfter(later);
		req.setKeyAlgorithm(PublicKeyAlgorithm.RSA2048);
		req.setSignatureAlgorithm(SignatureAlgorithm.SHA_256_RSA);
		PKIXCredential cred = CredentialUtils.createDomainAdministratorCredential(req);

		return cred;
	}

	public static PKIXCredential createUC(PKIXCredential dac) throws Exception {
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
