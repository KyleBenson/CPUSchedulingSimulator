package com.jimweller.cpuscheduler;

import java.util.*;
import java.io.*;
//import Process;
import java.text.*;


/** CPUScheduler runs a simulation of one of four different scheduling
    algorithms (FCFS,SJF,ROUNDROBIN,PRIORITY).  It can be set to run the whole simulation
    automatically in one fell swoop, or the programmer can imcrement
    on a step by step basis.
    @author Jim Weller
    @version 0.50 */

public class CPUScheduler{

    /** A constant for use in specifying the First Come 
	First Serve scheduling algorithm */
    public static final int FCFS       = 1;

    /** A constant for use in specifying the 
	Shortes Job First scheduling algorithm */
    public static final int SJF        = 2;

    /** A constant for use in specifying the 
	Priority Queue scheduling algorithm */
    public static final int PRIORITY   = 3;

    /** A constant for use in specifying the 
	Round Robin scheduling algorithm */
    public static final int ROUNDROBIN = 4;

    /** The default number of processes to randomly generate. The
	programmer can use the articulate constructor to build their
	own process set of any lenghth */
    static final int DEF_PROC_COUNT=50;

    /** This simulates elapsed time. */
    long currentTime=0;

    /** The amount of elapsed idle time. */
    long idle=0;

    /** The amount of elapsed time that the CPU was kept busy. */
    long busy=0;

    /** for use in the round robin algorithm. It is the timeslice each process
	gets */
    long quantum=10;

    /** A  count down to when to interrupt a process because it's timeslice is
	over. */
    long quantumCounter=quantum;

    /** Only for the priority round robin algorithm, this variable keeps track of
	the number of consecutive timeslices a process has consumed. */
    long turnCounter=0;

    /** The number of jobs submitted for execution. */
    int procsIn=0;

    /** the number of jobs that have been executed to completion. */
    int procsOut=0;

    /** Whether to use premption for the SJF and Priority algorithms. */
    boolean preemptive=false;

    /** Whether to use priority weights for the round robin algorithm. */
    boolean priority=false;

    /** The algorithm to use for this simulation. */
    int algorithm=this.FCFS;
    

    /** The collection of all processes involved in this simulation. 
	Extraneous now but handy for debugging.*/
    Vector allProcs   = new Vector(DEF_PROC_COUNT);

    /** The collection of all jobs that will be used */
    Vector jobQueue   = new Vector(DEF_PROC_COUNT);

    /** The collection of all jobs that have arrived and require CPU time. */
    Vector readyQueue = new Vector(DEF_PROC_COUNT);

    /** A reference to the currently active job. The cpu changes this reference to 
	different jobs in the ready queue using the respective algorithm's criteria */
    Process activeJob = null;

    /** The index into the vector/array/readyQueue. */
    int     activeIndex = 0;

    /* Variables to store harvested statistics on wait, response and
       turnaround time */
    int minWait=0,maxWait=0;
    double meanWait=0.0,sDevWait=0.0;

    int minResponse=0,maxResponse=0;
    double meanResponse=0.0,sDevResponse=0.0;

    int minTurn=0,maxTurn=0;
    double meanTurn=0.0,sDevTurn=0.0;

    /** Default constructor which builds DEF_PROC_COUNT randomly
	generated processes and loads them into the job queue */
    CPUScheduler(){
	buildRandomQueue();
    }

   /** Empty and populate a CPUScheduler */
   void buildRandomQueue() {
        activeJob = null;
	jobQueue.clear();
	allProcs.clear();
        Process p;
        for(int i=0; i < DEF_PROC_COUNT; i++){
            p = new Process();
            allProcs.add(p);
        }
        LoadJobQueue(allProcs);	
   }    

    /** Articulate constructor that allows the programmer to design
	his/her own Vector of processes and use them in the scheduler */
    CPUScheduler(Vector ap){
	activeJob = null;
	allProcs = ap;
	LoadJobQueue(ap);
    }
    

