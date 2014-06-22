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
package org.tdmx.core.system.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

public class JaxbMarshaller<T> {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final String DEFAULT_ENCODING = "UTF-8";
	private String encoding = DEFAULT_ENCODING;
	private String noNamespaceSchemaLocation;
	private JAXBContext jaxbContext;
	private QName qName;
	private final Class<T> responseClass;
	private boolean suppressXMLDeclaration = false;
	private boolean prettyPrint = true;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	/**
	 * Constructor sufficient for Unmarshalling.
	 * 
	 * @param responseClass
	 */
	public JaxbMarshaller(Class<T> responseClass) {
		this.responseClass = responseClass;
		initializeJAXB(responseClass.getPackage().getName());
	}

	/**
	 * Constructor sufficient for Marshaling and Unmarshaling the requestClass.
	 * 
	 * @param requestClass
	 * @param qname
	 */
	public JaxbMarshaller(Class<T> requestClass, QName qname) {
		this.responseClass = requestClass;
		this.qName = qname;
		initializeJAXB(responseClass.getPackage().getName());
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	public byte[] marshal(T instance) throws JAXBException {
		Object jaxbElement = instance;
		if (qName != null) {
			JAXBElement<T> element = createElement(instance, qName);
			jaxbElement = element;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
		if (getNoNamespaceSchemaLocation() != null) {
			marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, getNoNamespaceSchemaLocation());
		}
		if (isSuppressXMLDeclaration()) {
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		}
		if (isPrettyPrint()) {
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		}
		marshaller.marshal(jaxbElement, new PrintStream(out));
		return out.toByteArray();
	}

	public T unmarshal(byte[] xml) throws JAXBException {
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return unmarshall(unmarshaller, xml, responseClass);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private JAXBElement<T> createElement(T instance, QName qName) {
		JAXBElement<T> element = new JAXBElement<T>(qName, (Class<T>) instance.getClass(), instance);
		return element;
	}

	private T unmarshall(Unmarshaller unmarshaller, byte[] xml, Class<T> clazz) throws JAXBException {
		JAXBElement<T> root = unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(xml)), clazz);
		return root.getValue();
	}

	private void initializeJAXB(String contextPath) {
		try {
			jaxbContext = JAXBContext.newInstance(contextPath);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to initialize Context for " + contextPath, ex);
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getNoNamespaceSchemaLocation() {
		return noNamespaceSchemaLocation;
	}

	public void setNoNamespaceSchemaLocation(String noNamespaceSchemaLocation) {
		this.noNamespaceSchemaLocation = noNamespaceSchemaLocation;
	}

	public QName getqName() {
		return qName;
	}

	public void setqName(QName qName) {
		this.qName = qName;
	}

	public boolean isSuppressXMLDeclaration() {
		return suppressXMLDeclaration;
	}

	public void setSuppressXMLDeclaration(boolean suppressXMLDeclaration) {
		this.suppressXMLDeclaration = suppressXMLDeclaration;
	}

}
