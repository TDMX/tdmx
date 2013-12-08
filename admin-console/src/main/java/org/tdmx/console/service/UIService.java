package org.tdmx.console.service;

import java.util.List;

import org.tdmx.console.domain.Domain;
import org.tdmx.console.domain.Job;
import org.tdmx.console.domain.Problem;
import org.tdmx.console.domain.User;

public interface UIService {

	public List<Domain> listDomains();

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
	
}