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

ClientCLI> domain:create domain=z2.tdmx.org zacPassword=changeme exec
         >Domain info: tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
         >ZAS sessionId: ababe79662219d836e2d32081e3dc62c
         >Domain z2.tdmx.org successfully created.

confirm with 
ClientCLI> domain:search zacPassword=changeme exec
         >ZAS sessionId: ababe79662219d836e2d32081e3dc62c
         >Found 1 domains.
         >z2.tdmx.org

Step 13: create a domain administrator

ClientCLI> domainadmin:create domain=z2.tdmx.org dacPassword=changeme zacPassword=changeme exec
         >certificate=-----BEGIN CERTIFICATE-----
         >MIIFazCCA1WgAwIBAgIBATALBgkqhkiG9w0BAQwwgYcxCzAJBgNVBAYTAkNIMQww
         >..
         >IR8MVtTbBst2nmqC7niFuW7gLuLWSL7RZRX75ydhZRkZo1VZEKcu/DOC1W0xAnQ=
         >-----END CERTIFICATE-----
         >
         >serialNumber=1
 
Step 14: activate the domain administrator at the service provider

ClientCLI> domainadmin:activate domain=z2.tdmx.org zacPassword=changeme exec
         >Domain info: tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
         >ZAS sessionId: ababe79662219d836e2d32081e3dc62c
         >Administrator for domain z2.tdmx.org with fingerprint 50214b243c62676ffd2944f6ad8ea0bdfa00e2e72b38fe0b5ec3a75a201a90f1 activated.

confirm with 

