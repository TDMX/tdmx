package org.tdmx.client.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Binds java.util.Calendar to a xsd:date by invoking the JAXB provider's
 * implementation of parse and print methods.
 * 
 * @see global jaxb bindings in schema
 */
public class DateAdapter extends XmlAdapter<String, Calendar>{

    public Calendar unmarshal(String value) {
        return (DatatypeConverter.parseDate(value));
    }

    public String marshal(Calendar value) {
        if (value == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(value.getTimeZone());
        
        return formatter.format(value.getTime());
    }

}
