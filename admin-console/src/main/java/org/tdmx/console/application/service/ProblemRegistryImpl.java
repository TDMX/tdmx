package org.tdmx.console.application.service;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.tdmx.console.application.domain.Problem;

public class ProblemRegistryImpl implements ProblemRegistry {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private LinkedList<Problem> problemList = new LinkedList<>();
	private int maxSize = 1000;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	//TODO merge the last 2 problems together if the same - adding # to Problem
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

	@Override
	public Problem getLastProblem() {
		try {
			return problemList.getLast();
		} catch ( NoSuchElementException e ) {
			return null;
		}
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
}
