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
package org.tdmx.server.rs.sas;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.tdmx.server.rs.sas.resource.AccountResource;
import org.tdmx.server.rs.sas.resource.AccountZoneAdministrationCredentialResource;
import org.tdmx.server.rs.sas.resource.AccountZoneResource;
import org.tdmx.server.rs.sas.resource.DatabasePartitionResource;
import org.tdmx.server.rs.sas.resource.DnsResolverGroupResource;
import org.tdmx.server.rs.sas.resource.PartitionControlServerResource;
import org.tdmx.server.rs.sas.resource.SSLCertificateResource;
import org.tdmx.server.rs.sas.resource.SegmentResource;

@Path("/sas")
@Produces({ "application/json" })
public interface SAS {

	// TODO maxvalue (admin only)

	// TODO control job (admin only)

	// TODO lock (admin only)

	/*
	 * RESTFUL service for DnsResolverGroup
	 */
	@POST
	@Path("/dnsresolvergroups")
	DnsResolverGroupResource createDnsResolverGroup(DnsResolverGroupResource dnsResolverGroup);

	@GET
	@Path("/dnsresolvergroups")
	List<DnsResolverGroupResource> searchDnsResolverGroup(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize, @QueryParam("groupName") String groupName);

	@GET
	@Path("/dnsresolvergroups/{drgid}")
	DnsResolverGroupResource getDnsResolverGroup(@PathParam("drgid") Long drgId);

	@PUT
	@Path("/dnsresolvergroups/{drgid}")
	DnsResolverGroupResource updateDnsResolverGroup(@PathParam("drgid") Long drgId,
			DnsResolverGroupResource dnsResolverGroup);

	@DELETE
	@Path("/dnsresolvergroups/{drgid}")
	Response deleteDnsResolverGroup(@PathParam("drgid") Long drgId);

	/*
	 * RESTFUL service for Segment
	 */
	@POST
	@Path("/segments")
	SegmentResource createSegment(SegmentResource segment);

	@GET
	@Path("/segments")
	List<SegmentResource> searchSegment(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize, @QueryParam("segment") String segment);

	@GET
	@Path("/segments/{sid}")
	SegmentResource getSegment(@PathParam("sid") Long sId);

	@PUT
	@Path("/segments/{sid}")
	SegmentResource updateSegment(@PathParam("sid") Long sId, SegmentResource segment);

	@DELETE
	@Path("/segments/{sid}")
	Response deleteSegment(@PathParam("sid") Long sId);

	/*
	 * RESTFUL service for SSLCertificate
	 */
	@POST
	@Path("/sslcertificates")
	SSLCertificateResource createSSLCertificate(SSLCertificateResource sslcertificate);

	@GET
	@Path("/sslcertificates")
	List<SSLCertificateResource> searchSSLCertificate(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize, @QueryParam("fingerprint") String fingerprint,
			@QueryParam("contains") String contains);

	@GET
	@Path("/sslcertificates/{tcId}")
	SSLCertificateResource getSSLCertificate(@PathParam("tcId") Long tcId);

	@PUT
	@Path("/sslcertificates/{tcId}")
	SSLCertificateResource updateSSLCertificate(@PathParam("tcId") Long tcId, SSLCertificateResource sslcertificate);

	@DELETE
	@Path("/sslcertificates/{tcId}")
	Response deleteSSLCertificate(@PathParam("tcId") Long tcId);

	/*
	 * RESTFUL service for PartitionControlServers
	 */
	@POST
	@Path("/pcs")
	PartitionControlServerResource createPartitionControlServer(PartitionControlServerResource pcs);

	@GET
	@Path("/pcs")
	List<PartitionControlServerResource> searchPartitionControlServer(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize, @QueryParam("segment") String segment,
			@QueryParam("modulo") Integer modulo, @QueryParam("ipaddress") String ipaddress,
			@QueryParam("port") Integer port);

	@GET
	@Path("/pcs/{pcsid}")
	PartitionControlServerResource getPartitionControlServer(@PathParam("pcsid") Long pcsId);

	@PUT
	@Path("/pcs/{pcsid}")
	PartitionControlServerResource updatePartitionControlServer(@PathParam("pcsid") Long pcsId,
			PartitionControlServerResource pcs);

	@DELETE
	@Path("/pcs/{pcsid}")
	Response deletePartitionControlServer(@PathParam("pcsid") Long pcsId);

	/*
	 * RESTFUL service for DatabasePartition
	 */
	@POST
	@Path("/partitions")
	DatabasePartitionResource createDatabasePartition(DatabasePartitionResource partition);

	@GET
	@Path("/partitions")
	List<DatabasePartitionResource> searchDatabasePartition(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize, @QueryParam("partitionId") String partitionId,
			@QueryParam("dbType") String dbType, @QueryParam("segment") String segment);

