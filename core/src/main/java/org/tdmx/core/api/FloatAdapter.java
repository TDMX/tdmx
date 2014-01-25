package org.tdmx.core.api;

import java.math.BigDecimal;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Binds java.math.BigDecimal to a xsd:float by invoking the JAXB provider's
 * implementation of parse and print methods.
 * <p>
 * The sample schema supplied by the vendor incorrectly specify float where
 * decimal should have been used, such as for amounts and monetary values.
 * </p>
 * 
 * @see global jaxb bindings in schema
 */
public class FloatAdapter extends XmlAdapter<String, BigDecimal> {

	public BigDecimal unmarshal(String value) {
		return DatatypeConverter.parseDecimal(value);
	}
	
	public String marshal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return (DatatypeConverter.printDecimal(value));
	}

}
