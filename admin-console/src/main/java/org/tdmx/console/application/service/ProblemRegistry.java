package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.Problem;

public interface ProblemRegistry {

	public void addProblem( Problem problem );
	public void deleteProblem( int problemId );
	public void deleteAllProblems();
	public List<Problem> getProblems();
	
}
