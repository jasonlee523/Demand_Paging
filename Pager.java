package lab_4;
import java.util.*;
import java.io.*;

public class Pager {
	int fid, mSize, pageSize, procSize, job, numFrames, numRef, numPr, time, debug, q = 3;
	String algo;
	Scanner in;
	LinkedList<Process> pList = new LinkedList<Process>();
	LinkedList<Frame> frameList = new LinkedList<Frame>();
	
	private Pager(String mSize, String pageSize, String procSize, String job, String numRef, String algo, Scanner in) {
		super();
		this.mSize = Integer.parseInt(mSize);
		this.pageSize = Integer.parseInt(pageSize);
		this.procSize = Integer.parseInt(procSize);
		this.job = Integer.parseInt(job);
		this.numRef = Integer.parseInt(numRef);
		this.algo = algo;
		this.in = in;
		this.fid = this.numFrames = this.mSize / this.pageSize;		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		Scanner input = new Scanner(new File("lab_4/random-numbers.txt"));	
		Pager pager = new Pager(args[0], args[1], args[2], args[3], args[4], args[5], input);
		pager.initiate();
		int random, temp;
		double x;
		final double RAND_MAX = Integer.MAX_VALUE + 1d;
		Process running = null; 

		while(!pager.isTerminated()) {		
			running = pager.getNext();		
			for (int i = 0; i < pager.q; i++) {
				pager.time++;
				pager.checkFrames(running);
				running.refsRemaining--;
				random = input.nextInt();
				x = random / RAND_MAX;
				temp = running.ref;
				if (x < running.job.A)
					running.ref = (temp + 1 + pager.procSize) % pager.procSize;
				else if (x < (running.job.A + running.job.B))
					running.ref = (temp - 5 + pager.procSize) % pager.procSize;
				else if (x < (running.job.A + running.job.B + running.job.C))
					running.ref = (temp + 4 + pager.procSize) % pager.procSize;
				else if (x >= (running.job.A + running.job.B + running.job.C)) {
					int tempRand = input.nextInt();
					running.ref = (tempRand + pager.procSize) % pager.procSize;
				}
				if (running.refsRemaining == 0) {
					running.isTerminated = true;
					break;
				}
			}	
			pager.pList.remove(running);
			pager.pList.add(running);
		}
		pager.print();
	}
	
	void initiate() {
		Process p;
		if(job == 1) {
			p = new Process(1, this.procSize, this.numRef, (111+this.procSize)%this.procSize, this.numRef, new Job(1,0,0,0));
			pList.add(p);
		}
		else if(job == 2) {
			for (int i = 1; i <= 4; i++) {
				p = new Process(i, this.procSize, this.numRef, (111*i+this.procSize)%this.procSize,this.numRef, new Job(1,0,0,0));
				pList.add(p);
			}
		}
		else if(job == 3) {
			for (int i = 1; i <= 4; i++) {
				p = new Process(i, this.procSize, this.numRef, (111*i+this.procSize)%this.procSize, this.numRef, new Job(0,0,0,1));
				pList.add(p);
			}
		}
		else if(job == 4) {
			p = new Process(1, this.procSize, this.numRef, (111*1+this.procSize)%this.procSize, this.numRef, new Job(.75,.25,0,0));
			pList.add(p);
			p = new Process(2, this.procSize, this.numRef, (111*2+this.procSize)%this.procSize, this.numRef, new Job(.75,0,.25,0));
			pList.add(p);
			p = new Process(3, this.procSize, this.numRef, (111*3+this.procSize)%this.procSize, this.numRef, new Job(.75,.125,.125,0));
			pList.add(p);
			p = new Process(4, this.procSize, this.numRef, (111*4+this.procSize)%this.procSize, this.numRef, new Job(.5,.125,.125,.25));
			pList.add(p);
		}
	}
	
	void evict(Frame f, Process p) {
		f.timeEvicted = this.time;
		Process updateP = null;
		for (Process pTemp: this.pList) {
			if (pTemp.pid == f.pid) updateP = pTemp;
		}
		
		updateP.avgRes += f.timeEvicted - f.timeLoaded;
		p.faults++;
		updateP.numEvictions++;		
		f.timeLoaded = this.time;
		f.pid = p.pid;
		f.bounds(p.ref);
	}
	
	void checkFrames(Process p) {
		for (Frame f: frameList) {
			if ((f.lBound <= p.ref && p.ref <= f.uBound) && (f.pid == p.pid)) {
				if (!this.algo.equals("random")) {
					this.frameList.remove(f);
					this.frameList.add(f);
				}
				return;
			}
		}
		
		if (this.frameList.size() != numFrames) {
			p.faults++;
			Frame frame = new Frame(--this.fid, p.pid, this.pageSize, this.time);
			frame.bounds(p.ref);
			this.frameList.add(frame);
			if (this.algo.equals("random")) 
				this.frameList.sort(new FidComparator());
			return;
		}

		if (this.algo.equals("lru")) {
			Frame f = this.frameList.poll();
			this.evict(f, p);
			this.frameList.add(f);
		} else if (this.algo.equals("random")) {
			int randNum = this.in.nextInt();
			int randIndex =  (randNum + this.numFrames) % this.numFrames;
			Frame f = this.frameList.get(randIndex);
			this.evict(f, p);
		} else if (this.algo.equals("lifo")) {
			Frame frame = this.frameList.peekLast();
			for (Frame f: this.frameList) {
				if (f.timeLoaded > frame.timeLoaded)
					frame = f;
			}
			this.evict(frame, p);
		}
	}
	
	Process getNext() {
		for (int i = 0; i < this.pList.size(); i++) {
			Process temp = this.pList.peek();
			if (!temp.isTerminated)
				return temp;
			this.pList.poll();
			this.pList.add(temp);
		}
		return null;
	}
	
	boolean isTerminated() {
		for (int i = 0; i < pList.size(); i++) {
			if (!pList.get(i).isTerminated)
				return false;
		}
		return true;
	}
	
	void print() {
		System.out.printf("The machine size is %d.\n", this.mSize);
		System.out.printf("The page size is %d.\n", this.pageSize);
		System.out.printf("The process size is %d.\n", this.procSize);
		System.out.printf("The job mix number is %d.\n", this.job);
		System.out.printf("The number of references per process is %d.\n", this.numRef);
		System.out.printf("The replacement algoritm is %s.\n", this.algo);
		System.out.printf("The level of debugging output is %d.\n\n", this.debug);		
		int totFaults = 0, totEvictions = 0;
		double totAvgRes = 0;
		
		for (Process p: this.pList) {
			if (p.numEvictions != 0) {
				totAvgRes += p.avgRes;
				p.avgRes = p.avgRes / p.numEvictions;
				totEvictions += p.numEvictions;
			}
			totFaults += p.faults;		
			if (p.numEvictions == 0) {
				System.out.printf("Process %d had %d faults.\n", p.pid, p.faults);
				System.out.println("\tWith no evictions, the average residence is undefined.");
				continue;
			}
			System.out.printf("Process %d had %d faults and %f average residency.\n", p.pid, p.faults, p.avgRes);
		}
		
		totAvgRes = totAvgRes / totEvictions;
		System.out.printf("\nThe total number of faults is %d", totFaults);
		if (totEvictions == 0)
			System.out.println("\n\tWith no evictions, the overall average residence is undefined.");
		else
			System.out.printf(" and the overall average residency is %f.\n", totAvgRes);
	}
}