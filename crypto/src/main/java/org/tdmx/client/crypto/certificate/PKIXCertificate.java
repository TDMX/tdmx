package org.tdmx.client.crypto.certificate;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

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
	
	//TODO public key - type + leyken ie RSA(2048bit) AsymmetricEncryptionAlgorithm
	//TODO basic constraints - CA? certificate chain length
	//TODO issuer
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
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public X509Certificate getCertificate() {
		return certificate;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public String getIssuer() {
		if ( certificate.getIssuerX500Principal() != null ) {
			return certificate.getIssuerX500Principal().getName();
		}
		return null;
	}

	public String getSubject() {
		if ( certificate.getSubjectX500Principal() != null ) {
			return certificate.getSubjectX500Principal().getName();
		}
		return null;
	}

	public String getCommonName() {
		return getFirstRDN(holder.getSubject(), BCStyle.CN);
	}

	public String getOrganization() {
		return getFirstRDN(holder.getSubject(), BCStyle.O);
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
		return 0;
	}
	
	public boolean isTdmxDomainCA() {
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
		if ( ! ku.hasUsages(KeyUsage.keyCertSign|KeyUsage.digitalSignature) ) {
			return false;
		}
		
		// critical nameConstraint where subject(-DN)==namecontraint subtree
		X500Name snc = getSubjectNameConstraint();
		if ( snc != null ) {
			String ou_c = getFirstRDN(snc, BCStyle.OU);
			if ( !CertificateAuthorityUtils.TDMX_DOMAIN_CA_OU.equals(ou_c) ) {
				return false;
			}
			String o_c = getFirstRDN(snc, BCStyle.O);
			String o = getOrganization();
			if ( o_c == null || o == null || !o_c.equals(o) ) {
				return false;
			}
			String c_c = getFirstRDN(snc, BCStyle.C);
			String c = getCountry();
			if ( c_c == null || c == null || !c_c.equals(c) ) {
				return false;
			}
			return true;
		}
		
		return false;
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


}
