package org.tdmx.client.serviceprovider.common;

import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Binds java.util.Calendar to a xsd:Time by invoking the JAXB provider's
 * implementation of parse and print methods.
 * 
 * @see global jaxb bindings in schema
 */
public class TimeAdapter extends XmlAdapter<String, Calendar>{

    public Calendar unmarshal(String value) {
        return (DatatypeConverter.parseTime(value));
    }

    public String marshal(Calendar value) {
        if (value == null) {
            return null;
        }
        return (DatatypeConverter.printTime(value));
    }

}
