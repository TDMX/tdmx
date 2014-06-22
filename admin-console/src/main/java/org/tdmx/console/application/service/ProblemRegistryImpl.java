/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.console.application.service;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.tdmx.console.application.domain.ProblemDO;

public class ProblemRegistryImpl implements ProblemRegistry {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final LinkedList<ProblemDO> problemList = new LinkedList<>();
	private final int maxSize = 1000;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// TODO merge the last 2 problems together if the same - adding # to Problem
	@Override
	public void addProblem(ProblemDO problem) {
		problemList.addLast(problem);
		if (problemList.size() > maxSize) {
			problemList.removeFirst();
		}
	}

	@Override
	public void deleteProblem(String problemId) {
		if (problemId == null) {
			return;
		}
		Iterator<ProblemDO> it = problemList.iterator();
		while (it.hasNext()) {
			ProblemDO p = it.next();
			if (problemId.equals(p.getId())) {
				it.remove();
			}
		}
	}

	@Override
	public void deleteAllProblems() {
		problemList.clear();
	}

	@Override
	public List<ProblemDO> getProblems() {
		return Collections.unmodifiableList(problemList);
	}

	@Override
	public ProblemDO getLastProblem() {
		try {
			return problemList.getLast();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------
}
