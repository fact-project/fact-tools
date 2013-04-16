/*
 *  Copyright (C) 2007 Christian Bockermann <chris@jwall.org>
 *
 *  This file is part of the  WebTap.
 *
 *  WebTap  is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  WebTap  is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fact.viewer.ui;

import java.util.Vector;



/**
 * 
 * This class defines a simple task. A task is like a thread, but will notify
 * all registered task monitors as the task advances. 
 * 
 * To use this class you simply extend <code>Task</code> and implement the working 
 * method <code>run()</code>. Within <code>run()</code> you may then adjust the
 * level of completion or the like and notify all registered monitors by calling
 * <code>advanced()</code>.
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public abstract class Task
extends Thread
{
    
    /** The list of monitors, registered to this task */
	private Vector<TaskMonitor> monitors;
	
	/**
	 * This creates a simple task instance with an empty list
	 * of task monitors.
	 */
	public Task(){
		monitors = new Vector<TaskMonitor>();
	}

	
	/**
	 * This method shall return the fraction of completion of this task.
	 * 
	 * @return A value between 0.0 and 1.0, denoting the advancement of the task.
	 */
    public abstract double percentageCompleted();
    
    
    /**
     * The status may be an arbitrary text message which describes the current
     * status of the task (like &quot;Copying files...&quot; or the like).
     * 
     * @return The status message of this task.
     */
    public abstract String getStatus();
    

    /**
     * This method adds the given monitor to the list of monitors notified by
     * any advancement of this task.
     * 
     * @param m The monitor to be added.
     */
    public void addTaskMonitor( TaskMonitor m ){
    	monitors.add( m );
    }
    

    /**
     * This method removes the given monitor from the list of monitors notified by
     * any advancement of this task.
     * 
     * @param m The monitor to be added.
     */
    public void removeTaskMonitor( TaskMonitor m ){
    	monitors.remove( m );
    }
    
    
    /**
     * This method notifies all monitors that the task has advanced.
     */
    public void advanced(){
    	for( TaskMonitor m : monitors )
    		m.taskAdvanced( this );
    }
    

    /**
     * This method notifies all monitors that the task has finished.
     */
    public void finish(){
    	for( TaskMonitor m : monitors )
    		m.taskFinished( this );
    }

    
    /**
     * This method starts the thread (i.e. the real work) and notifies all
     * monitors that the task has been started.
     */
    public void start(){
    	super.start();
    	for( TaskMonitor m : monitors )
    		m.taskStarted( this );
    }
    
    
    /**
     * This method is used for the work of this task.
     * 
     * @see java.lang.Thread#run()
     */
    public abstract void run();
}