ClientCLI> domainadmin:search zacPassword=changeme exec
         >ZAS sessionId: ababe79662219d836e2d32081e3dc62c
         >Found 1 domain administrators.
         >Administrator[ z2.tdmx.org SerialNumber=1 Fingerprint=50214b243c62676ffd2944f6ad8ea0bdfa00e2e72b38fe0b5ec3a75a201a90f1 Status=ACTIVE Administrator Public Key=-----BEGIN CERTIFICATE-----
         >MIIFazCCA1WgAwIBAgIBATALBgkqhkiG9w0BAQwwgYcxCzAJBgNVBAYTAkNIMQww
         >IR8MVtTbBst2nmqC7niFuW7gLuLWSL7RZRX75ydhZRkZo1VZEKcu/DOC1W0xAnQ=
         >-----END CERTIFICATE-----


Step 15: create an address

ClientCLI> address:create localname=user2 domain=z2.tdmx.org dacPassword=changeme exec
         >Domain info: tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
         >ZAS sessionId: e313744daf43e486e6e766ff230a39be
         >Address [user2@z2.tdmx.org] successfully created.
         
Step 16: request a channel authorization

ClientCLI> channel:authorize from=user2@z2.tdmx.org to=user1@z1.tdmx.org#service1 domain=z2.tdmx.org dacPassword=changeme exec
         >Domain info: tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
         >ZAS sessionId: e313744daf43e486e6e766ff230a39be
         >Authorization Channel [user2@z2.tdmx.org->usr1@z1.tdmx.org#service1] successful.

confirm with 

ClientCLI> channel:search domain=z2.tdmx.org dacPassword=changeme exec
         >
         >Domain info: tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
         >ZAS sessionId: e313744daf43e486e6e766ff230a39be
         >Found 1 channels.
         >Channel Info [Domain [z2.tdmx.org]
         >Channel [Channel [user2@z2.tdmx.org->usr1@z1.tdmx.org#service1]]
         >Current Authorization [Origin Permission [ALLOW Size=536870912 Signature [Administrator Signature [ Administrator Public Key=-----BEGIN CERTIFICATE-----
         >MIIFazCCA1WgAwIBAgIBATALBgkqhkiG9w0BAQwwgYcxCzAJBgNVBAYTAkNIMQww
         >...
         >IR8MVtTbBst2nmqC7niFuW7gLuLWSL7RZRX75ydhZRkZo1VZEKcu/DOC1W0xAnQ=
         >-----END CERTIFICATE-----
         >
         >SignatureValue [ Timestamp=12. Februar 2016 21:32:07 MEZ Algorithm=SHA_384_WITH_RSA Signature=9737e139e15af005cb503d071f3e1031da4e0994dffdc15fdfa567c5d404615a5950a9de2205f0e115965d39fb6b26140b4a837dd531f0aa424eb5a09d25756f8953e8eab0a85ffc46675496c38c6a25689c11b21743aacb90d861962e20ce77b18962924c59695ee17d186c517c9f7cb5406c40eb63f4a6456db215a6dd0631964244dade7f0b5663f8099f81a36097511e77c51c6e99505025373fdbca54c39f41021377f32d657307147574f62e0b4bfa7ffa89b7a0a27df08d8a55d259663c82ba7f4ef5491e2234315a6873320a81c590546fd2a19d131cd4211e76914578c4cc9b9be8372a32e6d17e20a89448f70c8227244c0e9667791a20992e0d5a]]]]
         >No Destination Permission
         >]
         >Requested Authorization [No Origin Permission
         >No Destination Permission
         >]
         >Processing State [Error [306] Unable to locate TDMX zone apex information for domain z1.tdmx.org in DNS. Status=FAILURE Timestamp=12. Februar 2016 21:32:07 MEZ]
         >No Session
         >FlowStatus [ RelayStatus=OPEN FlowStatus=OPEN UsedBytes=0]
         >]


Step 17: create a user

ClientCLI> user:create username=user2@z2.tdmx.org userPassword=changeme dacPassword=changeme exec
         >
         >certificate=-----BEGIN CERTIFICATE-----
         >MIIDxTCCAq+gAwIBAgIBATALBgkqhkiG9w0BAQwwZjELMAkGA1UEBhMCQ0gxDDAK
         >..
         >773CWUSFHemllIjIDXqRmpjIMNUx9xs5uXKia7pWwcTJN6nphxnevg01vuHWNdpL
         >Lj2yw2yHmrPC
         >-----END CERTIFICATE-----
         >
         >serialNumber=1

         
Step 18: activate a user

ClientCLI> user:activate username=user2@z2.tdmx.org dacPassword=changeme exec
         >Domain info: tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
         >ZAS sessionId: de923b313f9830403fff92e813ead629
         >User user2@z2.tdmx.org with fingerprint 5850202af86e54b03ca75205fe4769068adb72fe3ffbf28668b6e4a5aaf429e9 activated.

confirm with
ClientCLI> user:search dacPassword=changeme domain=z2.tdmx.org exec
         >Found 1 users.
         >User[ user2 SerialNumber=1 Fingerprint=5850202af86e54b03ca75205fe4769068adb72fe3ffbf28668b6e4a5aaf429e9 Status=ACTIVE User Public Key=-----BEGIN CERTIFICATE-----
         >MIIDxTCCAq+gAwIBAgIBATALBgkqhkiG9w0BAQwwZjELMAkGA1UEBhMCQ0gxDDAK
         >773CWUSFHemllIjIDXqRmpjIMNUx9xs5uXKia7pWwcTJN6nphxnevg01vuHWNdpL
         >Lj2yw2yHmrPC
         >-----END CERTIFICATE-----

Step 19: create a service to receive from

ClientCLI>service:create service=service1 domain=z2.tdmx.org dacPassword=changeme exec
         >Domain info: tdmx version=1 zac=4f36bc2fd2b58e40e7556edbf6534b2eaa236b7c6b1131515fd9cd36118dd42c scs=https://segment1.scs.tdmx.org:8444/scs/v1.0/scs
         >ZAS sessionId: de923b313f9830403fff92e813ead629
         >Service [z2.tdmx.org#service1] successfully created.

confirm with

ClientCLI> service:search dacPassword=changeme domain=z2.tdmx.org exec


Step 20: setup a receiver configuration

ClientCLI> destination:configure destination=user2@z2.tdmx.org#service1 userPassword=changeme exec
         >destination descriptor file user2@z2.tdmx.org#service1.dst was created.
         >dataDir=.
         >encryptionScheme=ecdh384:rsa/aes256
         >salt=b92970d9785c00f8f38cfdef3776df97
         >sessionDurationInHours=24
         >sessionRetentionInDays=24

