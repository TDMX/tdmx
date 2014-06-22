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
package org.tdmx.core.api;

import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Binds java.util.Calendar to a xsd:Time by invoking the JAXB provider's implementation of parse and print methods.
 * 
 * @see global jaxb bindings in schema
 */
public class TimeAdapter extends XmlAdapter<String, Calendar> {

	@Override
	public Calendar unmarshal(String value) {
		return DatatypeConverter.parseTime(value);
	}

	@Override
	public String marshal(Calendar value) {
		if (value == null) {
			return null;
		}
		return DatatypeConverter.printTime(value);
	}

}
