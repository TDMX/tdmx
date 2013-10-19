package org.tdmx.console.service;

import java.util.List;

import org.tdmx.console.domain.Domain;
import org.tdmx.console.domain.Problem;

public interface UIService {

	public abstract List<Domain> listDomains();

	public boolean hasProblems();
	public int getNumberOfProblems();
	public List<Problem> getProblems();
	public void deleteAllProblems();
	public void deleteProblem( int id );
	public Problem getMostRecentProblem();
	
}