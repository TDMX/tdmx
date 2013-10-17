package org.tdmx.console.application.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;


public class JaxbMarshaller<T> {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
    private static final String DEFAULT_ENCODING = "ISO-8859-1";
	private String encoding=DEFAULT_ENCODING;
	private String noNamespaceSchemaLocation;
	private JAXBContext jaxbContext;
	private QName qName;
	private Class<T> responseClass;
	private boolean suppressXMLDeclaration = false;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	/**
	 * Constructor sufficient for Unmarshalling.
	 * @param responseClass
	 */
	public JaxbMarshaller( Class<T> responseClass ) {
		this.responseClass = responseClass;
		initializeJAXB(responseClass.getPackage().getName());
	}
	
	/**
	 * Constructor sufficient for Marshaling and Unmarshaling the requestClass.
	 * 
	 * @param requestClass
	 * @param qname
	 */
	public JaxbMarshaller( Class<T> requestClass, QName qname ) {
		this.responseClass = requestClass;
		this.qName = qname;
		initializeJAXB(responseClass.getPackage().getName());
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	public byte[] marshal(T instance) throws JAXBException {
		Object jaxbElement = instance;
		if (qName != null) {
			JAXBElement<T> element = createElement(instance,qName);
			jaxbElement = element;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
		if(getNoNamespaceSchemaLocation()!=null){
			marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, getNoNamespaceSchemaLocation());
		}
		if(isSuppressXMLDeclaration()) {
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		}
		marshaller.marshal(jaxbElement, new PrintStream(out));
		return out.toByteArray();
	}

	public T unmarshal(byte[] xml)	throws JAXBException {
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return unmarshall(unmarshaller, xml, responseClass);
	}

    public String getXmlBytesAsString( byte[] bytes ) {
        String msg = "<Unknown/>";
        try {
            msg = new String( bytes, getEncoding());
        } catch ( UnsupportedEncodingException e ) {
            msg = new String(bytes);
        }
        return msg;
    }
    
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private JAXBElement<T> createElement(T instance,QName qName) {
		JAXBElement<T> element =
			new JAXBElement<T>(qName, (Class<T>) instance.getClass(),
					instance);
		return element;
	}

	private T unmarshall(Unmarshaller unmarshaller,byte[] xml,
			Class<T> clazz) throws JAXBException {
		JAXBElement<T> root =
			unmarshaller.unmarshal(new StreamSource(
					new ByteArrayInputStream(xml)), clazz);
		return root.getValue();
	}

 	private void initializeJAXB( String contextPath ) {
		try {
			jaxbContext = JAXBContext.newInstance(contextPath);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to initialize Context for "+contextPath, ex);
		}	
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

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
