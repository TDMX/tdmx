package plugon.lib.system.test;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * AsyncTestRunner - can be used to run test methods for a certain time
 * or number of repetitions with a separate thread.
 * 
 * Any exception recorded during running the method can be retrieved by
 * {@link #getError()}
 */
public class AsyncTestRunner extends Thread {
    
    //-------------------------------------------------------------------------
    //PUBLIC CONSTANTS
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    //PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
    //-------------------------------------------------------------------------
    private int repetitions = 1;
    private long duration = 0;
    private Object subject;
    private Throwable error;
    private String method;
    
    //-------------------------------------------------------------------------
    //CONSTRUCTORS
    //-------------------------------------------------------------------------
    
    public AsyncTestRunner( Object subject, String method ) {
    	this.subject = subject;
    	this.method = method;
    }

    //-------------------------------------------------------------------------
    //PUBLIC METHODS
    //-------------------------------------------------------------------------

    /**
     * Run an object's method in parallel for N repetitions
     * 
     * @param subject the object who's method is called
     * @param method the methodname on the subject which is called
     * @param parallel the number of concurrent threads calling the method
     * @param repetitions each thread calls the subject's method repetition times
     * 
     * @throws Exception if any of the concurrent threads failed running the subject's method
     */
    public static void runRepetitions( Object subject, String method, int parallel, int repetitions ) throws Exception {
    	run( subject, method, parallel, repetitions, 0);
    }
    
    /**
     * Run an object's method in parallel for duration
     * 
     * @param subject the object who's method is called
     * @param method the methodname on the subject which is called
     * @param parallel the number of concurrent threads calling the method
     * @param duration (ms) each thread calls the subject's method for the duration in ms.
     * 
     * @throws Exception if any of the concurrent threads failed running the subject's method
     */
    public static void runDuration( Object subject, String method, int parallel, long duration ) throws Exception {
    	run( subject, method, parallel, 1, duration);
    }
    
    @Override
    public void run() {
    	try {
	    	if ( getDuration() > 0 ) {
	    		long startTime = System.currentTimeMillis();
	    		long now = System.currentTimeMillis();
	    		do {
	    			doSubjectMethodCall();
	    			yield();
	    			now = System.currentTimeMillis();
	    		} while( startTime + getDuration() > now );
	    	} else {
	    		for ( int i= 0; i < getRepetitions(); i++ ) {
	    			doSubjectMethodCall();
	    			yield();
	    		}
	    	}
    	} catch ( Throwable t ) {
    		error = t;
    	}
    }
    
    //-------------------------------------------------------------------------
    //PROTECTED METHODS
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    //PRIVATE METHODS
    //-------------------------------------------------------------------------

    public static void run( Object subject, String method, int parallel, int repetitions, long duration ) throws Exception {
    	AsyncTestRunner[] runners = new AsyncTestRunner[parallel];
    	for( int i=0; i < runners.length; i++) {
    		AsyncTestRunner runner = new AsyncTestRunner(subject, method);
    		if ( duration > 0 ) {
    			runner.setDuration(duration);
    		}
    		runner.setRepetitions(repetitions);
    		runners[i] = runner;
    	}
    	for( int i=0; i < runners.length; i++) {
    		runners[i].start();
    	}
    	for( int i=0; i < runners.length; i++) {
    		runners[i].join();
    	}
    	for( int i=0; i < runners.length; i++) {
    		if ( runners[i].getError() != null ) {
    			throw new Exception("Runner " + i + " failed on method "+method, runners[i].getError());
    		}
    	}    	
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void doSubjectMethodCall() throws Throwable {
    	Class clazz = getSubject().getClass();
    	Method m = clazz.getMethod(getMethod(), new Class[0]);
    	try {
    		m.invoke(getSubject(), new Object[0]);
    	} catch ( InvocationTargetException e ) {
    		throw e.getTargetException();
    	}
    }
    
    //-------------------------------------------------------------------------
    //PUBLIC ACCESSORS (GETTERS / SETTERS)
    //-------------------------------------------------------------------------

	/**
	 * @return Returns the error.
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * @param error The error to set.
	 */
	public void setError(Throwable error) {
		this.error = error;
	}

	/**
	 * @return Returns the repetitions.
	 */
	public int getRepetitions() {
		return repetitions;
	}

	/**
	 * @param repetitions The repetitions to set.
	 */
	public void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}

	/**
	 * @return Returns the subject.
	 */
	public Object getSubject() {
		return subject;
	}

	/**
	 * @param subject The subject to set.
	 */
	public void setSubject(Object subject) {
		this.subject = subject;
	}

	/**
	 * @return Returns the method.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method The method to set.
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return Returns the duration.
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration The duration to set.
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}
    

}