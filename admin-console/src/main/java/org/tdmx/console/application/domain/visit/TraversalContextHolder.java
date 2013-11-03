package org.tdmx.console.application.domain.visit;


public class TraversalContextHolder<E> {

	private boolean stopped = false;
	public void stop() {
		stopped = true;
	}
	public boolean isStopped() {
		return stopped;
	}
	
	private E object;
	public E getResult() {
		return this.object;
	}
	
	public void setResult(E result) {
		this.object = result;
	}
	
}
