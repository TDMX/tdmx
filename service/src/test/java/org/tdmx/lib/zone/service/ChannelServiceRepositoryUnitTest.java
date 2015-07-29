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

import java.math.BigInteger;
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
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.lib.zone.domain.ChannelAuthorization;
import org.tdmx.lib.zone.domain.ChannelAuthorizationSearchCriteria;
import org.tdmx.lib.zone.domain.Zone;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ChannelServiceRepositoryUnitTest {

	@Autowired
	private TestDataGenerator dataGenerator;

	@Autowired
	private ThreadLocalPartitionIdProvider zonePartitionIdProvider;
	@Autowired
	private ChannelService channelService;

	private TestDataGeneratorInput input;
	private TestDataGeneratorOutput data;

	private Zone zone;

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
		assertNotNull(channelService);
	}

	@Test
	public void testFindById_NotFound() throws Exception {
		Channel c = channelService.findById(new Random().nextLong());
		assertNull(c);
	}

	@Test
	public void testSearch_None() throws Exception {
		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		List<Channel> channels = channelService.search(zone, criteria);
		assertNotNull(channels);
		assertEquals(1, channels.size());
	}

	@Test
	public void testSearch_OriginAll() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);

		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		criteria.getOrigin().setLocalName(ca.getChannel().getOrigin().getLocalName());
		criteria.getOrigin().setDomainName(ca.getChannel().getOrigin().getDomainName());

		List<Channel> channels = channelService.search(zone, criteria);
		assertNotNull(channels);
		assertEquals(1, channels.size());
	}

	@Test
	public void testSearch_DestinationAll() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);

		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		criteria.getDestination().setLocalName(ca.getChannel().getDestination().getLocalName());
		criteria.getDestination().setDomainName(ca.getChannel().getDestination().getDomainName());
		criteria.getDestination().setServiceName(ca.getChannel().getDestination().getServiceName());

		List<Channel> channels = channelService.search(zone, criteria);
		assertNotNull(channels);
		assertEquals(1, channels.size());
	}

	@Test
	public void testSearch_OriginAndDestinationAll() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);

		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		criteria.getOrigin().setLocalName(ca.getChannel().getOrigin().getLocalName());
		criteria.getOrigin().setDomainName(ca.getChannel().getOrigin().getDomainName());
		criteria.getDestination().setLocalName(ca.getChannel().getDestination().getLocalName());
		criteria.getDestination().setDomainName(ca.getChannel().getDestination().getDomainName());
		criteria.getDestination().setServiceName(ca.getChannel().getDestination().getServiceName());

		List<Channel> channels = channelService.search(zone, criteria);
		assertNotNull(channels);
		assertEquals(1, channels.size());
	}

	@Test
	public void testLookup_FindByChannel() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);

		ChannelAuthorization storedCA = channelService.findByChannel(zone, ca.getChannel().getDomain(), ca.getChannel()
				.getOrigin(), ca.getChannel().getDestination());
		assertNotNull(storedCA);
	}

	@Test
	public void testSearch_UnknownZone() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);

		ChannelAuthorizationSearchCriteria criteria = new ChannelAuthorizationSearchCriteria(new PageSpecifier(0, 999));
		criteria.getOrigin().setLocalName(ca.getChannel().getOrigin().getLocalName());

		Zone gugus = new Zone(zone.getAccountZoneId(), zone.getZoneApex());
		gugus.setId(new Random().nextLong());

		List<Channel> channels = channelService.search(gugus, criteria);
		assertNotNull(channels);
		assertEquals(0, channels.size());
	}

	@Test
	public void testModify() throws Exception {
		ChannelAuthorization ca = data.getDomains().get(0).getAuths().get(0);
		ChannelAuthorization storedCA = channelService.findByChannel(zone, ca.getChannel().getDomain(), ca.getChannel()
				.getOrigin(), ca.getChannel().getDestination());
		assertNotNull(storedCA);
		storedCA.getUndeliveredBuffer().setHighMarkBytes(BigInteger.TEN);
		storedCA.getUndeliveredBuffer().setLowMarkBytes(BigInteger.ONE);
		storedCA.getUnsentBuffer().setHighMarkBytes(BigInteger.TEN);
		storedCA.getUnsentBuffer().setLowMarkBytes(BigInteger.ONE);
		channelService.createOrUpdate(storedCA.getChannel());

		ChannelAuthorization modifiedCA = channelService.findByChannel(zone, ca.getChannel().getDomain(), ca
				.getChannel().getOrigin(), ca.getChannel().getDestination());
		assertNotNull(modifiedCA);
		assertEquals(storedCA.getUndeliveredBuffer().getHighMarkBytes(), modifiedCA.getUndeliveredBuffer()
				.getHighMarkBytes());
		assertEquals(storedCA.getUndeliveredBuffer().getLowMarkBytes(), modifiedCA.getUndeliveredBuffer()
				.getLowMarkBytes());
		assertEquals(storedCA.getUnsentBuffer().getHighMarkBytes(), modifiedCA.getUnsentBuffer().getHighMarkBytes());
		assertEquals(storedCA.getUnsentBuffer().getLowMarkBytes(), modifiedCA.getUnsentBuffer().getLowMarkBytes());

	}

}