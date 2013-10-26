package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.ProblemDO;

public interface ProblemRegistry {

	public void addProblem( ProblemDO problem );
	public void deleteProblem( String problemId );
	public void deleteAllProblems();
	public ProblemDO getLastProblem();
	public List<ProblemDO> getProblems();
	
}
