package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.console.application.domain.validation.FieldError;
import org.tdmx.console.application.domain.validation.FieldError.ERROR;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.util.ValidationUtils;



/**
 * A DnsResolverList is an ordered list of IP addresses which are used to resolve DNS
 * lookups.
 * 
 * @author Peter
 *
 */
public class DnsResolverListDO extends AbstractDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final DomainObjectField F_ACTIVE 		= new DomainObjectField("active", DnsResolverListDO.class.getName());
	public static final DomainObjectField F_NAME 		= new DomainObjectField("name", DnsResolverListDO.class.getName());
	public static final DomainObjectField F_HOSTNAMES 	= new DomainObjectField("hostnames", DnsResolverListDO.class.getName());

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Boolean active = Boolean.TRUE;
	private String name;
	private List<String> hostnames;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public DnsResolverListDO() {
		super();
	}
	
	public DnsResolverListDO( DnsResolverListDO original ) {
		setId(original.getId());
		setActive(original.isActive());
		setName(original.getName());
		setHostnames(original.getHostnames());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends DomainObject> E copy() {
		return (E) new DnsResolverListDO(this);
	}

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public DomainObjectType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		DnsResolverListDO o = narrow(other);
		DomainObjectFieldChanges holder = new DomainObjectFieldChanges(this);
		setActive(conditionalSet(isActive(), o.isActive(), F_ACTIVE,holder));
		setName(conditionalSet(getName(), o.getName(), F_NAME, holder));
		setHostnames(conditionalSet(getHostnames(), o.getHostnames(), F_HOSTNAMES, holder));
		return holder;
	}

	@Override
	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();
		
		ValidationUtils.mandatoryField(isActive(), F_ACTIVE, ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getName(), F_NAME, ERROR.MISSING, errors);
		if ( getHostnames() != null ) {
			for( String hostname : getHostnames()) {
				ValidationUtils.optionalHostnameField(hostname, F_HOSTNAMES, ERROR.INVALID, errors);
			}
		}
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

	private <E extends DomainObject> DnsResolverListDO narrow( E other ) {
		return (DnsResolverListDO)other;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getHostnames() {
		return hostnames;
	}

	public void setHostnames(List<String> hostnames) {
		this.hostnames = hostnames;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
