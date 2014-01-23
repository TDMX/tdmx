package org.tdmx.client.api;

import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Binds java.util.Calendar to a xsd:dateTime by invoking the JAXB provider's
 * implementation of parse and print methods.
 * 
 * @see global jaxb bindings in schema
 */
public class DateTimeAdapter extends XmlAdapter<String, Calendar>{

    public Calendar unmarshal(String value) {
        return (DatatypeConverter.parseDateTime(value));
    }

    public String marshal(Calendar value) {
        if (value == null) {
            return null;
        }
        return (DatatypeConverter.printDateTime(value));
    }

}
