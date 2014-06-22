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
package org.tdmx.console.application.dao;

import java.io.IOException;

import javax.xml.bind.JAXBException;

public class ServiceProviderStoreMockImpl implements ServiceProviderStore {

	private int scale = 10;
	private ServiceProviderStorage state = null;

	public ServiceProviderStoreMockImpl(int scale) {
		this.scale = scale;

		ServiceProviderStorage s = new ServiceProviderStorage();
		for (int i = 0; i < scale; i++) {
			s.getServiceprovider().add(ServiceProviderStoreFacade.getServiceProvider(i));
		}
		state = s;
	}

	@Override
	public ServiceProviderStorage load() throws IOException, JAXBException {
		return this.state;
	}

	@Override
	public void save(ServiceProviderStorage content) throws IOException, JAXBException {
		this.state = content;
	}

	public int getScale() {
		return scale;
	}

}
