package lab_4;
import java.util.Comparator;

public class FidComparator implements Comparator<Frame> {
	public int compare(Frame f1, Frame f2) {
		return ((f1.fid < f2.fid) ? -1 : ((f1.fid == f2.fid) ? 0 : 1));
	}
}
