/** RandomSchedulingAlgorithm.java
 * 
 * A scheduling algorithm that randomly picks the next job to go.
 *
 * @author: Kyle Benson
 * Winter 2013
 *
 */
package com.jimweller.cpuscheduler;

import java.util.*;

public class RandomSchedulingAlgorithm implements SchedulingAlgorithm {

    private Vector<Process> jobs;
    private Random rand;

    RandomSchedulingAlgorithm(){
	rand = new Random();
	jobs = new Vector<Process>();
    }

    /** Add the new job to the correct queue.*/
    public void addJob(Process p){
	jobs.add(p);
    }
    
    /** Returns true if the job was present and was removed. */
    public boolean removeJob(Process p){
	return jobs.remove(p);

    }

    /** Returns the next process that should be run by the CPU, null if none available.*/
    public Process getNextJob(){
	return jobs.get(rand.nextInt() % jobs.size());
    }

    public String getName(){
	return "Random Job";
    }
}