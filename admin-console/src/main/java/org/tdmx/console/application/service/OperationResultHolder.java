package org.tdmx.console.application.service;

import org.tdmx.console.domain.validation.OperationError;


public class OperationResultHolder<E> {

	private OperationError error;
	private E result;
	
	public OperationError getError() {
		return error;
	}

	public void setError(OperationError error) {
		this.error = error;
	}

	public E getResult() {
		return this.result;
	}
	
	public void setResult(E result) {
		this.result = result;
	}
}
