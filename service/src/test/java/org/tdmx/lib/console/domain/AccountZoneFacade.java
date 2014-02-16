package org.tdmx.lib.console.domain;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AuthorizationStatus;

public class AccountZoneFacade {

	public static AccountZone createAccountZone( PKIXCertificate zoneAdminCert ) throws Exception  {
		AccountZone az = new AccountZone();
		az.setZoneApex(zoneAdminCert.getTdmxZoneInfo().getZoneRoot());

		az.setAccountId("1234");
		az.setAuthorizationStatus(AuthorizationStatus.ACTIVE);
		az.setSegment("test");
		az.setZonePartitionId("test-zone");
		return az;
	}

}
