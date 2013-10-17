package org.tdmx.console.service;

import java.util.List;

import org.tdmx.console.domain.Domain;
import org.tdmx.console.domain.Problem;

public interface GlobalService {

	public abstract List<Domain> listDomains();

	public boolean hasProblems();
	public List<Problem> getProblems();
	public void deleteAllProblems();
	public void deleteProblem( int id );
}