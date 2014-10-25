import java.util.Arrays;
import java.util.Scanner;

/**
 * 
 * 1024 frames in PM
 * 512 words per frame
 *
 */
public class VMSim {
	
	Scanner sc = new Scanner(System.in);
	boolean[] frames = new boolean[1024];
	int[] pm = new int[524288];
	{
		Arrays.fill(frames, false);
		frames[0] = true;
		Arrays.fill(pm, 0);
	}
	
	
	public void run() {
		init();
	}
	
	public void init() {
		// get the segment PT addresses
		String initSt = sc.nextLine();
		String[] initStArray = initSt.split(" ");
		for (int i = 0; i < initStArray.length; i+=2) {
			int segment = Integer.parseInt(initStArray[i]);
			int ptAddress = Integer.parseInt(initStArray[i+1]);
			setSegment(segment, ptAddress);
		}
		
		// get the page addresses of each segment
		String initPt = sc.nextLine();
		String[] initPtArray = initPt.split(" ");
		for (int i = 0; i < initPtArray.length; i+=3) {
			int page = Integer.parseInt(initPtArray[i]);
			int segment = Integer.parseInt(initPtArray[i+1]);
			int pgAddress = Integer.parseInt(initPtArray[i+2]);
			setPage(page, segment, pgAddress);
		}
	}

	
	
	
	
	
	
	
	
	
	

	private void setSegment(int segment, int ptAddress) {
		//TODO
		if (frameOccupied(ptAddress)) {
			println("frame already occupied, cannot set");
		} else {
			pm[segment] = ptAddress;
			setFramePT(ptAddress);
		}
	}

	private void setPage(int page, int segment, int pgAddress) {
		//TODO
		if (frameOccupied(pgAddress)) {
			println("frame already occupied, cannot set");
		} else {
			int ptAddress = getPtAddress(segment);
			pm[ptAddress + page] = pgAddress;
			setFramePage(pgAddress);
		}
	}
	
	public boolean frameOccupied(int address) {
		int frame = getFrame(address);
		return frames[frame];
	}
	
	public void setFramePage(int address) {
		int frame = getFrame(address);
		frames[frame] = true;
	}
	
	public void setFramePT(int address) {
		int frame = getFrame(address);
		frames[frame] = true;
		frames[frame+1] = true;
	}
	
	private int getFrame(int address) {
		return address / 1024;
	}

	private int getPtAddress(int segmentNumber) {
		return pm[segmentNumber];
	}
	
	private int getPgAddress(int ptAddress, int pageNumber) {
		return pm[ptAddress + pageNumber];
	}
	
	
	
	
	
	
	
	

	public static void main(String[] args) {
		new VMSim().run();
	}
	
	public void println(String s) {
		System.out.println(s);
	}
}
