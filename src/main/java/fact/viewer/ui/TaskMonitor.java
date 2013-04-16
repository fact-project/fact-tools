package fact.viewer.ui;



/**
 * This interface defines a task monitor. The method defined within the interface
 * are called from outside "worker threads". A class implementing this interface
 * might then update the UI components.
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 */
public interface TaskMonitor {
	
    /**
     * This method is called as the given task has been started.
     * 
     * @param t The task that has been started.
     */
	public void taskStarted( Task t );
	
	
	/**
	 * This method is called as the given task has somehow advanced.
	 * 
	 * @param t The task that has made progress.
	 */
	public void taskAdvanced( Task t );
	
	
	/**
	 * This is called as the given task is completed.
	 * 
	 * @param t The task that has finished.
	 */
	public void taskFinished( Task t );
	
	
	/**
	 * This method is called if the given task has been paused.
	 * 
	 * @param t The task that has been paused.
	 */
	public void taskPaused( Task t );
	
	
	/**
	 * This method is called if the given task has been resumed.
	 * 
	 * @param t The task that has been resumed.
	 */
	public void taskResumed( Task t );
}
