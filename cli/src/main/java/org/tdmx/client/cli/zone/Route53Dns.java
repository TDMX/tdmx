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
package org.tdmx.client.cli.zone;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.tdmx.client.cli.ClientCliUtils;
import org.tdmx.client.cli.ClientCliUtils.ZoneDescriptor;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.cli.annotation.Cli;
import org.tdmx.core.cli.annotation.Parameter;
import org.tdmx.core.cli.runtime.CommandExecutable;
import org.tdmx.core.system.dns.DnsUtils;
import org.tdmx.core.system.dns.DnsUtils.TdmxZoneRecord;
import org.tdmx.core.system.lang.StringUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.GetChangeRequest;
import com.amazonaws.services.route53.model.GetChangeResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesByNameRequest;
import com.amazonaws.services.route53.model.ListHostedZonesByNameResult;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;

@Cli(name = "dns:route53", description = "Describes the TXT record for the zone.", note = "Helps to copy-paste the DNS TXT record contents into a DNS server configuration tool.")
public class Route53Dns implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@Parameter(name = "zacPassword", required = true, description = "the zone administrator's keystore password.")
	private String zacPassword;

	@Parameter(name = "awsRegion", required = true, description = "the Amazon AWS region name.")
	private String awsRegion;

	@Parameter(name = "awsHostedZoneName", defaultValueText = "<the TDMX zone name>", description = "the Amazon AWS HostedZone name - equal or super domain of the zone name.")
	private String awsHostedZoneName;

	@Parameter(name = "ttl", defaultValue = "86400", description = "the DNS TXT record's TTL in seconds.")
	private int ttl;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void run(PrintStream out) {
		Regions r = null;
		try {
			r = Regions.fromName(awsRegion);
		} catch (IllegalArgumentException e) {
			StringBuilder warning = new StringBuilder();
			warning.append("Invalid region - use one of ");
			for (Regions reg : Regions.values()) {
				warning.append(reg.getName()).append(", ");
			}
			out.println(warning.toString());
			return;
		}

		ZoneDescriptor zd = ClientCliUtils.loadZoneDescriptor();
		if (StringUtils.hasText(awsHostedZoneName) && !awsHostedZoneName.equals(zd.getZoneApex())
				&& !DnsUtils.isSubdomain(zd.getZoneApex(), awsHostedZoneName)) {
			out.println("The zone must be equal to the awsHostedZoneName or a subdomain of awsHostedZoneName.");
			return;
		}
		if (zd.getScsUrl() == null) {
			out.println("Missing SCS URL. Use modify:zone to set the SessionControlServer's URL.");
			return;
		}

		PKIXCredential zac = ClientCliUtils.getZAC(zacPassword);
		String zacFingerprint = zac.getPublicCert().getFingerprint();

		TdmxZoneRecord zr = new TdmxZoneRecord(zd.getVersion(), zacFingerprint, zd.getScsUrl());
		String txtRecordValue = DnsUtils.formatDnsTxtRecord(zr);
		out.println(txtRecordValue);

		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}

		AmazonRoute53 s3 = new AmazonRoute53Client(credentials);
		s3.setRegion(Region.getRegion(r));

		if (!StringUtils.hasText(awsHostedZoneName)) {
			awsHostedZoneName = zd.getZoneApex();
		}

		ListHostedZonesByNameRequest req = new ListHostedZonesByNameRequest();
		req.setDNSName(awsHostedZoneName);
		req.setMaxItems("1");

		ListHostedZonesByNameResult res = s3.listHostedZonesByName(req);
		if (res.getHostedZones() == null || res.getHostedZones().isEmpty()) {
			out.println("The AWS HostedZone " + awsHostedZoneName + " was not found in AWS region " + r.getName());
			return;
		}
		HostedZone hz = res.getHostedZones().get(0);
		out.println(hz);
		String hzId = hz.getId().substring(hz.getId().lastIndexOf("/"));

		ChangeResourceRecordSetsRequest rsreq = createRRSet(hzId, awsHostedZoneName, zd.getZoneApex(), txtRecordValue,
				ttl);
		ChangeResourceRecordSetsResult rsres = s3.changeResourceRecordSets(rsreq);
		ChangeInfo ci = rsres.getChangeInfo();

		out.println("1st status :" + ci);

		GetChangeRequest gcr = new GetChangeRequest(ci.getId());
		GetChangeResult gcrres = s3.getChange(gcr);

		out.println("2nd status :" + gcrres.getChangeInfo());
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private ChangeResourceRecordSetsRequest createRRSet(String hzId, String hostedZoneName, String zoneApex,
			String txtRecordValue, int ttl) {
		// http://docs.aws.amazon.com/Route53/latest/DeveloperGuide/resource-record-sets-values-basic.html
		ResourceRecordSet rrset = new ResourceRecordSet();
		rrset.setType(RRType.TXT);
		if (!hostedZoneName.equals(zoneApex)) {
			rrset.setName(zoneApex + ".");
		}
		rrset.setTTL(Long.valueOf(ttl));
		ResourceRecord rr = new ResourceRecord("\"" + txtRecordValue + "\"");

		List<ResourceRecord> rrs = new ArrayList<>();
		rrs.add(rr);
		rrset.setResourceRecords(rrs);

		Change c = new Change();
		c.setAction(ChangeAction.UPSERT);
		c.setResourceRecordSet(rrset);

		List<Change> changes = new ArrayList<>();
		changes.add(c);

		ChangeBatch cb = new ChangeBatch();
		cb.setChanges(changes);

		ChangeResourceRecordSetsRequest rsreq = new ChangeResourceRecordSetsRequest();
		rsreq.setHostedZoneId(hzId);
		rsreq.setChangeBatch(cb);

		return rsreq;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
