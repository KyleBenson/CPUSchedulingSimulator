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

public class RoundRobinSchedulingAlgorithm implements SchedulingAlgorithm {

	private Vector<Process> jobs;

	/** the timeslice each process gets */
	private long quantum;

	/**
	 * A count down to when to interrupt a process because it's timeslice is
	 * over.
	 */
	private long quantumCounter;

	/**
	 * this variable keeps track of the number of consecutive timeslices a
	 * process has consumed.
	 */
	private long turnCounter;

	/** The index into the vector/array/readyQueue. */
	private int activeIndex;

	private boolean priority;

	RoundRobinSchedulingAlgorithm() {
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
		if (activeIndex > jobs.indexOf(p)) 
			activeIndex--;
		return jobs.remove(p);
	}

	/**
	 * Get the value of quantum.
	 * 
	 * @return Value of quantum.
	 */
	public long getQuantum() {
		return quantum;
	}

	/**
	 * Set the value of quantum.
	 * 
	 * @param v
	 *            Value to assign to quantum.
	 */
	public void setQuantum(long v) {
		this.quantum = v;
	}

	/**
	 * Returns the next process that should be run by the CPU, null if none
	 * available.
	 */
	public Process getNextJob() {
		Process p = null, nextJob = null;
		int index = 0;

		// All right who's next? (index)
		if (activeIndex >= (jobs.size() - 1))
			index = 0;
		else if (activeIndex >= 0 && jobs.get(activeIndex).isFinished()) {
			index = activeIndex;
		} else {
			index = (activeIndex + 1);
		}

		nextJob = (Process) jobs.get(index);
		activeIndex = index;

		if (priority == true) {
			// weight timeslice by priority
			// quantumCounter = quantum * activeJob.getPriorityWeight(); //
			// backwards
			quantumCounter = quantum * (10 - nextJob.getPriorityWeight()); // backwards
		} else {
			quantumCounter = quantum;
		}

		return nextJob;
	}

	public boolean shouldPreempt(long currentTime) {
		// lastTime = currentTime;
		boolean ret = (currentTime == 0 || activeIndex < 0 || jobs.get(activeIndex).isFinished() || quantumCounter == 0);
		quantumCounter--;
		return ret;
	}

	public String getName() {
		return "Round Robin";
	}
}