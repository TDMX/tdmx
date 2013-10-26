package org.tdmx.console.service;

import java.util.List;

import org.tdmx.console.application.service.ProxyService;
import org.tdmx.console.domain.Domain;
import org.tdmx.console.domain.Job;
import org.tdmx.console.domain.Problem;
import org.tdmx.console.domain.Proxy;
import org.tdmx.console.domain.User;

public interface UIService {

	public abstract List<Domain> listDomains();

	// Global application calls 
	//
	public List<Job> getJobs();
	
	// Problem related calls 
	//
	public boolean hasProblems();
	public int getNumberOfProblems();
	public List<Problem> getProblems();
	public void deleteAllProblems();
	public void deleteProblem( String id );
	public Problem getMostRecentProblem();
	
	// User related calls 
	//
	public User authenticate(String login, String password);
	
	// Proxy related calls
	//
	public List<String> getProxyTypes();
	public List<Proxy> getProxies();
	public Proxy lookupProxy( String id );
	public List<ProxyService.ERROR> save( Proxy proxy ); // create and update
	public void deleteProxy( String id );
	
}