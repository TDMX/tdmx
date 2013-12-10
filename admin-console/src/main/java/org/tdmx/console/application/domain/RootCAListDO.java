package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.console.application.domain.validation.FieldError;
import org.tdmx.console.application.domain.validation.FieldError.ERROR;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.util.ValidationUtils;



/**
 * A RootCAList is consists of two unordered lists of TrustStoreEntries.
 * 
 * One list is the trusted RootCA certificates, the other of the distrusted RootCA certificates.
 * 
 * @author Peter
 *
 */
public class RootCAListDO extends AbstractDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final DomainObjectField F_ACTIVE 		= new DomainObjectField("active", RootCAListDO.class.getName());
	public static final DomainObjectField F_NAME 		= new DomainObjectField("name", RootCAListDO.class.getName());

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Boolean active = Boolean.TRUE;
	private String name;
	//TODO other fields
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public RootCAListDO() {
		super();
	}
	
	public RootCAListDO( RootCAListDO original ) {
		setId(original.getId());
		setActive(original.isActive());
		setName(original.getName());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends DomainObject> E copy() {
		return (E) new RootCAListDO(this);
	}

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		RootCAListDO o = narrow(other);
		DomainObjectFieldChanges holder = new DomainObjectFieldChanges(this);
		setActive(conditionalSet(isActive(), o.isActive(), F_ACTIVE,holder));
		setName(conditionalSet(getName(), o.getName(), F_NAME, holder));
		return holder;
	}

	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();
		
		ValidationUtils.mandatoryField(isActive(), F_ACTIVE, ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getName(), F_NAME, ERROR.MISSING, errors);
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

	private <E extends DomainObject> RootCAListDO narrow( E other ) {
		return (RootCAListDO)other;
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

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
