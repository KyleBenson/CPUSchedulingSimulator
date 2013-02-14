/** PrioritySchedulingAlgorithm.java
 * 
 * A single-queue priority scheduling algorithm.
 *
 * @author: Kyle Benson
 * Winter 2013
 *
 */
package com.jimweller.cpuscheduler;

import java.util.*;

public class PrioritySchedulingAlgorithm extends BaseSchedulingAlgorithm {

    PrioritySchedulingAlgorithm(){
	super();
    }

    /** Add the new job to the correct queue.*/
    public void addJob(Process p){

    }
    
    /** Returns true if the job was present and was removed. */
    public boolean removeJob(Process p){

    }

    public boolean shouldPreempt(long currentTime){

    }

    /** Transfer all the jobs in the queue of a SchedulingAlgorithm to another, such as
	when switching to another algorithm in the GUI */
    public void transferJobsTo(SchedulingAlgorithm otherAlg) {

    }


    /** Returns the next process that should be run by the CPU, null if none available.*/
    public Process getNextJob(long currentTime){
    }

    public String getName(){
	return "Single-queue Priority";
    }
}