	@GET
	@Path("/partitions/{pid}")
	DatabasePartitionResource getDatabasePartition(@PathParam("pid") Long pId);

	@PUT
	@Path("/partitions/{pid}")
	DatabasePartitionResource updateDatabasePartition(@PathParam("pid") Long pId, DatabasePartitionResource partition);

	@DELETE
	@Path("/partitions/{pid}")
	Response deleteDatabasePartition(@PathParam("pid") Long pId);

	/*
	 * RESTFUL service for Account
	 */
	@POST
	@Path("/accounts")
	AccountResource createAccount(AccountResource account);

	@GET
	@Path("/accounts")
	List<AccountResource> searchAccount(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize, @QueryParam("email") String email,
			@QueryParam("accountId") String accountId);

	@GET
	@Path("/accounts/{aid}")
	AccountResource getAccount(@PathParam("aid") Long aId);

	@PUT
	@Path("/accounts/{aid}")
	AccountResource updateAccount(@PathParam("aid") Long aId, AccountResource account);

	@DELETE
	@Path("/accounts/{aid}")
	Response deleteAccount(@PathParam("aid") Long aId);

	/*
	 * RESTFUL service for AccountZone
	 */
	@POST
	@Path("/accounts/{aid}/zones")
	AccountZoneResource createAccountZone(@PathParam("aid") Long aId, AccountZoneResource accountZone);

	@GET
	@Path("/accounts/{aid}/zones")
	List<AccountZoneResource> searchAccountZone(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize, @PathParam("aid") Long aId, @QueryParam("zone") String zoneApex);

	@GET
	@Path("/accounts/{aid}/zones/{zid}")
	AccountZoneResource getAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId);

	@PUT
	@Path("/accounts/{aid}/zones/{zid}")
	AccountZoneResource updateAccountZone(@PathParam("aid") Long aid, @PathParam("zid") Long zId,
			AccountZoneResource accountZone);

	@DELETE
	@Path("/accounts/{aid}/zones/{zid}")
	Response deleteAccountZone(@PathParam("aid") Long aId, @PathParam("zid") Long zId);

	@GET
	@Path("/accountzones")
	List<AccountZoneResource> searchAccountZone(@QueryParam("pageNumber") Integer pageNo,
			@QueryParam("pageSize") Integer pageSize, @QueryParam("accountId") String accountId,
			@QueryParam("zone") String zoneApex, @QueryParam("segment") String segment,
			@QueryParam("zonePartitionId") String zonePartitionId, @QueryParam("status") String status);

	/*
	 * RESTFUL service for AccountZoneAdministrationCredential
	 */
	@POST
	@Path("/accounts/{aid}/zones/{zid}/zacs")
	AccountZoneAdministrationCredentialResource createAccountZoneAdministrationCredential(@PathParam("aid") Long aId,
			@PathParam("zid") Long zId, AccountZoneAdministrationCredentialResource zac);

	@GET
	@Path("/accounts/{aid}/zones/{zid}/zacs")
	List<AccountZoneAdministrationCredentialResource> searchAccountZoneAdministrationCredential(
			@QueryParam("pageNumber") Integer pageNo, @QueryParam("pageSize") Integer pageSize,
			@PathParam("aid") Long aId, @PathParam("zid") Long zId);

	@GET
	@Path("/accounts/{aid}/zones/{zid}/zacs/{zcid}")
	AccountZoneAdministrationCredentialResource getAccountZoneAdministrationCredential(@PathParam("aid") Long aId,
			@PathParam("zid") Long zId, @PathParam("zcid") Long zcId);

	@PUT
	@Path("/accounts/{aid}/zones/{zid}/zacs/{zcid}")
	AccountZoneAdministrationCredentialResource updateAccountZoneAdministrationCredential(@PathParam("aid") Long aId,
			@PathParam("zid") Long zId, @PathParam("zcid") Long zcId, AccountZoneAdministrationCredentialResource zac);

	@DELETE
	@Path("/accounts/{aid}/zones/{zid}/zacs/{zcid}")
	Response deleteAccountZoneAdministrationCredential(@PathParam("aid") Long aId, @PathParam("zid") Long zId,
			@PathParam("zcid") Long zcId);

	@GET
	@Path("/accountzoneadministrationcredentials/")
	List<AccountZoneAdministrationCredentialResource> searchAccountZoneAdministrationCredential(
			@QueryParam("pageNumber") Integer pageNo, @QueryParam("pageSize") Integer pageSize,
			@QueryParam("zone") String zoneApex, @QueryParam("accountId") String accountId,
			@QueryParam("fingerprint") String fingerprint, @QueryParam("status") String status);

}
