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

import java.math.BigDecimal;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Binds java.math.BigDecimal to a xsd:float by invoking the JAXB provider's implementation of parse and print methods.
 * <p>
 * The sample schema supplied by the vendor incorrectly specify float where decimal should have been used, such as for
 * amounts and monetary values.
 * </p>
 * 
 * @see global jaxb bindings in schema
 */
public class FloatAdapter extends XmlAdapter<String, BigDecimal> {

	@Override
	public BigDecimal unmarshal(String value) {
		return DatatypeConverter.parseDecimal(value);
	}

	@Override
	public String marshal(BigDecimal value) {
		if (value == null) {
			return null;
		}
		return DatatypeConverter.printDecimal(value);
	}

}
