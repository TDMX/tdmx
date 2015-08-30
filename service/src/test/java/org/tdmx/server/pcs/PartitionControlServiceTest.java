package org.tdmx.server.pcs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.pcs.PartitionedControlServiceImpl.ServerHolder;
import org.tdmx.server.pcs.ServerSessionController.ServerServiceStatistics;
import org.tdmx.server.pcs.ServerSessionController.ServiceStatistic;
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;

public class PartitionControlServiceTest {

	PartitionedControlServiceImpl sut;

	private String segment = "segment";
	private Map<SeedAttribute, Long> seedAttributes;

	@Before
	public void setUp() throws Exception {
		seedAttributes = new HashMap<>();

		sut = new PartitionedControlServiceImpl();
		sut.setSessionIdLength(10);
		sut.start(segment, null);
	}

	@After
	public void tearDown() throws Exception {
		sut.stop();

		assertNull(sut.getSegment());
		assertEquals(0, sut.getCertificateFingerprints().size());
		for (WebServiceApiName api : WebServiceApiName.values()) {
			assertEquals(0, sut.getApiSessions(api).size());
			assertEquals(0, sut.getServers(api).size());
		}
	}

	@Test
	public void testRegisterService() {
		PKIXCertificate serviceCert = mock(PKIXCertificate.class);
		ServerSessionController ssm = mock(ServerSessionController.class);
		ServiceHandle sh = new ServiceHandle(segment, WebServiceApiName.MOS, "url-1", serviceCert);

		sut.registerServer(Arrays.<ServiceHandle> asList(sh), ssm);

		assertEquals(1, sut.getServers(WebServiceApiName.MOS).size());
	}

	@Test
	public void testAssociateApiSession_OneServer() {
		PKIXCertificate serviceCert = mock(PKIXCertificate.class);
		ServerSessionController ssm = mock(ServerSessionController.class);
		ServiceHandle mossh = new ServiceHandle(segment, WebServiceApiName.MOS, "url-mos", serviceCert);
		ServiceHandle mdssh = new ServiceHandle(segment, WebServiceApiName.MDS, "url-mds", serviceCert);
		sut.registerServer(Arrays.<ServiceHandle> asList(mossh, mdssh), ssm);
		assertEquals(1, sut.getServers(WebServiceApiName.MOS).size());
		assertEquals(1, sut.getServers(WebServiceApiName.MDS).size());

		PKIXCertificate clientCert = mock(PKIXCertificate.class);
		when(clientCert.getFingerprint()).thenReturn("client-fingerprint-1");
		SessionHandle sesh = new SessionHandle(segment, WebServiceApiName.MOS, "sessionKey-1", seedAttributes);

		ServerServiceStatistics stats = new ServerServiceStatistics();
		ServiceStatistic mos = new ServiceStatistic(WebServiceApiName.MOS, "url-mos", 100);
		stats.addStatistic(mos);
		ServiceStatistic mds = new ServiceStatistic(WebServiceApiName.MDS, "url-mds", 50);
		stats.addStatistic(mds);
		when(ssm.createSession(Matchers.eq(WebServiceApiName.MOS), Matchers.isA(String.class),
				Matchers.same(clientCert), Matchers.same(seedAttributes))).thenReturn(stats);

		WebServiceSessionEndpoint wsse = sut.associateApiSession(sesh, clientCert);
		assertNotNull(wsse);
		assertEquals("url-mos", wsse.getHttpsUrl());
		assertTrue(serviceCert == wsse.getPublicCertificate());
		assertNotNull(wsse.getSessionId());
		assertEquals(sut.getSessionIdLength(), wsse.getSessionId().length());
		verify(ssm).createSession(Matchers.eq(WebServiceApiName.MOS), Matchers.isA(String.class),
				Matchers.same(clientCert), Matchers.same(seedAttributes));

		ServerHolder mosserver = sut.getServers(WebServiceApiName.MOS).get(0);
		assertEquals(100, mosserver.getLoadValue());
		assertEquals(100, sut.getTotalLoad(WebServiceApiName.MOS));
		ServerHolder mdsserver = sut.getServers(WebServiceApiName.MDS).get(0);
		assertEquals(50, mdsserver.getLoadValue());
		assertEquals(50, sut.getTotalLoad(WebServiceApiName.MDS));

		PKIXCertificate clientCert2 = mock(PKIXCertificate.class);
		when(clientCert2.getFingerprint()).thenReturn("client-fingerprint-2");

		stats = new ServerServiceStatistics();
		mos = new ServiceStatistic(WebServiceApiName.MOS, "url-mos", 101);
		stats.addStatistic(mos);
		mds = new ServiceStatistic(WebServiceApiName.MDS, "url-mds", 49);
		stats.addStatistic(mds);
		when(ssm.addCertificate(Matchers.eq(WebServiceApiName.MOS), Matchers.isA(String.class),
				Matchers.same(clientCert2))).thenReturn(stats);

		wsse = sut.associateApiSession(sesh, clientCert2);
		assertNotNull(wsse);
		assertEquals("url-mos", wsse.getHttpsUrl());
		assertTrue(serviceCert == wsse.getPublicCertificate());
		assertNotNull(wsse.getSessionId());
		assertEquals(sut.getSessionIdLength(), wsse.getSessionId().length());
		verify(ssm).addCertificate(Matchers.eq(WebServiceApiName.MOS), Matchers.isA(String.class),
				Matchers.same(clientCert2));

		assertEquals(101, mosserver.getLoadValue());
		assertEquals(101, sut.getTotalLoad(WebServiceApiName.MOS));
		assertEquals(49, mdsserver.getLoadValue());
		assertEquals(49, sut.getTotalLoad(WebServiceApiName.MDS));
	}

