/** RoundRobinSchedulingAlgorithm.java
 * 
 * A scheduling algorithm that randomly picks the next job to go.
 *
 * @author: Kyle Benson
 * Winter 2013
 *
 */
package com.jimweller.cpuscheduler;

import java.util.*;

public class RoundRobinSchedulingAlgorithm extends BaseSchedulingAlgorithm {

    private Vector<Process> jobs;

    /** the timeslice each process gets */
    private int quantum;

    /**
     * A count down to when to interrupt a process because it's timeslice is
     * over.
     */
    private int quantumCounter;

    /**
     * this variable keeps track of the number of consecutive timeslices a
     * process has consumed.
     */
    private long turnCounter;

    /** The index into the vector/array/readyQueue. */
    private int activeIndex;

    private boolean priority;

    RoundRobinSchedulingAlgorithm() {
	activeJob = null;
	quantum = 10;
	turnCounter = 0;
	quantumCounter = quantum;
	activeIndex = -1;
	priority = false;
	jobs = new Vector<Process>();
    }

    /** Add the new job to the correct queue. */
    public void addJob(Process p) {
	jobs.add(p);
    }

    /** Returns true if the job was present and was removed. */
    public boolean removeJob(Process p) {
	int jobIndex = jobs.indexOf(p);
	boolean ret = jobs.remove(p);
	if (activeIndex >= jobIndex && jobIndex >= 0){
	    activeIndex--;
	    try {
		activeJob = jobs.get(activeIndex);
	    } catch (ArrayIndexOutOfBoundsException e){
		activeJob = null;
	    }
	}
	quantumCounter = 0; //so that we know to preempt next cycle
	return ret;
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

    /**
     * Get the value of quantum.
     * 
     * @return Value of quantum.
     */
    public int getQuantum() {
	return quantum;
    }

    /**
     * Set the value of quantum.
     * 
     * @param v
     *            Value to assign to quantum.
     */
    public void setQuantum(int v) {
	this.quantum = v;
    }

    /**
     * Returns the next process that should be run by the CPU, null if none
     * available.
     */
    public Process getNextJob(long currentTime) {
	Process p = null, nextJob = null;
	int index = 0;

	quantumCounter--; //hint: this gets called every tick of the CPU clock

	//not time to preempt
	if (activeIndex >= 0 && !isJobFinished() && quantumCounter > 0) {
	    return activeJob;
	}

	//no jobs available
	if (jobs.size() == 0){
	    activeIndex = -1;
	    return null;
	}

	// All right who's next? (index)
	if (activeIndex >= (jobs.size() - 1) || activeIndex < 0)
	    index = 0;
	else {
	    index = (activeIndex + 1);
	}

	nextJob = (Process) jobs.get(index);
	activeIndex = index;

	/*if (priority == true) {
	    // weight timeslice by priority
	    // quantumCounter = quantum * activeJob.getPriorityWeight(); //
	    // backwards
	    quantumCounter = quantum * (10 - nextJob.getPriorityWeight()); // backwards
	    } else {*/
	quantumCounter = quantum;
	//}

	this.activeJob = nextJob;
	return nextJob;
    }

    public String getName() {
	return "Round Robin";
    }
}