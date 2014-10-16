import java.util.Arrays;
import java.util.Scanner;


public class VMSim {
	
	Scanner sc = new Scanner(System.in);
	boolean[] freeFrames = new boolean[1024];
	{
		Arrays.fill(freeFrames, false);
		//println(Arrays.toString(freeFrames));
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
			assignSegment(segment, ptAddress);
		}
		
		// get the page addresses of each segment
		String initPt = sc.nextLine();
		String[] initPtArray = initPt.split(" ");
		for (int i = 0; i < initPtArray.length; i+=3) {
			int page = Integer.parseInt(initPtArray[i]);
			int segment = Integer.parseInt(initPtArray[i+1]);
			int pgAddress = Integer.parseInt(initPtArray[i+2]);
			assignPage(page, segment, pgAddress);
		}
	}

	
	
	
	
	
	
	
	
	
	

	private void assignPage(int page, int segment, int pgAddress) {
		//TODO
	}

	private void assignSegment(int segment, int ptAddress) {
		//TODO
	}

	public static void main(String[] args) {
		new VMSim().run();
	}
	
	public void println(String s) {
		System.out.println(s);
	}
}
