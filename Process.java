package lab_4;

public class Process {
	int pid, pSize, refsRemaining, faults, residency, ref, numEvictions; 
	double avgRes = 0.0;
	boolean isTerminated = false;
	Job job;
	
	Process(int pid, int pSize, int refsRemaining, int ref, int numRef, Job job) {
		super();
		this.pid = pid;
		this.pSize = pSize;
		this.refsRemaining = refsRemaining;
		this.ref = ref;
		this.refsRemaining = numRef;
		this.job = job;
	}
}