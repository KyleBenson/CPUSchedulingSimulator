/** BaseSchedulingAlgorithm.java
 * 
 * An abstract scheduling algorithm for others to inherit from.
 *
 * @author: Kyle Benson
 * Winter 2013
 *
 */
package com.jimweller.cpuscheduler;

import java.util.*;

public abstract class BaseSchedulingAlgorithm implements SchedulingAlgorithm {
    /** True if this algorithm will ever choose to preempt a currently running process. */
    protected boolean preemptive;

    /** The currently running process, null if none. */
    protected Process activeJob;

    /** Add the new job to the correct queue.*/
    public abstract void addJob(Process p);
    
    /** Returns true if the job was present and was removed. */
    public abstract boolean removeJob(Process p);

    /** Returns true if it is time to switch to another process. */
    public abstract boolean shouldPreempt(long currentTime);

    /** Returns the next process that should be run by the CPU, null if none available.*/
    public abstract Process getNextJob();

    /** Return a human-readable name for the algorithm. */
    public abstract String getName();

    /** Returns true if the current job is finished or there is no such job. */
    public boolean isJobFinished(){
	if (activeJob != null)
	    return activeJob.isFinished();
	else
	    return true;
    }

    /**
     * Get the value of preemptive.
     * @return Value of preemptive.
     */
    public boolean isPreemptive(){
	return preemptive;
    }
    
    /**
     * Set the value of preemptive.
     * @param v  Value to assign to preemptive.
     */
    public void setPreemptive(boolean  v){
	preemptive = v;
    }

}
