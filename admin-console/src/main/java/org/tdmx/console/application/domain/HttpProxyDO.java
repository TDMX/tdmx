package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tdmx.console.application.domain.DomainObjectField.FieldType;
import org.tdmx.console.application.service.ProxyService;
import org.tdmx.console.application.util.ValidationUtils;
//import org.tdmx.console.application.search.FieldDescriptor.FieldType;


/**
 * An outgoing HTTP proxy.
 * 
 * @author Peter
 *
 */
public class HttpProxyDO extends AbstractDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	public static List<String> proxyTypes;
	
	public static final DomainObjectField F_HOSTNAME 	= new DomainObjectField("hostname", FieldType.String, HttpProxyDO.class.getName());
	public static final DomainObjectField F_PORT 		= new DomainObjectField("port", FieldType.Number, HttpProxyDO.class.getName());
	public static final DomainObjectField F_TYPE 		= new DomainObjectField("type", FieldType.Enum, HttpProxyDO.class.getName());
	public static final DomainObjectField F_USERNAME 	= new DomainObjectField("username", FieldType.String, HttpProxyDO.class.getName());
	public static final DomainObjectField F_PASSWORD	= new DomainObjectField("password", FieldType.Protected, HttpProxyDO.class.getName());

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String hostname;
	private Integer port;
	private String type; //HTTP or SOCKS
	private String username; // if need Basic ProxyAuthorization
	private String password;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	static {
		List<String> pt = new ArrayList<>();
		pt.add("HTTP");
		pt.add("SOCKS");
		proxyTypes = Collections.unmodifiableList(pt);
	}
	
	public HttpProxyDO() {
		super();
	}
	
	private HttpProxyDO( HttpProxyDO original ) {
		setId(original.getId());
		setHostname(original.getHostname());
		setPort(original.getPort());
		setType(original.getType());
		setUsername(original.getUsername());
		setPassword(original.getPassword());
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public <F extends DomainObject> DomainObjectFieldChanges merge(F other) {
		HttpProxyDO o = narrow(other);
		DomainObjectFieldChanges holder = new DomainObjectFieldChanges(this);
		setHostname(conditionalSet(getHostname(), o.getHostname(), F_HOSTNAME, holder));
		setPort(conditionalSet(getPort(), o.getPort(), F_PORT, holder));
		setType(conditionalSet(getType(), o.getType(), F_TYPE, holder));
		setUsername(conditionalSet(getUsername(), o.getUsername(), F_USERNAME, holder));
		setPassword(conditionalSet(getPassword(), o.getPassword(), F_PASSWORD, holder));
		return holder;
	}

	
	public List<ProxyService.ERROR> validate() {
		List<ProxyService.ERROR> errors = new ArrayList<>();
		
		ValidationUtils.mandatoryTextField(getHostname(), ProxyService.ERROR.HOSTNAME_MISSING, errors);
		ValidationUtils.optionalHostnameField(getHostname(), ProxyService.ERROR.HOSTNAME_INVALID, errors);
		ValidationUtils.mandatoryNumberField(getPort(), ProxyService.ERROR.PORT_MISSING, errors);
		ValidationUtils.optionalPortField(getPort(), ProxyService.ERROR.PORT_INVALID, errors);
		
		ValidationUtils.mandatoryTextField(getType(), ProxyService.ERROR.TYPE_MISSING, errors);
		ValidationUtils.optionalEnumeratedTextField(getType(), proxyTypes, ProxyService.ERROR.TYPE_INVALID, errors);
		
		ValidationUtils.optionalTextFieldGroup(new String[] { getUsername(),  getPassword()}, ProxyService.ERROR.USERNAME_OR_PASSWORD_MISSING, errors);

		return errors;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends DomainObject> E copy() {
		return (E) new HttpProxyDO(this);
	}

	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private <E extends DomainObject> HttpProxyDO narrow( E other ) {
		return (HttpProxyDO)other;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		if ( username != null ) {
			sb.append(username).append("@");
		}
		sb.append(hostname).append(":").append(port);
		sb.append("/").append(type);
		return sb.toString();
	}


}