    /**
     * Articulate constructor that reads the process data from a file.
     * @param filename a string containing the file to open 
     */
    CPUScheduler(String filename){
	activeJob = null;
	Process proc = null;
	String s = null;
	long b=0,d=0,p=0;
	try{
	    BufferedReader input = new BufferedReader(new FileReader(filename));
  	    while( (s = input.readLine()) != null ){
  	        StringTokenizer st = new StringTokenizer(s);
  	        b = Long.parseLong(st.nextToken());
  	        d = Long.parseLong(st.nextToken());
  	        p = Long.parseLong(st.nextToken());
  	        proc = new Process(b,d,p);
  	        allProcs.add(proc);
 	    }
	    
	}
	catch(FileNotFoundException fnfe){}
	catch(IOException ioe){}
	LoadJobQueue(allProcs);
    }



   /**
     * Articulate constructor that reads the process data from a file.
     * @param filename A File object to read data from
     */
    CPUScheduler(File filename){
	activeJob = null;
	Process proc = null;
	String s = null;
	long b=0,d=0,p=0;
	try{
	    BufferedReader input = new BufferedReader(new FileReader(filename));
  	    while( (s = input.readLine()) != null ){
  	        StringTokenizer st = new StringTokenizer(s);
  	        b = Long.parseLong(st.nextToken());
  	        d = Long.parseLong(st.nextToken());
  	        p = Long.parseLong(st.nextToken());
  	        proc = new Process(b,d,p);
  	        allProcs.add(proc);
 	    }
	    
	}
	catch(FileNotFoundException fnfe){}
	catch(IOException ioe){}
    LoadJobQueue(allProcs);
    }



    
    /** Use the appropriate scheduler to choose the next process. Then
     * dispatch the process. */
    void Schedule(){
	switch( algorithm ){
	case FCFS :
	    RunFCFS(readyQueue);
	    break;
	case SJF :
	    RunSJF(readyQueue);
	    break;
	case PRIORITY :
	    RunPriority(readyQueue);
	    break;
	case ROUNDROBIN :
	    RunRoundRobin(readyQueue);
	    break;
	default:
	    System.out.println("Not a valid scheduling algorithm");
	    break;
	}
	Dispatch();
    }


    /** Actually run the active job and wait the rest of them */
    void Dispatch(){
	Process p=null;

	activeJob.executing(currentTime);
	for(int i=0 ; i < readyQueue.size() ; ++i){
	    p = (Process) readyQueue.get(i);
	    if( p.getPID() !=  activeJob.getPID() ){
		p.waiting(currentTime);
	    }
	}

    }

    /** Do the FCFS scheduling algorithm */
    void RunFCFS(Vector jq){
	Process p;
	
	try {
	    if(  busy == 0 || activeJob.getBurstTime() == 0 ){
		activeJob = findEarliestJob(jq);
		activeIndex = jq.indexOf(activeJob);
	    }
	}
	catch( NullPointerException e){
	}
    }


    /** Do the SJF scheduling algorithm. */
    void RunSJF(Vector jq){
	Process p;
	try {
	    if(  busy == 0 || activeJob.isFinished() || preemptive == true ){
		activeJob = findShortestJob(jq);
		activeIndex = jq.indexOf(activeJob);
	    }
	}
	catch( NullPointerException e){
	}
    }

    /** Do the Priority scheduling algorithm */
    void RunPriority(Vector jq){
	try {
	    if( busy == 0 || activeJob.isFinished() || preemptive == true ){
		activeJob = findLoftiestJob(jq);
		activeIndex = jq.indexOf(activeJob);
	    }
	}
	catch( NullPointerException e){
	}
    }

    /** Do the RR scheduling algorithm */
    void RunRoundRobin(Vector jq){
	Process p=null;
	
	try {
	    if( busy == 0 || activeJob.isFinished() || quantumCounter == 0){
		activeJob = findNextJob(jq);
		activeIndex = jq.indexOf(activeJob);
		if( priority == true ){
		    // weight timeslice by priority
		    //quantumCounter = quantum * activeJob.getPriorityWeight(); // backwards
		    quantumCounter = quantum * (10 - activeJob.getPriorityWeight() ); // backwards
		}
		else{
		    quantumCounter = quantum;
		}
		
	    }
	    quantumCounter--;
	}
	catch( NullPointerException e){
	}
	
    }