	@Test
	public void testUnregisterService() {
		PKIXCertificate serviceCert = mock(PKIXCertificate.class);
		ServerSessionController ssm = mock(ServerSessionController.class);
		ServiceHandle sh = new ServiceHandle(segment, WebServiceApiName.MOS, "url-1", serviceCert);
		sut.registerServer(Arrays.<ServiceHandle> asList(sh), ssm);

		sut.unregisterServer(Arrays.<ServiceHandle> asList(sh));
		assertEquals(0, sut.getServers(WebServiceApiName.MOS).size());
	}

	@Test
	public void testNotifySessionsRemoved() {
		PKIXCertificate serviceCert = mock(PKIXCertificate.class);
		ServerSessionController ssm = mock(ServerSessionController.class);
		ServiceHandle mossh = new ServiceHandle(segment, WebServiceApiName.MOS, "url-mos", serviceCert);
		ServiceHandle mdssh = new ServiceHandle(segment, WebServiceApiName.MDS, "url-mds", serviceCert);
		sut.registerServer(Arrays.<ServiceHandle> asList(mossh, mdssh), ssm);
		assertEquals(1, sut.getServers(WebServiceApiName.MOS).size());
		assertEquals(1, sut.getServers(WebServiceApiName.MDS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MOS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MDS).size());

		PKIXCertificate clientCert = mock(PKIXCertificate.class);
		when(clientCert.getFingerprint()).thenReturn("client-fingerprint-1");
		SessionHandle sesh = new SessionHandle(segment, WebServiceApiName.MOS, "sessionKey-1", seedAttributes);

		ServerServiceStatistics stats = new ServerServiceStatistics();
		ServiceStatistic mos = new ServiceStatistic(WebServiceApiName.MOS, "url-mos", 100);
		stats.addStatistic(mos);
		ServiceStatistic mds = new ServiceStatistic(WebServiceApiName.MDS, "url-mds", 50);
		stats.addStatistic(mds);
		when(ssm.createSession(Matchers.eq(WebServiceApiName.MOS), Matchers.isA(String.class),
				Matchers.same(clientCert), Matchers.same(seedAttributes))).thenReturn(stats);

		WebServiceSessionEndpoint wsse = sut.associateApiSession(sesh, clientCert);
		assertEquals(1, sut.getApiSessions(WebServiceApiName.MOS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MDS).size());

		sut.notifySessionsRemoved(WebServiceApiName.MOS, new HashSet<String>(Arrays.asList(wsse.getSessionId())));

		assertEquals(0, sut.getApiSessions(WebServiceApiName.MOS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MDS).size());
	}

	@Test
	public void testStart() {
		PartitionedControlServiceImpl s = new PartitionedControlServiceImpl();
		s.start("gugus", Arrays.<WebServiceApiName> asList(WebServiceApiName.MOS, WebServiceApiName.MDS,
				WebServiceApiName.MRS, WebServiceApiName.ZAS));
		assertEquals("gugus", s.getSegment());
	}

	@Test
	public void testStop() {
		PartitionedControlServiceImpl s = new PartitionedControlServiceImpl();
		s.start("gugus", null);
		assertEquals("gugus", s.getSegment());
		s.stop();
		assertNull(s.getSegment());
	}

}
