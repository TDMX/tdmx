Step 1: create a zone, initially scsUrl is not known.

ClientCLI> zone:create zone=z2.tdmx.org exec

confirm with 

ClientCLI> zone:describe exec
	zone=z2.tdmx.org
	scsUrl=null
	version=1

Step 2: create a zone administrator

ClientCLI> zoneadmin:create password=changeme name="Peter Klauser" email=pjklauser@gmail.com telephone="041..." location=Zug country=CH department=IT organization=OrgName exec
	zone=z2.tdmx.org
	certificate=-----BEGIN CERTIFICATE-----
	MIIGdTCCBF+gAwIBAgIBATALBgkqhkiG9w0BAQwwgYcxCzAJBgNVBAYTAkNIMQww
	CgYDVQQHDANadWcxEDAOBgNVBAoMB09yZ05hbWUxCzAJBgNVBAsMAklUMSIwIAYJ
	...
	ifFs5TXw/tj6o6lEBvPBUhQ4sylHRDNr/UhcMz4i5y439yzBETcQQlgVkUWaJzXe
	yTEkJaFe73tvh1vUUePFhB8rPTMsfu59gQ==
	-----END CERTIFICATE-----
	fingerprint=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c

confirm with 

ClientCLI> zoneadmin:describe zacPassword=changeme exec


Step 3: create an account at a service provider

ServerAdmin> account:create email=pjklauser@gmail.com firstName=z2 lastName="Zone2 Example" exec
           >Account; 50; 1000000117; pjklauser@gmail.com; z2; Zone2 Example
	
confirm with 
ServerAdmin> account:search email=pjklauser@gmail.com exec
           >Account; 50; 1000000117; pjklauser@gmail.com; z2; Zone2 Example


Step 3: choose a segment for the account's zone

ServerAdmin> segment:search exec
           >Segment; 1; s1; https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
           >Segment; 2; s2; https://segment2.scs.tdmx.org:8444/scs/v1.0/scs
           
Step 5: create an account zone at the service provider in one of the segments.

ServerAdmin> zone:create account=1000000117 zone=z2.tdmx.org segment=s1 exec
           >AccountZone; 60; 1000000117; z2.tdmx.org; s1; s1_zdb2; ACTIVE; 70
           

Step 6: modify the zone's scsUrl to point to the service provider.

ClientCLI> zone:modify scsUrl=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs exec
         >zone=z2.tdmx.org
         >scsUrl=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs

Step 7: figure out what the zone's DNS descriptor should contain

ClientCLI> dns:describe zacPassword=changeme exec
         >The following line contains the expected DNS TXT record contents for the zone z2.tdmx.org
         >tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
         
Step 8: provision DNS with a TXT record for the hostedzone

ClientCLI> dns:route53 zacPassword=changeme awsHostedZoneName=tdmx.org awsRegion=eu-west-1 exec

check with
ClientCLI> dns:check zacPassword=changeme exec
         >The following line contains the expected DNS TXT record contents for the zone z2.tdmx.org
         >tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
         >Found
         >tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs

         
Step 9: setup the zone administrator at the service provider.

ClientCLI> zoneadmin:describe zacPassword=changeme exec
ClientCLI>certificate=-----BEGIN CERTIFICATE-----
ClientCLI>MIIGdTCCBF+gAwIBAgIBATALBgkqhkiG9w0BAQwwgYcxCzAJBgNVBAYTAkNIMQww
ClientCLI>CgYDVQQHDANadWcxEDAOBgNVBAoMB09yZ05hbWUxCzAJBgNVBAsMAklUMSIwIAYJ
ClientCLI>...
ClientCLI>ifFs5TXw/tj6o6lEBvPBUhQ4sylHRDNr/UhcMz4i5y439yzBETcQQlgVkUWaJzXe
ClientCLI>yTEkJaFe73tvh1vUUePFhB8rPTMsfu59gQ==
ClientCLI>-----END CERTIFICATE-----
ClientCLI>
ClientCLI>fingerprint=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c