    /** 
     * RR: get the next job we should run (could be the one we're running).
     */
    Process findNextJob(Vector que){
	Process p = null , nextJob = null;
	int index=0;
	
	// All right who's next?  (index)
	if( activeIndex >= (que.size() - 1) )
	    index = 0;
	else if( activeJob != null && activeJob.isFinished() ){
	    index = activeIndex;
	}
	else{
		index = (activeIndex + 1);
	}
	
	nextJob = (Process) que.get(index);

	return nextJob;
    }


    /**
     * SJF: Locate the smallest job in a queue 
     */
    Process findShortestJob(Vector que){
	Process p=null,shortest=null;
	long time=0,shorttime=0;
	
	for(int i=0; i < que.size(); ++i){
	    p = (Process) que.get(i);
	    time = p.getBurstTime();
	    if( (time < shorttime) || (i == 0) ){
		shorttime = time;
		shortest = p;
	    }
	}
	return shortest;
    }

    /** 
     * FCFS: Get the job that got here first 
     */
    Process findEarliestJob(Vector que){
	Process p=null,earliest=null;
	long time=0,arrTime=0;
	
	for(int i=0; i < que.size(); ++i){
	    p = (Process) que.get(i);
	    time = p.getArrivalTime();
	    if( (time < arrTime) || (i == 0) ){
		arrTime = time;
		earliest = p;
	    }
	}
	return earliest ;
    }

    
    /** 
     * find the job with the highest priority
     * In case of tie take first in the queue 
     */
    Process findLoftiestJob(Vector que){
	Process p=null,loftiest=null;
	long priority=0, highest=0;
	
	for(int i=0; i < que.size(); ++i){
	    p = (Process) que.get(i);
	    priority = p.getPriorityWeight();
	    if( ( priority < highest ) || (i == 0) ){
		highest = priority;
		loftiest = p;
	    }
	}
	return loftiest;
    }
		

    /**
     * Loop through the job queue and grab important statistics
     */
    private void harvestStats(){
	int allWaited=0,allResponded=0, allTurned=0;
	int sDevWaited=0, sDevWaitedSquared=0;
	int sDevTurned=0,sDevTurnedSquared=0;
	int sDevResponded=0,sDevRespondedSquared=0;
	int startedCount=0,finishedCount=0;
	Process p=null;
	int i=0;

	for( i=0;i < allProcs.size();i++){
	    p = (Process) allProcs.get(i);
	    if(p.isStarted() ){
		startedCount++;
		int responded = (int) p.getResponseTime();
		allResponded += responded;
		sDevResponded += responded;
		sDevRespondedSquared += responded * responded;
		if( responded < minResponse || i == 0 ){
		    minResponse = responded;
		}
		else if( responded > maxResponse || i == 0){
		    maxResponse = responded;
		}
	    }
	}
	
	if( startedCount > 0){
	    meanResponse = ((double)allResponded) / ((double)startedCount);
	    if(startedCount > 1){
		double sdev =  (double)sDevRespondedSquared;
		sdev -= (double)(sDevResponded*sDevResponded) / (double) startedCount;
		sdev /= (double) (startedCount-1);
		sDevResponse = Math.sqrt(sdev);
	    }
	    else{
		sDevResponse = 0.0;
	    }

	}
	else{
	    meanResponse = 0.0;
	    sDevResponse = 0.0;
	}


	for( i=0;i < allProcs.size();i++){
	    p = (Process) allProcs.get(i);
	    
	    if( p.isFinished() ){
		finishedCount++;
		int waited = (int) p.getWaitTime();
		int turned = (int) p.getLifetime();
		allWaited += waited;
		sDevWaited += waited;
		sDevWaitedSquared += waited * waited;
		allTurned += turned;
		sDevTurned += turned;
		sDevTurnedSquared += turned*turned;

		if ( waited < minWait || i == 0){
		    minWait = waited;
		}
		else if( waited > maxWait || i == 0){
		    maxWait = waited;
		}
		

		if( turned < minTurn || i == 0){
		    minTurn = turned;
		}
		else if ( turned > maxTurn || i == 0){
		    maxTurn = turned;
		}
		
	    }
	}


	if( finishedCount > 0){
	    meanWait = (double)allWaited / (double)finishedCount;
	    meanTurn = (double)allTurned/(double)finishedCount;

	    if( finishedCount > 1){
		double sdev =  (double)sDevWaitedSquared ;
		sdev -= (double)(sDevWaited*sDevWaited) / (double) finishedCount;
		sdev /= (double) ( finishedCount-1);
		sDevWait = Math.sqrt(sdev);
		sdev = 0.0;
		sdev =  (double)sDevTurnedSquared ;
		sdev -= (double)(sDevTurned*sDevTurned) / (double) finishedCount;
		sdev /= (double) (finishedCount-1);
		sDevTurn = Math.sqrt(sdev);
	    }
	    else{
		sDevWait = 0.0;
		sDevTurn = 0.0;
	    }
	}
	else{
	    meanWait = 0.0;
	    meanTurn = 0.0;
	}

    }
    	
