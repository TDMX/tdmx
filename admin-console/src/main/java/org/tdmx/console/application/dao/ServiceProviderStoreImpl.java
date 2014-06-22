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
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.core.system.lang.JaxbMarshaller;

public class ServiceProviderStoreImpl implements ServiceProviderStore {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private String filename;
	private final AtomicInteger suffixId = new AtomicInteger();

	private final JaxbMarshaller<ServiceProviderStorage> marshaller = new JaxbMarshaller<>(
			ServiceProviderStorage.class, new QName("urn:dao.application.console.tdmx.org", "service-provider-storage"));

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ServiceProviderStoreImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public synchronized ServiceProviderStorage load() throws IOException, JAXBException {
		byte[] fileContents = FileUtils.getFileContents(getFilename());
		if (fileContents != null) {
			ServiceProviderStorage content = marshaller.unmarshal(fileContents);
			return content;
		}
		return new ServiceProviderStorage();
	}

	@Override
	public synchronized void save(ServiceProviderStorage content) throws IOException, JAXBException {
		byte[] bytes = marshaller.marshal(content);
		FileUtils.storeFileContents(getFilename(), bytes, "." + suffixId.getAndIncrement());
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
