package org.tdmx.server.pcs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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
import org.tdmx.lib.control.domain.Segment;
import org.tdmx.lib.control.domain.SegmentFacade;
import org.tdmx.server.pcs.SessionControlServiceImpl.ServerHolder;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.session.WebServiceSessionEndpoint;
import org.tdmx.server.ws.session.WebServiceApiName;

public class RemoteControlServiceTest {

	SessionControlServiceImpl sut;

	private Segment segment = SegmentFacade.createSegment("segment", "https://scsHostname/scs");
	private Map<AttributeId, Long> seedAttributes;

	private String tosAddress = "localhost:1111";

	@Before
	public void setUp() throws Exception {
		seedAttributes = new HashMap<>();

		sut = new SessionControlServiceImpl();
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
		ServiceHandle sh = new ServiceHandle(segment.getSegmentName(), WebServiceApiName.MOS, "url-1", serviceCert);

		sut.registerServer(Arrays.<ServiceHandle> asList(sh), ssm, tosAddress);

		assertEquals(1, sut.getServers(WebServiceApiName.MOS).size());
	}

	@Test
	public void testAssociateApiSession_OneServer() {
		PKIXCertificate serviceCert = mock(PKIXCertificate.class);
		ServerSessionController ssm = mock(ServerSessionController.class);
		ServiceHandle mossh = new ServiceHandle(segment.getSegmentName(), WebServiceApiName.MOS, "url-mos",
				serviceCert);
		ServiceHandle mdssh = new ServiceHandle(segment.getSegmentName(), WebServiceApiName.MDS, "url-mds",
				serviceCert);
		sut.registerServer(Arrays.<ServiceHandle> asList(mossh, mdssh), ssm, tosAddress);
		assertEquals(1, sut.getServers(WebServiceApiName.MOS).size());
		assertEquals(1, sut.getServers(WebServiceApiName.MDS).size());

		PKIXCertificate clientCert = mock(PKIXCertificate.class);
		when(clientCert.getFingerprint()).thenReturn("client-fingerprint-1");
		SessionHandle sesh = new SessionHandle(segment.getSegmentName(), WebServiceApiName.MOS, "sessionKey-1",
				seedAttributes);

		ServerServiceStatistics stats = new ServerServiceStatistics();
		ServiceStatistic mos = new ServiceStatistic(WebServiceApiName.MOS, "url-mos", 100);
		stats.addStatistic(mos);
		ServiceStatistic mds = new ServiceStatistic(WebServiceApiName.MDS, "url-mds", 50);
		stats.addStatistic(mds);
		when(ssm.createSession(Matchers.eq(WebServiceApiName.MOS), Matchers.isA(String.class),
				Matchers.same(clientCert), Matchers.same(seedAttributes))).thenReturn(mos);

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
		assertEquals(0, mdsserver.getLoadValue());
		assertEquals(0, sut.getTotalLoad(WebServiceApiName.MDS));

		PKIXCertificate clientCert2 = mock(PKIXCertificate.class);
		when(clientCert2.getFingerprint()).thenReturn("client-fingerprint-2");

		stats = new ServerServiceStatistics();
		mos = new ServiceStatistic(WebServiceApiName.MOS, "url-mos", 101);
		stats.addStatistic(mos);
		mds = new ServiceStatistic(WebServiceApiName.MDS, "url-mds", 49);
		stats.addStatistic(mds);
		when(ssm.addCertificate(Matchers.eq(WebServiceApiName.MOS), Matchers.isA(String.class),
				Matchers.same(clientCert2))).thenReturn(mos);

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
		assertEquals(0, mdsserver.getLoadValue());
		assertEquals(0, sut.getTotalLoad(WebServiceApiName.MDS));
	}

	@Test
	public void testUnregisterService() {
		PKIXCertificate serviceCert = mock(PKIXCertificate.class);
		ServerSessionController ssm = mock(ServerSessionController.class);
		ServiceHandle sh = new ServiceHandle(segment.getSegmentName(), WebServiceApiName.MOS, "url-1", serviceCert);
		sut.registerServer(Arrays.<ServiceHandle> asList(sh), ssm, tosAddress);

		sut.unregisterServer(Arrays.<ServiceHandle> asList(sh));
		assertEquals(0, sut.getServers(WebServiceApiName.MOS).size());
	}