    /** Check for new jobs. */
    void LoadReadyQueue(){
	Process p;
	for(int i=0; i < jobQueue.size() ; i++){
	    p = (Process) jobQueue.get(i);
	    if( p.getArrivalTime() == currentTime){
		readyQueue.add(p);
		procsIn++;
	    }
	}
	
    }
    
    /** Remove finished jobs. */
    void PurgeReadyQueue(){
	Process p;
	for(int i=0; i < readyQueue.size(); i++){
	    p = (Process) readyQueue.get(i);
	    if( p.isFinished() == true ){
		readyQueue.remove(i);
		procsOut++;
	    }
	}
    }	

    
    /** Get rid of jobs that are done */
    void PurgeJobQueue(){
	Process p;
	for(int i=0; i < jobQueue.size(); i++){
	    p = (Process) jobQueue.get(i);
	    if( p.isFinished() == true ){
		jobQueue.remove(i);
	    }
	}
    }
	
    /** Load all the jobs into the job queue and setup their arrival times */
    public void LoadJobQueue(Vector jobs){
	Process p;
	long arTime = 0;
	for(int i = 0 ; i < jobs.size() ; i++ ){
	    p  = (Process) jobs.get(i);
	    arTime += p.getDelayTime();
	    p.setArrivalTime(arTime);
	    jobQueue.add(p);
 	}
    }
	
    
    /** Dump to terminal. */
    public void print(){
	Process p;
	for(int i=0; i < allProcs.size(); i++){
	    p = (Process) allProcs.get(i);
	    p.print();
	    System.out.println("---------------");
	}
    }

    /** Dump ready queue to terminal. */
    public void printReadyQueue(){
	Process p;
	for(int i=0; i < readyQueue.size(); i++){
	    p = (Process) readyQueue.get(i);
	    p.print();
	    System.out.println("---------------");
	}
    }


    /** kindof nice looking table. Java sucks for text formatting. Printf? */
    public void printTable(){
	Process p;
	for(int i=0; i < allProcs.size(); i++){
	    p = (Process) allProcs.get(i);
	    p.println();
	}
    }


    /** kindof ugly table to import into spreadsheet. */
    public void printCSV(){
	Process p;
	System.out.println(getAlgorithmName()+","+
	                   (getPriority() ? "Priority," : ",")+
	                   (getPreemption() ? "Preemptive" : ""));
	System.out.println("\"PID\","+
                     "\"Burst\","+
                     "\"Priority\","+
                     "\"Arrival\","+
                     "\"Start\","+
                     "\"Finish\","+
                     "\"Wait\","+
                     "\"Response\","+
                     "\"Turnaround\"");
                     
	for(int i=0; i < allProcs.size(); i++){
	    p = (Process) allProcs.get(i);
	    p.printCSV();
	}
    NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(2);
	nf.setMinimumFractionDigits(2);
	nf.setGroupingUsed(false);

	System.out.println(",,,,,,,,");
	System.out.println(",,,,,"+
	                   "Min,"+minWait+","+minResponse+","+minTurn);
	System.out.println(",,,,,"+
	                   "Mean,"+nf.format(meanWait)+","+nf.format(meanResponse)+","+nf.format(meanTurn));
	System.out.println(",,,,,"+
	                   "Max,"+maxWait+","+maxResponse+","+maxTurn);
	System.out.println(",,,,,"+
	                   "StdDev,"+nf.format(sDevWait)+","+nf.format(sDevResponse)+","+nf.format(sDevTurn));
    }




