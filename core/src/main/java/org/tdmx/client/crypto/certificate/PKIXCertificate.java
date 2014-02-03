package org.tdmx.client.crypto.certificate;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralSubtree;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.NameConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.scheme.CryptoException;

public class PKIXCertificate {
	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private X509Certificate certificate;
	private JcaX509CertificateHolder holder;
	
	private String fingerprint;
	
	private static Map<String,String> oidMap = new HashMap<>();
	static {
		oidMap.put(BCStyle.E.getId(), "EMAIL");
		oidMap.put(BCStyle.TELEPHONE_NUMBER.getId(), "TEL");
	}
	
	//TODO public key - type + leyken ie RSA(2048bit) AsymmetricEncryptionAlgorithm
	//TODO subject key identifier
	//TODO issuer key identifier
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public PKIXCertificate( X509Certificate cert ) throws CryptoCertificateException {
		try {
			certificate = cert;
			
			holder = new JcaX509CertificateHolder(certificate);
			{
				byte[] tbsCert = cert.getTBSCertificate();
				byte[] sha1 = DigestAlgorithm.SHA_1.kdf(tbsCert);
				fingerprint = ByteArray.asHex(sha1);
			}
			
		} catch ( CryptoException e ) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_EXCEPTION, e);
		} catch (CertificateEncodingException e) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_ENCODING, e);
		}
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private String getFirstRDN( X500Name x500name, ASN1ObjectIdentifier attributeType ) {
		if ( x500name == null ) {
			return null;
		}
		RDN[] rdns = x500name.getRDNs(attributeType);
		if ( rdns != null && rdns.length > 0 ) {
			return IETFUtils.valueToString(rdns[0].getFirst().getValue());		
		}
		return null;
	}

	private String getSecondLastRDN( X500Name x500name, ASN1ObjectIdentifier attributeType ) {
		if ( x500name == null ) {
			return null;
		}
		RDN[] rdns = x500name.getRDNs(attributeType);
		if ( rdns != null && rdns.length > 1 ) {
			return IETFUtils.valueToString(rdns[rdns.length-2].getFirst().getValue());		
		}
		return null;
	}

	private String getLastRDN( X500Name x500name, ASN1ObjectIdentifier attributeType ) {
		if ( x500name == null ) {
			return null;
		}
		RDN[] rdns = x500name.getRDNs(attributeType);
		if ( rdns != null && rdns.length > 0 ) {
			return IETFUtils.valueToString(rdns[rdns.length-1].getFirst().getValue());		
		}
		return null;
	}

	private X500Name getSubjectNameConstraint() {
		Extension e = holder.getExtension(Extension.nameConstraints);
		if ( e != null && e.isCritical() ) {
			NameConstraints nc = NameConstraints.getInstance(e.getParsedValue());
			GeneralSubtree[] permitted = nc.getPermittedSubtrees();
			if ( permitted != null && permitted.length > 0 ) {
				GeneralName base = permitted[0].getBase();
				if ( base != null ) {
					if ( GeneralName.directoryName == base.getTagNo() ) {
						X500Name baseName = X500Name.getInstance(base.getName());
						return baseName;
					}
				}
			}
		}
		return null;
	}
	
	private KeyUsage getKeyUsage() {
		Extension e = holder.getExtension(Extension.keyUsage);
		if ( e != null ) {
			KeyUsage ku = KeyUsage.getInstance(e.getParsedValue());
			return ku;
		}
		return null;
	}
	
	public TdmxZoneInfo getTdmxZoneInfo() {
		Extension e = holder.getExtension(TdmxZoneInfo.tdmxZoneInfo);
		if ( e != null ) {
			TdmxZoneInfo ku = TdmxZoneInfo.getInstance(e.getParsedValue());
			return ku;
		}
		return null;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public X509Certificate getCertificate() {
		return certificate;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public X500Name getIssuerName() {
		return holder.getIssuer();
	}
	
	public String getIssuer() {
		if ( certificate.getIssuerX500Principal() != null ) {
			return certificate.getIssuerX500Principal().getName(X500Principal.RFC2253,oidMap);
		}
		return null;
	}

	public X500Name getSubjectName() {
		return holder.getSubject();
	}
	
	public String getSubject() {
		if ( certificate.getSubjectX500Principal() != null ) {
			return certificate.getSubjectX500Principal().getName(X500Principal.RFC2253,oidMap);
		}
		return null;
	}

	public String getCommonName() {
		return getFirstRDN(holder.getSubject(), BCStyle.CN);
	}

	public String getTelephoneNumber() {
		return getFirstRDN(holder.getSubject(), BCStyle.TELEPHONE_NUMBER);
	}

	public String getEmailAddress() {
		return getFirstRDN(holder.getSubject(), BCStyle.E);
	}

	public String getOrganization() {
		return getFirstRDN(holder.getSubject(), BCStyle.O);
	}

	public String getOrgUnit() {
		String ou = getFirstRDN(holder.getSubject(), BCStyle.OU); 
		return !CredentialUtils.TDMX_DOMAIN_CA_OU.equals(ou) ? ou : null;
	}

	public String getLocation() {
		return getFirstRDN(holder.getSubject(), BCStyle.L);
	}

	public String getCountry() {
		return getFirstRDN(holder.getSubject(), BCStyle.C);
	}

	public boolean isCA() {
		Extension e = holder.getExtension(Extension.basicConstraints);
		if ( e != null && e.isCritical() ) {
			BasicConstraints bc = BasicConstraints.getInstance(e.getParsedValue());
			return bc.isCA();
		}
		return false;
	}
	
	public int getCAPathLengthConstraint() {
		Extension e = holder.getExtension(Extension.basicConstraints);
		if ( e != null && e.isCritical() ) {
			BasicConstraints bc = BasicConstraints.getInstance(e.getParsedValue());
			if ( bc.getPathLenConstraint() != null ) {
				return bc.getPathLenConstraint().intValue();
			}
		}
		return -1;
	}
	
	//TODO is uc
	
	public boolean isTdmxZoneAdminCertificate() {
		// critical basicConstraints CA=true, max path length=1
		boolean caConstrained = isCA() && 1 == getCAPathLengthConstraint();
		if ( !caConstrained ) {
			return false;
		}
		
		// keyusage keyCertSign + digitalSignature
		KeyUsage ku = getKeyUsage();
		if ( ku == null ) {
			return false;
		}
		if ( !ku.hasUsages(KeyUsage.keyCertSign|KeyUsage.digitalSignature) ) {
			return false;
		}
		
		// is self signed, ie. subject == issuer
		String subjectName = getSubject();
		String issuerName = getIssuer();
		if ( subjectName == null || issuerName == null || !subjectName.equals(issuerName)  ) {
			return false;
		}
		//TODO subjectKey == issuerKey identifiers
		
		TdmxZoneInfo zi = getTdmxZoneInfo();
		if ( zi == null ) {
			return false;
		}
		
		// critical nameConstraint where subject(-DN)==namecontraint subtree
		X500Name snc = getSubjectNameConstraint();
		if ( snc != null ) {
			if ( getCountry() != null ) {
				String c = getFirstRDN(snc, BCStyle.C);
				if ( !getCountry().equals(c) ) {
					return false;
				}
			}
			if ( getLocation() != null ) {
				String l = getFirstRDN(snc, BCStyle.L);
				if ( !getLocation().equals(l) ) {
					return false;
				}
			}
			if ( getOrganization() != null ) {
				String o = getFirstRDN(snc, BCStyle.O);
				if ( !getOrganization().equals(o) ) {
					return false;
				}
			}
			if ( getOrgUnit() != null ) {
				String ou = getFirstRDN(snc, BCStyle.OU);
				if ( !getOrgUnit().equals(ou) ) {
					return false;
				}
			}
			String tdmx_ou = getLastRDN(snc, BCStyle.OU);
			if ( !CredentialUtils.TDMX_DOMAIN_CA_OU.equals(tdmx_ou) ) {
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	public boolean isTdmxDomainAdminCertificate() {
		// critical basicConstraints CA=true, max path length=1
		boolean caConstrained = isCA() && 0 == getCAPathLengthConstraint();
		if ( !caConstrained ) {
			return false;
		}
		
		// keyusage keyCertSign + digitalSignature
		KeyUsage ku = getKeyUsage();
		if ( ku == null ) {
			return false;
		}
		if ( !ku.hasUsages(KeyUsage.keyCertSign|KeyUsage.digitalSignature) ) {
			return false;
		}
		
		// domain cert is NOT self signed, ie. subject != issuer
		String subjectName = getSubject();
		String issuerName = getIssuer();
		if ( subjectName == null || issuerName == null || subjectName.equals(issuerName)  ) {
			return false;
		}
		//TODO subjectKey identifiers present
		//TODO issuerKey identifiers present
		
		TdmxZoneInfo zi = getTdmxZoneInfo();
		if ( zi == null ) {
			return false;
		}
		
		if ( !getCommonName().equals(zi.getZoneRoot()) ) {
			//domain is subdomain of zone root
			if ( !getCommonName().endsWith("."+zi.getZoneRoot()) ) {
				return false; 
			}
		}
		// critical nameConstraint where subject(-DN)==namecontraint subtree
		X500Name snc = getSubjectNameConstraint();
		if ( snc != null ) {
			if ( getCountry() != null ) {
				String c = getFirstRDN(snc, BCStyle.C);
				if ( !getCountry().equals(c) ) {
					return false;
				}
			}
			if ( getLocation() != null ) {
				String l = getFirstRDN(snc, BCStyle.L);
				if ( !getLocation().equals(l) ) {
					return false;
				}
			}
			if ( getOrganization() != null ) {
				String o = getFirstRDN(snc, BCStyle.O);
				if ( !getOrganization().equals(o) ) {
					return false;
				}
			}
			if ( getOrgUnit() != null ) {
				String ou = getFirstRDN(snc, BCStyle.OU);
				if ( !getOrgUnit().equals(ou) ) {
					return false;
				}
			}
			String tdmx_ou = getSecondLastRDN(snc, BCStyle.OU);
			if ( !CredentialUtils.TDMX_DOMAIN_CA_OU.equals(tdmx_ou) ) {
				return false;
			}
			
			String domain_ou = getLastRDN(snc, BCStyle.OU);
			if ( !getCommonName().equals(domain_ou) ) {
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean isTdmxUserCertificate() {
		// critical basicConstraints CA=true, max path length=1
		if ( isCA() ) {
			return false;
		}
		
		// keyusage keyCertSign + digitalSignature
		KeyUsage ku = getKeyUsage();
		if ( ku == null ) {
			return false;
		}
		if ( !ku.hasUsages(KeyUsage.keyEncipherment|KeyUsage.digitalSignature|KeyUsage.nonRepudiation) ) {
			return false;
		}
		
		// domain cert is NOT self signed, ie. subject != issuer
		String subjectName = getSubject();
		String issuerName = getIssuer();
		if ( subjectName == null || issuerName == null || subjectName.equals(issuerName)  ) {
			return false;
		}
		//TODO subjectKey identifiers present
		//TODO issuerKey identifiers present
		
		TdmxZoneInfo zi = getTdmxZoneInfo();
		if ( zi == null ) {
			return false;
		}

		//Last OU is the domainName
		String domainName = getLastRDN(getSubjectName(), BCStyle.OU);
		if ( domainName == null ) {
			return false;
		}
		if ( !domainName.equals(zi.getZoneRoot()) ) {
			//domain is subdomain of zone root
			if ( !domainName.endsWith("."+zi.getZoneRoot()) ) {
				return false; 
			}
		}
		
		return true;
	}
	
	public Calendar getNotBefore() {
		if ( holder.getNotBefore() != null ) {
			Calendar notBefore = Calendar.getInstance();
			notBefore.setTime(holder.getNotBefore());
			return notBefore;
		}
		return null;
	}

	public Calendar getNotAfter() {
		if ( holder.getNotAfter() != null ) {
			Calendar notAfter = Calendar.getInstance();
			notAfter.setTime(holder.getNotAfter());
			return notAfter;
		}
		return null;
	}

	public String getSignatureAlgorithm() {
		if ( holder.getSignatureAlgorithm() != null ) {
			return IETFUtils.valueToString(holder.getSignatureAlgorithm());
		}
		return null;
	}

	public String getSignature() {
		if ( holder.getSignature() != null ) {
			return ByteArray.asHex(holder.getSignature());
		}
		return null;
	}

	public String getInfo() {
		return certificate.toString();
	}

	public boolean isIdentical( PKIXCertificate other ) {
		try {
			return other != null && ByteArray.equals(certificate.getEncoded(), other.getCertificate().getEncoded());
		} catch (CertificateEncodingException e) {
			return false;
		}
	}

}
