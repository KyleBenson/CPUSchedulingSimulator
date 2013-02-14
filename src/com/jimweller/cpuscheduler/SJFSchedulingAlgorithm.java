/** SJFSchedulingAlgorithm.java
 * 
 * A shortest job first scheduling algorithm.
 *
 * @author: Kyle Benson
 * Winter 2013
 *
 */
package com.jimweller.cpuscheduler;

import java.util.*;

public class SJFSchedulingAlgorithm extends BaseSchedulingAlgorithm implements OptionallyPreemptiveSchedulingAlgorithm {
    private boolean preemptive;
    private Vector<Process> jobs;

    SJFSchedulingAlgorithm(){
	activeJob = null;
	preemptive = false;
	jobs = new Vector<Process>();
    }

    /** Add the new job to the correct queue.*/
    public void addJob(Process p){
	jobs.add(p);
    }
    
    /** Returns true if the job was present and was removed. */
    public boolean removeJob(Process p){
	if (p == activeJob)
	    activeJob = null;
	return jobs.remove(p);
    }

    /** Transfer all the jobs in the queue of a SchedulingAlgorithm to another, such as
	when switching to another algorithm in the GUI */
    public void transferJobsTo(SchedulingAlgorithm otherAlg) {
	for (int i = jobs.size()-1; i >= 0; i--) {
	    Process job = this.jobs.get(0);
	    this.removeJob(job);
	    otherAlg.addJob(job);
	}
    }

    /** Returns the next process that should be run by the CPU, null if none available.*/
    public Process getNextJob(long currentTime){
	Process p=null,shortest=null;
	long time=0,shorttime=0;
	
	if (!isJobFinished() && !isPreemptive())
	    return activeJob;
	
	for(int i=0; i < jobs.size(); ++i){
	    p = (Process) jobs.get(i);
	    time = p.getBurstTime();
	    if( (time < shorttime) || (i == 0) ){
		shorttime = time;
		shortest = p;
	    }
	}
	activeJob = shortest;
	return activeJob;
    }

    public String getName(){
	return "Shortest job first";
    }

    /**
     * @return Value of preemptive.
     */
    public boolean isPreemptive(){
	return preemptive;
    }
    
    /**
     * @param v  Value to assign to preemptive.
     */
    public void setPreemptive(boolean  v){
	preemptive = v;
    }
}