    /** kindof ugly table to import into spreadsheet. */
    public void printCSV(PrintWriter pw){
	Process p;
	pw.println(getAlgorithmName()+","+
	                   (getPriority() ? "Priority," : ",")+
	                   (getPreemption() ? "Preemptive" : ""));
	pw.println("\"PID\","+
                     "\"Burst\","+
                     "\"Priority\","+
                     "\"Arrival\","+
                     "\"Start\","+
                     "\"Finish\","+
                     "\"Wait\","+
                     "\"Response\","+
                     "\"Turnaround\"");
                     
	for(int i=0; i < allProcs.size(); i++){
	    p = (Process) allProcs.get(i);
	    p.printCSV(pw);
	}
    NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(2);
	nf.setMinimumFractionDigits(2);
	nf.setGroupingUsed(false);

	pw.println(",,,,,,,,");
	pw.println(",,,,,"+
	                   "Min,"+minWait+","+minResponse+","+minTurn);
	pw.println(",,,,,"+
	                   "Mean,"+nf.format(meanWait)+","+nf.format(meanResponse)+","+nf.format(meanTurn));
	pw.println(",,,,,"+
	                   "Max,"+maxWait+","+maxResponse+","+maxTurn);
	pw.println(",,,,,"+
	                   "StdDev,"+nf.format(sDevWait)+","+nf.format(sDevResponse)+","+nf.format(sDevTurn));
    }





    /**
     * Get the value of preemptive.
     * @return Value of preemptive.
     */
    public boolean getPreemption() {return preemptive;}
    
    /**
     * Set the value of preemptive.
     * @param v  Value to assign to preemptive.
     */
    public void setPreemption(boolean  v) {this.preemptive = v;}
    
    /**
     * Set the value of algorithm.
     * @param algo The algorithm to use for this simualtion.
     */
    public void setAlgorithm(int algo){ algorithm = algo;}

    /**
     * Get the value of algorithm.
     * @return Value of algorithm.
     */
    public int getAlgorithm(){ return algorithm; }





    /**
     * Get the number of idle cpu cycles.
     * @return Number of idle cpu cycles.
     */
    public long getIdleTime() {return idle;}

    /**
     * Get the total time this simulation has been running.
     * @return total running time.
     */
    public long getTotalTime() {return currentTime;}

    /**
     * Get the amount of time the CPU has been used so far.
     * @return Busy cpu cycles.
     */
    public long getBusyTime() {return busy;}


    /**
     * Get the value of quantum.
     * @return Value of quantum.
     */
    public long getQuantum() {return quantum;}
    
    /**
     * Set the value of quantum.
     * @param v  Value to assign to quantum.
     */
    public void setQuantum(long  v) {this.quantum = v;}
    

    /**
     * Get the value of priority.
     * @return Value of priority.
     */
    public boolean getPriority() {return priority;}
    
    /**
     * Set the value of priority.
     * @param v  Value to assign to priority.
     */
    public void setPriority(boolean  v) {this.priority = v;}


    /**
     * Get the number of completed processes .
     * @return Value of procsOut.
     */
    public long getProcsOut() {return procsOut;}

    /**
     * Get the number of recieved jobs.
     * @return Value of procsOut.
     */
    public long getProcsIn() {return  procsOut;}

    /**
     * Get the system load.  
     * @return The current sysetm load which is input processes over output processes. */
    public double getLoad() { return ( (double)procsIn/(double)procsOut ) ; }
    

    /** Get the Process that is actively being executed */
    public Process getActiveProcess( ){ return activeJob; }
    

    /** Run the whole simulation in one while loop */
    public void Simulate(){
	while( nextCycle() );
    }

