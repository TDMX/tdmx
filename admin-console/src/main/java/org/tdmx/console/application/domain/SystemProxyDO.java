package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.console.application.domain.validation.FieldError;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.service.ObjectRegistry;



/**
 * An object representing the System's HTTPS or SOCKS proxy.
 * 
 * @author Peter
 *
 */
public class SystemProxyDO extends AbstractDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final DomainObjectField F_HTTPSPROXY 			= new DomainObjectField("httpsProxy", DomainObjectType.SystemProxy);
	public static final DomainObjectField F_HTTPSNONPROXYHOSTS 	= new DomainObjectField("httpsNonProxyHosts", DomainObjectType.SystemProxy);
	public static final DomainObjectField F_SOCKSPROXY	 		= new DomainObjectField("socksProxy", DomainObjectType.SystemProxy);

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String httpsProxy;
	private String httpsNonProxyHosts;
	private String socksProxy;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public SystemProxyDO() {
		super();
	}
	
	public SystemProxyDO( SystemProxyDO original ) {
		setId(original.getId());
		setHttpsProxy(original.getHttpsProxy());
		setHttpsNonProxyHosts(original.getHttpsNonProxyHosts());
		setSocksProxy(original.getSocksProxy());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends DomainObject> E copy() {
		return (E) new SystemProxyDO(this);
	}

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public DomainObjectType getType() {
		return DomainObjectType.SystemProxy;
	}

	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		SystemProxyDO o = narrow(other);
		DomainObjectFieldChanges holder = new DomainObjectFieldChanges(this);
		setHttpsNonProxyHosts(conditionalSet(getHttpsNonProxyHosts(), o.getHttpsNonProxyHosts(), F_HTTPSPROXY, holder));
		setHttpsNonProxyHosts(conditionalSet(getHttpsNonProxyHosts(), o.getHttpsNonProxyHosts(), F_HTTPSNONPROXYHOSTS, holder));
		setSocksProxy(conditionalSet(getSocksProxy(), o.getSocksProxy(), F_SOCKSPROXY, holder));
		return holder;
	}

	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();
		return errors;
	}

	@Override
	public void gatherSearchFields(ObjectSearchContext ctx, ObjectRegistry registry) {
		//TODO
	}
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private <E extends DomainObject> SystemProxyDO narrow( E other ) {
		return (SystemProxyDO)other;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getHttpsProxy() {
		return httpsProxy;
	}

	public void setHttpsProxy(String httpsProxy) {
		this.httpsProxy = httpsProxy;
	}

	public String getHttpsNonProxyHosts() {
		return httpsNonProxyHosts;
	}

	public void setHttpsNonProxyHosts(String httpsNonProxyHosts) {
		this.httpsNonProxyHosts = httpsNonProxyHosts;
	}

	public String getSocksProxy() {
		return socksProxy;
	}

	public void setSocksProxy(String socksProxy) {
		this.socksProxy = socksProxy;
	}

}
