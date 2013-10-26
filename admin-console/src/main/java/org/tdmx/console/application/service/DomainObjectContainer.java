package org.tdmx.console.application.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tdmx.console.application.domain.DomainObject;

public class DomainObjectContainer<D extends DomainObject> {

	private List<D> instances = new CopyOnWriteArrayList<D>();
	
	public List<D> getList() {
		return instances;
	}
	
	public boolean add(D o) {
		if ( !instances.contains(o)) {
			return instances.add(o);
		}
		return false;
	}
	
	public boolean remove(D o) {
		return instances.remove(o);
	}
}