    /** Just run one cycle of the simulation. This represents one time
     * unit. 
     * @return a boolean that is true if more cycles remain to be run.
     */
    public boolean nextCycle(){
	boolean moreCycles=false;
	if( jobQueue.isEmpty() ){
	    moreCycles = false;
	}
	else{
	    LoadReadyQueue();
	    moreCycles = true;
	    if( readyQueue.isEmpty() ){
		idle++;
	    }
	    else{
		Schedule();
		busy++;
		cleanUp();
	    }
	    currentTime++;
	}
	harvestStats();
	return moreCycles;
    }


    /**
     * Purge the runtime queues
     */
    void cleanUp(){
	PurgeJobQueue();
	PurgeReadyQueue();
    }


    /**
     * Restore time and statisitic variables to their defaults.
     * Also restores all processes to original state and reloads the 
     * queues. Leavs algorithm configurations alone an other state variables.
     */
    public void restore(){
	Process p;

	activeJob = null;
	currentTime = 0;
	busy = 0;
	idle = 0;
	procsIn = 0;
	procsOut = 0;
	quantum = 10;
	quantumCounter = quantum;
	turnCounter = 0;

	minWait=0;
	meanWait=0;
	maxWait=0;
 	sDevWait=0;

	minResponse=0;
	meanResponse=0;
	maxResponse=0;
 	sDevResponse=0;

	minTurn=0;
	meanTurn=0;
	maxTurn=0;
 	sDevTurn=0;

	for(int i=0;i<allProcs.size();i++){
	    p = (Process) allProcs.get(i);
	    p.restore();
	}
	jobQueue.clear();
	readyQueue.clear();
	LoadJobQueue(allProcs);

    }


   /**
     * Get all jobs
     * @return Vector of all Processes
     */
    public Vector getJobs()      { return allProcs; }

    /**
     * Get the mean process wait time
     * @return an int containting the mean wait
     */
    public double getMeanWait()  { return meanWait; }

    /**
     * Get the minimum process wait time
     * @return an int containting the minimum wait
     */
    public int getMinWait()      { return minWait;  }

    /**
     * Get the maximum process wait time
     * @return an int containting the maximum wait
     */
    public int getMaxWait()      { return maxWait;  }

    /**
     * Get the standard deviation in process wait time
     * @return an int containting the standard deviation for wait
     */
    public double getStdDevWait(){ return sDevWait; }


   /**
     * Get the mean process response time
     * @return an int containting the mean response
     */
     public double getMeanResponse()  { return meanResponse; }
    /**
     * Get the minimum process response time
     * @return an int containting the minimum response
     */
    public int getMinResponse()      { return minResponse;  }
    /**
     * Get the maximum process response time
     * @return an int containting the maximum response
     */
    public int getMaxResponse()      { return maxResponse;  }
   /**
     * Get the standard deviation in process response time
     * @return an int containting the standard deviation for response
     */
     public double getStdDevResponse(){ return sDevResponse; }


   /**
     * Get the mean process turn around time
     * @return an int containting the mean turn around
     */
     public double getMeanTurn()  { return meanTurn; }
    /**
     * Get the minimum process turn around time
     * @return an int containting the minimum turn around
     */
     public int getMinTurn()      { return minTurn;  }
   /**
     * Get the maximum process turn around time
     * @return an int containting the maximum turn around
     */
    public int getMaxTurn()      { return maxTurn;  }
   /**
     * Get the standard deviation in process turn around time
     * @return an int containting the standard deviation for turn around
     */
    public double getStdDevTurn(){ return sDevTurn; }



    /**
     * Get a string with the current algorithm's name
     * @return String containing the currently running algorithm's name
     */
    public String getAlgorithmName(){
	String s="";
	switch( algorithm ){
	case FCFS :
	    s = "First come first serve";
	    break;
	case SJF :
	    s = "Shortest job first";
	    break;
	case PRIORITY :
	    s = "Priority Weighted";
	    break;
	case ROUNDROBIN :
	    s = "Round Robin";
	    break;
	default:
	    break;
	}
	return s;
    }

}// ENDS class CPUScheduler
