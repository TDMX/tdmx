package org.tdmx.console.service;

import java.util.List;

import org.tdmx.console.domain.Domain;
import org.tdmx.console.domain.Problem;
import org.tdmx.console.domain.User;
import org.tdmx.console.service.command.AddServiceProviderCommand;

public interface UIService {

	public abstract List<Domain> listDomains();

	// Global application calls 
	//
	public int getBusyId();
	
	// Problem related calls 
	//
	public boolean hasProblems();
	public int getNumberOfProblems();
	public List<Problem> getProblems();
	public void deleteAllProblems();
	public void deleteProblem( int id );
	public Problem getMostRecentProblem();
	
	// User related calls 
	//
	public User authenticate(String login, String password);
	
	// Commands
	public void execute( AddServiceProviderCommand cmd );
}