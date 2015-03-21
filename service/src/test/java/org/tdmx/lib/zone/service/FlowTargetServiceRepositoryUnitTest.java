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
package org.tdmx.lib.zone.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tdmx.lib.common.domain.PageSpecifier;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.control.domain.TestDataGeneratorInput;
import org.tdmx.lib.control.domain.TestDataGeneratorOutput;
import org.tdmx.lib.control.job.TestDataGenerator;
import org.tdmx.lib.zone.domain.AgentCredential;
import org.tdmx.lib.zone.domain.FlowTarget;
import org.tdmx.lib.zone.domain.FlowTargetSearchCriteria;
import org.tdmx.lib.zone.domain.Service;
import org.tdmx.lib.zone.domain.Zone;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class FlowTargetServiceRepositoryUnitTest {

	@Autowired
	private TestDataGenerator dataGenerator;

	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private FlowTargetService flowTargetService;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private Zone zone;
	private AgentCredential user;
	private Service service;

	@Before
	public void doSetup() throws Exception {

		input = new TestDataGeneratorInput("zone.apex." + System.currentTimeMillis(),
				MockZonePartitionIdInstaller.ZP1_S1);
		input.setNumZACs(1);
		input.setNumDomains(1);
		input.setNumServicesPerDomain(1);
		input.setNumDACsPerDomain(1);
		input.setNumAddressesPerDomain(1);
		input.setNumUsersPerAddress(1);

		data = dataGenerator.setUp(input);

		zone = data.getZone();
		user = data.getDomains().get(0).getAddresses().get(0).getUcs().get(0).getAg();
		service = data.getDomains().get(0).getServices().get(0).getService();

		zonePartitionIdProvider.setPartitionId(input.getZonePartitionId());
	}

	@After
	public void doTeardown() {
		zonePartitionIdProvider.clearPartitionId();

		dataGenerator.tearDown(input, data);

	}

	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(dataGenerator);
		assertNotNull(flowTargetService);
	}

	@Test
	public void testFindById_NotFound() throws Exception {
		FlowTarget c = flowTargetService.findById(new Random().nextLong());
		assertNull(c);
	}

	@Test
	public void testSearch_None() throws Exception {
		FlowTargetSearchCriteria criteria = new FlowTargetSearchCriteria(new PageSpecifier(0, 999));
		List<FlowTarget> flowTargets = flowTargetService.search(zone, criteria);
		assertNotNull(flowTargets);
		assertEquals(1, flowTargets.size());
	}

	@Test
	public void testLookup_FindByChannel() throws Exception {
		FlowTarget storedCA = flowTargetService.findByTargetService(zone, user, service);
		assertNotNull(storedCA);
	}

	@Test
	public void testSearch_UnknownZone() throws Exception {
		FlowTargetSearchCriteria criteria = new FlowTargetSearchCriteria(new PageSpecifier(0, 999));
		Zone gugus = new Zone(zone.getAccountZoneId(), zone.getZoneApex());
		gugus.setId(new Random().nextLong());

		List<FlowTarget> channelAuths = flowTargetService.search(gugus, criteria);
		assertNotNull(channelAuths);
		assertEquals(0, channelAuths.size());
	}

	@Test
	public void testSearch_ServiceName() throws Exception {
		FlowTargetSearchCriteria criteria = new FlowTargetSearchCriteria(new PageSpecifier(0, 999));
		criteria.setServiceName(service.getServiceName());
		List<FlowTarget> channelAuths = flowTargetService.search(zone, criteria);
		assertNotNull(channelAuths);
		assertEquals(1, channelAuths.size());
	}

	@Test
	public void testSearch_TargetAddress() throws Exception {
		FlowTargetSearchCriteria criteria = new FlowTargetSearchCriteria(new PageSpecifier(0, 999));
		criteria.getTarget().getAddressName();
		List<FlowTarget> channelAuths = flowTargetService.search(zone, criteria);
		assertNotNull(channelAuths);
		assertEquals(1, channelAuths.size());
	}

	@Test
	public void testSearch_TargetUser() throws Exception {
		FlowTargetSearchCriteria criteria = new FlowTargetSearchCriteria(new PageSpecifier(0, 999));
		criteria.getTarget().setAddressName(user.getAddressName());
		criteria.getTarget().setDomainName(user.getDomainName());
		criteria.getTarget().setStatus(user.getCredentialStatus());
		List<FlowTarget> channelAuths = flowTargetService.search(zone, criteria);
		assertNotNull(channelAuths);
		assertEquals(1, channelAuths.size());
	}

	@Test
	public void testSearch_TargetAgent() throws Exception {
		FlowTargetSearchCriteria criteria = new FlowTargetSearchCriteria(new PageSpecifier(0, 999));
		criteria.getTarget().setAgent(user);
		List<FlowTarget> channelAuths = flowTargetService.search(zone, criteria);
		assertNotNull(channelAuths);
		assertEquals(1, channelAuths.size());
	}

	@Test
	public void testModify() throws Exception {
		FlowTarget storedCA = flowTargetService.findByTargetService(zone, user, service);
		assertNotNull(storedCA);
		storedCA.getConcurrency().setConcurrencyLevel(999);
		storedCA.getConcurrency().setConcurrencyLimit(1000);
		flowTargetService.createOrUpdate(storedCA);

		FlowTarget modifiedCA = flowTargetService.findByTargetService(zone, user, service);
		assertNotNull(modifiedCA);
		assertTrue(modifiedCA != storedCA);
		assertEquals(storedCA.getConcurrency().getConcurrencyLevel(), modifiedCA.getConcurrency().getConcurrencyLevel());
		assertEquals(storedCA.getConcurrency().getConcurrencyLimit(), modifiedCA.getConcurrency().getConcurrencyLimit());
	}

}