ServerAdmin> zoneadmin:create account=1000000117 zone=z2.tdmx.org pemText="
ServerAdmin>-----BEGIN CERTIFICATE-----
ServerAdmin>MIIGdTCCBF+gAwIBAgIBATALBgkqhkiG9w0BAQwwgYcxCzAJBgNVBAYTAkNIMQww
ServerAdmin>CgYDVQQHDANadWcxEDAOBgNVBAoMB09yZ05hbWUxCzAJBgNVBAsMAklUMSIwIAYJ
ServerAdmin>...
ServerAdmin>ifFs5TXw/tj6o6lEBvPBUhQ4sylHRDNr/UhcMz4i5y439yzBETcQQlgVkUWaJzXe
ServerAdmin>yTEkJaFe73tvh1vUUePFhB8rPTMsfu59gQ==
ServerAdmin>-----END CERTIFICATE-----" exec

check installed
ServerAdmin> zoneadmin:search account=1000000117 exec
           >AccountZoneAdministrationCredential; 80; 1000000117; z2.tdmx.org; 4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c; INSTALLED; null; 

Step 10: check connectivity to the service provider with the zone administrator

ClientCLI> scs:check zacPassword=changeme exec
ClientCLI>Step: TRUST_CHECK
ClientCLI>Remote IPAddress: segment1.scs.tdmx.org/192.168.178.21
ClientCLI>Certificate chain length: 3
ClientCLI>cert[0] subject=CN=segment1.scs.tdmx.org
ClientCLI>cert[0] fingerprint=721b156a52ce6160ce197d610c9b5dd50fef60bba4ce88845aa96089d73da911
ClientCLI>cert[1] subject=CN=StartCom Class 1 DV Server CA,OU=StartCom Certification Authority,O=StartCom Ltd.,C=IL
ClientCLI>cert[1] fingerprint=9bce787d0b6f137a31d46b2a6157a344aded7aced3782ea723c3ca951b74e66f
ClientCLI>cert[2] subject=CN=StartCom Certification Authority,OU=Secure Digital Certificate Signing,O=StartCom Ltd.,C=IL
ClientCLI>cert[2] fingerprint=e6f79754976245b19b33d66ba2fb76381e96c2fe296354e73d19ef9d177a5025
ClientCLI>Connection exception: sun.security.validator.ValidatorException: No trusted certificate found

Step 11: create a truststore to use for accessing SCS

ClientCLI> scs:download zacPassword=changeme fingerprint=9bce787d0b6f137a31d46b2a6157a344aded7aced3782ea723c3ca951b74e66f exec
ClientCLI>Step: TRUST_CHECK
ClientCLI>Remote IPAddress: segment1.scs.tdmx.org/192.168.178.21
ClientCLI>Certificate chain length: 3
ClientCLI>Trusted certificate stored in file scs.crt

ClientCLI> scs:check zacPassword=changeme scsTrustedCertFile=scs.crt exec
ClientCLI>Step: COMPLETE
ClientCLI>Remote IPAddress: segment1.scs.tdmx.org/192.168.178.21
ClientCLI>Certificate chain length: 3
ClientCLI>cert[0] subject=CN=segment1.scs.tdmx.org
ClientCLI>cert[0] fingerprint=721b156a52ce6160ce197d610c9b5dd50fef60bba4ce88845aa96089d73da911
ClientCLI>cert[1] subject=CN=StartCom Class 1 DV Server CA,OU=StartCom Certification Authority,O=StartCom Ltd.,C=IL
ClientCLI>cert[1] fingerprint=9bce787d0b6f137a31d46b2a6157a344aded7aced3782ea723c3ca951b74e66f
ClientCLI>cert[2] subject=CN=StartCom Certification Authority,OU=Secure Digital Certificate Signing,O=StartCom Ltd.,C=IL
ClientCLI>cert[2] fingerprint=e6f79754976245b19b33d66ba2fb76381e96c2fe296354e73d19ef9d177a5025
ClientCLI>Connection established successfully.


Now we are ready to work with the service provider and provision





Step 12: create a domain

