package lab_4;

public class Frame {
	int fid, pid, fSize, timeLoaded, timeEvicted, lBound, uBound;

	Frame(int fid, int pid, int fSize, int timeLoaded) {
		super();
		this.fid = fid;
		this.pid = pid;
		this.fSize = fSize;
		this.timeLoaded = timeLoaded;
	}
	
	void bounds(int n) {
		this.lBound = (n/fSize)*fSize;
		this.uBound = lBound+fSize-1;
	}
}