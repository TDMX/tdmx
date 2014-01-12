package org.tdmx.client.crypto.certificate;

import java.math.BigInteger;
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * a TDMX zone info object.
 */
public class TdmxZoneInfo
    extends ASN1Object
{
	public static final ASN1ObjectIdentifier tdmxZoneInfo = new ASN1ObjectIdentifier("1.2.3.4.5.6.7.8.9");
	
    private ASN1Integer             version;
    private DERIA5String            zoneRoot;
    private DERIA5String            mrsHostname;

    public static TdmxZoneInfo getInstance(
        Object  o)
    {
        if (o instanceof TdmxZoneInfo)
        {
            return (TdmxZoneInfo)o;
        }
        else if (o instanceof ASN1Sequence)
        {
            return new TdmxZoneInfo((ASN1Sequence)o);
        }

        throw new IllegalArgumentException("unknown object in factory: " + o.getClass().getName());
    }

    public TdmxZoneInfo(
        int version,
        String            zoneRoot,
        String            mrsHostname)
    {
        this.version = new ASN1Integer(version);
        this.zoneRoot = new DERIA5String(zoneRoot);
        this.mrsHostname = new DERIA5String(mrsHostname);
    }

    public TdmxZoneInfo(
        ASN1Sequence seq)
    {
        Enumeration     e = seq.getObjects();

        version = (ASN1Integer)e.nextElement();
        zoneRoot = DERIA5String.getInstance(e.nextElement());
        mrsHostname = DERIA5String.getInstance(e.nextElement());
    }

    public int getVersion()
    {
    	BigInteger bi = version.getValue();
        return bi.intValue();
    }

    public String getZoneRoot()
    {
        return zoneRoot.getString();
    }

    public String getMrsHostname()
    {
        return mrsHostname.getString();
    }


    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     *  TdmxZoneInfo ::= SEQUENCE {
     *      version Version,
     *      zoneRoot IA5String,
     *      mrsHostname IA5String
     *  }
     * </pre>
     */
    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(version);
        v.add(zoneRoot);
        v.add(mrsHostname);

        return new DERSequence(v);
    }
}
