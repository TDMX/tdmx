package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.util.ValidationUtils;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.OperationError.ERROR;



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
	public static final DomainObjectField F_ACTIVE 		= new DomainObjectField("active", DomainObjectType.RootCAList);
	public static final DomainObjectField F_NAME 		= new DomainObjectField("name", DomainObjectType.RootCAList);

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
	public DomainObjectType getType() {
		return DomainObjectType.RootCAList;
	}

	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		RootCAListDO o = narrow(other);
		DomainObjectFieldChanges holder = new DomainObjectFieldChanges(this);
		setActive(conditionalSet(isActive(), o.isActive(), F_ACTIVE,holder));
		setName(conditionalSet(getName(), o.getName(), F_NAME, holder));
		return holder;
	}

	@Override
	public List<FieldError> validate() {
		List<FieldError> errors = new ArrayList<>();
		
		ValidationUtils.mandatoryField(isActive(), F_ACTIVE.getName(), ERROR.MISSING, errors);
		ValidationUtils.mandatoryTextField(getName(), F_NAME.getName(), ERROR.MISSING, errors);
		return errors;
	}

	@Override
	public void updateSearchFields(ObjectRegistry registry) {
		ObjectSearchContext ctx = new ObjectSearchContext();
		setSearchFields(ctx.getSearchFields());
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
