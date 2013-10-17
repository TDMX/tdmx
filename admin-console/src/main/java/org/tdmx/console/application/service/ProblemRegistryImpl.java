package org.tdmx.console.application.service;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.tdmx.console.application.domain.Problem;

public class ProblemRegistryImpl implements ProblemRegistry {

	private LinkedList<Problem> problemList = new LinkedList<>();
	
	private int maxSize = 1000;
	
	@Override
	public void addProblem(Problem problem) {
		problemList.addLast(problem);
		if ( problemList.size() > maxSize ) {
			problemList.removeFirst();
		}
	}

	@Override
	public void deleteProblem(int problemId) {
		Iterator<Problem> it = problemList.iterator();
		while( it.hasNext() ) {
			Problem p = it.next();
			if ( p.getId() == problemId ) {
				it.remove();
			}
		}
	}

	@Override
	public void deleteAllProblems() {
		problemList.clear();
	}

	@Override
	public List<Problem> getProblems() {
		return Collections.unmodifiableList(problemList);
	}

}
