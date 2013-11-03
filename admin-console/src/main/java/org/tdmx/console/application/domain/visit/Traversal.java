package org.tdmx.console.application.domain.visit;


public class Traversal {

	public static <O,R> R traverse( final Iterable<O> objects, R context, TraversalFunction<O,R> function ) {
		TraversalContextHolder<R> result = new TraversalContextHolder<>();
		result.setResult(context);
		
		for( O object : objects ) {
			function.visit(object, result);
			if ( result.isStopped() ) {
				return result.getResult();
			}
		}
		return result.getResult();
	}

}
