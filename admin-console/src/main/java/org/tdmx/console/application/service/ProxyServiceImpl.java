package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.HttpProxyDO;


public class ProxyServiceImpl implements ProxyService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private ObjectRegistry objectRegistry;

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean isDeleteable(HttpProxyDO proxy) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public List<ERROR> create(HttpProxyDO proxy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ERROR> modify(HttpProxyDO proxy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(HttpProxyDO proxy) {
		// TODO Auto-generated method stub
		
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	public void setObjectRegistry(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}

}