	@Test
	public void testNotifySessionsRemoved() {
		PKIXCertificate serviceCert = mock(PKIXCertificate.class);
		ServerSessionController ssm = mock(ServerSessionController.class);
		ServiceHandle mossh = new ServiceHandle(segment.getSegmentName(), WebServiceApiName.MOS, "url-mos",
				serviceCert);
		ServiceHandle mdssh = new ServiceHandle(segment.getSegmentName(), WebServiceApiName.MDS, "url-mds",
				serviceCert);
		sut.registerServer(Arrays.<ServiceHandle> asList(mossh, mdssh), ssm, tosAddress);
		assertEquals(1, sut.getServers(WebServiceApiName.MOS).size());
		assertEquals(1, sut.getServers(WebServiceApiName.MDS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MOS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MDS).size());

		PKIXCertificate clientCert = mock(PKIXCertificate.class);
		when(clientCert.getFingerprint()).thenReturn("client-fingerprint-1");
		SessionHandle sesh = new SessionHandle(segment.getSegmentName(), WebServiceApiName.MOS, "sessionKey-1",
				seedAttributes);

		ServerServiceStatistics stats = new ServerServiceStatistics();
		ServiceStatistic mos = new ServiceStatistic(WebServiceApiName.MOS, "url-mos", 100);
		stats.addStatistic(mos);
		ServiceStatistic mds = new ServiceStatistic(WebServiceApiName.MDS, "url-mds", 50);
		stats.addStatistic(mds);
		when(ssm.createSession(Matchers.eq(WebServiceApiName.MOS), Matchers.isA(String.class),
				Matchers.same(clientCert), Matchers.same(seedAttributes))).thenReturn(mos);

		WebServiceSessionEndpoint wsse = sut.associateApiSession(sesh, clientCert);
		assertEquals(1, sut.getApiSessions(WebServiceApiName.MOS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MDS).size());

		sut.notifySessionsRemoved(WebServiceApiName.MOS, new HashSet<String>(Arrays.asList(wsse.getSessionId())));

		assertEquals(0, sut.getApiSessions(WebServiceApiName.MOS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MDS).size());
	}

	@Test
	public void testNotifyInvalidateCertificate() {
		PKIXCertificate serviceCert = mock(PKIXCertificate.class);
		ServerSessionController ssm = mock(ServerSessionController.class);
		ServiceHandle mossh = new ServiceHandle(segment.getSegmentName(), WebServiceApiName.MOS, "url-mos",
				serviceCert);
		ServiceHandle mdssh = new ServiceHandle(segment.getSegmentName(), WebServiceApiName.MDS, "url-mds",
				serviceCert);
		sut.registerServer(Arrays.<ServiceHandle> asList(mossh, mdssh), ssm, tosAddress);
		assertEquals(1, sut.getServers(WebServiceApiName.MOS).size());
		assertEquals(1, sut.getServers(WebServiceApiName.MDS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MOS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MDS).size());

		PKIXCertificate clientCert = mock(PKIXCertificate.class);
		when(clientCert.getFingerprint()).thenReturn("client-fingerprint-1");
		SessionHandle sesh = new SessionHandle(segment.getSegmentName(), WebServiceApiName.MOS, "sessionKey-1",
				seedAttributes);

		ServerServiceStatistics stats = new ServerServiceStatistics();
		ServiceStatistic mos = new ServiceStatistic(WebServiceApiName.MOS, "url-mos", 100);
		stats.addStatistic(mos);
		ServiceStatistic mds = new ServiceStatistic(WebServiceApiName.MDS, "url-mds", 50);
		stats.addStatistic(mds);
		when(ssm.createSession(Matchers.eq(WebServiceApiName.MOS), Matchers.isA(String.class),
				Matchers.same(clientCert), Matchers.same(seedAttributes))).thenReturn(mos);

		WebServiceSessionEndpoint wsse = sut.associateApiSession(sesh, clientCert);
		assertNotNull(wsse);
		assertEquals(1, sut.getApiSessions(WebServiceApiName.MOS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MDS).size());

		ServerServiceStatistics stats2 = new ServerServiceStatistics();
		ServiceStatistic mos2 = new ServiceStatistic(WebServiceApiName.MOS, "url-mos", 110);
		stats2.addStatistic(mos2);
		ServiceStatistic mds2 = new ServiceStatistic(WebServiceApiName.MDS, "url-mds", 40);
		stats2.addStatistic(mds2);

		when(ssm.removeCertificate(Matchers.same(clientCert))).thenReturn(stats2);
		sut.invalidateCertificate(clientCert);

		// the session remains, but it will not have any certs
		assertEquals(1, sut.getApiSessions(WebServiceApiName.MOS).size());
		assertEquals(0, sut.getApiSessions(WebServiceApiName.MDS).size());
		assertTrue(sut.getApiSessions(WebServiceApiName.MOS).get(0).getCertificates().isEmpty());
		assertFalse(sut.getCertificateFingerprints().contains(clientCert.getFingerprint()));

		ServerHolder mosserver = sut.getServers(WebServiceApiName.MOS).get(0);
		assertEquals(110, mosserver.getLoadValue());
		assertEquals(110, sut.getTotalLoad(WebServiceApiName.MOS));
		ServerHolder mdsserver = sut.getServers(WebServiceApiName.MDS).get(0);
		assertEquals(40, mdsserver.getLoadValue());
		assertEquals(40, sut.getTotalLoad(WebServiceApiName.MDS));

		// TODO invalidate session later.
	}

	@Test
	public void testStart() {
		Segment gugus = SegmentFacade.createSegment("gugus", "dada");
		SessionControlServiceImpl s = new SessionControlServiceImpl();
		s.start(gugus, Arrays.<WebServiceApiName> asList(WebServiceApiName.MOS, WebServiceApiName.MDS,
				WebServiceApiName.MRS, WebServiceApiName.ZAS));
		assertSame(gugus, s.getSegment());
	}

	@Test
	public void testStop() {
		Segment gugus = SegmentFacade.createSegment("gugus", "dada");
		SessionControlServiceImpl s = new SessionControlServiceImpl();
		s.start(gugus, null);
		assertSame(gugus, s.getSegment());
		s.stop();
		assertNull(s.getSegment());
	}

}
