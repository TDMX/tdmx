package org.tdmx.console.application;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.console.domain.Domain;

public class ObjectRegistryImpl implements ObjectRegistry {

	@Override
	public List<Domain> getDomains() {
    	List<Domain> domainList = new ArrayList<Domain>();
    	domainList.add(new Domain("Domain A"));
    	domainList.add(new Domain("Domain B"));
    	domainList.add(new Domain("Domain C"));
    	domainList.add(new Domain("Domain D"));
    	

		return domainList; 
	}

}
