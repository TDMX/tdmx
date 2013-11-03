package org.tdmx.console.application.domain.visit;


public interface TraversalFunction<O,R> {

	public void visit( O object, TraversalContextHolder<R> holder);
	
}
