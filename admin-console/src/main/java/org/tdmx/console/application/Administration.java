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
package org.tdmx.console.application;

import org.tdmx.console.application.dao.SystemTrustStore;
import org.tdmx.console.application.job.BackgroundJobRegistry;
import org.tdmx.console.application.search.SearchService;
import org.tdmx.console.application.service.CertificateAuthorityService;
import org.tdmx.console.application.service.CertificateService;
import org.tdmx.console.application.service.DnsResolverService;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ProblemRegistry;
import org.tdmx.console.application.service.SystemSettingsService;

public interface Administration {

	public ProblemRegistry getProblemRegistry();

	public ObjectRegistry getObjectRegistry();

	public SearchService getSearchService();

	public SystemTrustStore getTrustStore();

	public BackgroundJobRegistry getBackgroundJobRegistry();

	public SystemSettingsService getSystemSettingService();

	public DnsResolverService getDnsResolverService();

	public CertificateService getCertificateService();

	public CertificateAuthorityService getCertificateAuthorityService();

}
