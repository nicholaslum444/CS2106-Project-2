import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * 
 * 1024 frames in PM
 * 512 words per frame
 *
 */
public class VMSim {
	
	static class TLB {

		private static final int DONT_HAVE = -2;
		
		static class Entry implements Comparable<Entry> {
			
			Integer lru;
			int sp;
			int pa;
			
			@Override
			public int compareTo(Entry o) {
				return lru.compareTo(o.lru);
			}
		}
		
		ArrayList<Entry> tlb = new ArrayList<Entry>();
		int maxSize = 4;
		
		Entry dequeue(Entry e) {
			if (tlb.remove(e)) {
				return e;
			} else {
				return null;
			}
		}
		
		void enqueue(Entry e) {
			tlb.add(e);
		}
		
		void add(Entry e) {
			if (tlb.size() < maxSize) {
				tlb.add(e);
			} else {
				tlb.remove(0);
				tlb.add(e);
			}
		}
		
		void storePA(int sp, int pa) {
			Entry e = new Entry();
			e.sp = sp;
			e.pa = pa;
			add(e);
		}
		
		int getPA(int sp, int w) {
			Entry e = null;
			for (Entry entry : tlb) {
				if (entry.sp == sp) {
					e = entry;
				}
			}
			if (e == null) {
				return DONT_HAVE;
			}
			dequeue(e);
			enqueue(e);
			return e.pa + w;
		}
		
	}
	
	static final String outFile = "A0108358B1.txt";
	static final String outFileTLB = "A0108358B2.txt";
	
	static final int READ = 0;
	static final int WRITE = 1;
	
	static final int mask_W =  0b00000000000000000000000111111111;
	static final int mask_P =  0b00000000000001111111111000000000;
	static final int mask_S =  0b00001111111110000000000000000000;
	static final int mask_SP = 0b00001111111111111111111000000000;
	
	private static final String PAGE_FAULT = "pf";
	private static final String ERROR = "err";
	
	
	boolean useTLB = false;
	TLB tlb = new TLB();
	
	boolean[] frames = new boolean[1024];
	int[] pm = new int[524288];
	
	{
		Arrays.fill(frames, false);
		frames[0] = true;
		Arrays.fill(pm, 0);
	}
	
	// format:
	// { VMSim.java input1.txt input2.txt } for no TLB
	// { VMSim.java input1.txt input2.txt tlb } for yes TLB
	public void run(String[] args) {
		String initFilename = args[0];
		String inputFilename = args[1];
		
		if (args.length > 2) {
			if (args[2].toLowerCase().equals("tlb")) {
				useTLB = true;
			}
		}
		
		try {
			init(initFilename);
		} catch (FileNotFoundException e) {
				debug("init fnf");
			e.printStackTrace();
		} catch (IOException e) {
				debug("init io");
			e.printStackTrace();
		}
		
		try {
			input(inputFilename);
		} catch (FileNotFoundException e) {
				debug("input fnf");
			e.printStackTrace();
		} catch (IOException e) {
				debug("input io");
			e.printStackTrace();
		}
		
	}
	
	public void init(String filename) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		
		// get the segment PT addresses
		String initSTString = br.readLine();
		initST(initSTString);
		
		// get the page addresses of each segment
		String initPTString = br.readLine();
			debug(initPTString);
		initPT(initPTString);
		br.close();
	}
	
	public void input(String filename) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		
		// get the segment PT addresses
		String inputString = br.readLine();
		String[] inputArray = inputString.split(" ");
		for (int i = 0; i < inputArray.length; i+=2) {
			int rw = Integer.parseInt(inputArray[i]);
			int va = Integer.parseInt(inputArray[i+1]);
			executeVA(rw, va);
		}
		
		br.close();
	}

	public void initST(String initSTString) {
		String[] initSTArray = initSTString.split(" ");
		for (int i = 0; i < initSTArray.length; i+=2) {
			int segment = Integer.parseInt(initSTArray[i]);
			int ptAddress = Integer.parseInt(initSTArray[i+1]);
			setSegment(segment, ptAddress);
		}
	}

	public void initPT(String initPTString) {
		String[] initPTArray = initPTString.split(" ");
		for (int i = 0; i < initPTArray.length; i+=3) {
			int page = Integer.parseInt(initPTArray[i]);
			int segment = Integer.parseInt(initPTArray[i+1]);
			int pgAddress = Integer.parseInt(initPTArray[i+2]);
			setPage(page, segment, pgAddress);
		}
	}

	private void executeVA(int rw, int va) {
		if (rw == READ) {
			readVA(va);
		} else if (rw == WRITE) {
			writeVA(va);
		} else {
				debug(rw + " is not read or write");
		}
		
	}

	private void readVA(int va) {
			debug("read va");
			debug(va);
		
		int segment = (va & mask_S) >> 19;
			debug("s = " + segment);
		int page = (va & mask_P) >> 9;
			debug("p = " + page);
		int offset = va & mask_W;
			debug("w = " + offset);
		int sp = (va & mask_SP) >> 9;
		
		int ptAddress = getPTAddress(segment);
		
		if (ptAddress == -1) {
			print(PAGE_FAULT);
			return;
		} else if (ptAddress == 0) {
			print(ERROR);
			return;
		}
		
		int pgAddress = getPgAddress(ptAddress, page);
		
		if (pgAddress == -1) {
			print(PAGE_FAULT);
			return;
		} else if (pgAddress == 0) {
			print(ERROR);
			return;
		}
		
		int pa;
		
		if (useTLB) {
			pa = tlb.getPA(sp, offset);
			if (pa != TLB.DONT_HAVE) {
				print("h");
				print(pa);
				return;
			} else {
				print("m");
				tlb.storePA(sp, pgAddress);
			}
		}

		pa = pgAddress + offset;
		
		print(pa);
		
	}
	
	private void writeVA(int va) {
			debug("read va");
			debug(va);
		
		int segment = (va & mask_S) >> 19;
			debug("s = " + segment);
		int page = (va & mask_P) >> 9;
			debug("p = " + page);
		int offset = va & mask_W;
			debug("w = " + offset);
		int sp = (va & mask_SP) >> 9;
		
		int ptAddress = getPTAddress(segment);
		
		if (ptAddress == -1) {
			print(PAGE_FAULT);
			return;
		} else if (ptAddress == 0) {
				debug("blank st");
			int freeFrameNumber = getFreePTFrame();
			int newPTAddress = getFrameAddress(freeFrameNumber);
			setSegment(segment, newPTAddress);
			ptAddress = newPTAddress;
		}
		
		int pgAddress = getPgAddress(ptAddress, page);
		
		if (pgAddress == -1) {
			print(PAGE_FAULT);
			return;
		} else if (pgAddress == 0) {
				debug("blank page");
			int freeFrameNumber = getFreePgFrame();
			int newPgAddress = getFrameAddress(freeFrameNumber);
			setPage(page, segment, newPgAddress);
			pgAddress = newPgAddress;			
		}
		
		int pa;
		
		if (useTLB) {
			pa = tlb.getPA(sp, offset);
			if (pa != TLB.DONT_HAVE) {
				print("h");
				print(pa);
				return;
			} else {
				print("m");
				tlb.storePA(sp, pgAddress);
			}
		}
		
		pa = pgAddress + offset;
		
		print(pa);
	}
	
	
	
	


	

	

	private void setSegment(int segment, int ptAddress) {
		if (isFrameOccupied(ptAddress)) {
				debug("sg frame already occupied, cannot set");
		} else {
			pm[segment] = ptAddress;
			setFramePT(ptAddress);
		}
	}

	private void setPage(int page, int segment, int pgAddress) {
			debug(pgAddress);
		if (isFrameOccupied(pgAddress)) {
				debug("pg frame already occupied, cannot set");
		} else {
			int ptAddress = getPTAddress(segment);
				debug(ptAddress);
			pm[ptAddress + page] = pgAddress;
			setFramePage(pgAddress);
		}
	}
	
	public boolean isFrameOccupied(int address) {
		if (address == -1) {
			return false;
		}
		int frame = getFrameNumber(address);
			debug("frame number = " + frame);
		return frames[frame];
	}
	
	public void setFramePage(int address) {
		int frame = getFrameNumber(address);
			debug("pg frame set = " + frame);
		frames[frame] = true;
	}
	
	public void setFramePT(int address) {
		int frame = getFrameNumber(address);
			debug("pt frame set = " + frame);
		frames[frame] = true;
		frames[frame+1] = true;
	}
	
	private int getFreePTFrame() {
		int freeFrame = -1;
		for (int i = 0; i < frames.length; i+=2) {
			if (frames[i] && frames[i+1]) {
				freeFrame = i;
			}
		}
		return freeFrame;
	}

	private int getFreePgFrame() {
		int freeFrame = -1;
		for (int i = 0; i < frames.length; i++) {
			if (frames[i]) {
				freeFrame = i;
			}
		}
		return freeFrame;
	}

	private int getFrameNumber(int address) {
		return address / 512;
	}
	
	private int getFrameAddress(int frame) {
		return frame * 512;
	}

	private int getPTAddress(int segmentNumber) {
		return pm[segmentNumber];
	}
	
	private int getPgAddress(int ptAddress, int pageNumber) {
		return pm[ptAddress + pageNumber];
	}
	
	private int getPA(int s, int p, int w) {
		return pm[pm[s]+p]+w;
	}
	
	
	
	
	
	
	
	

	public static void main(String[] args) {
		new VMSim().run(args);
	}
	
	public void debug(Object s) {
		System.err.println("DEBUG: " + s);
	}
	
	public void print(Object s) {
		System.out.print(s + " ");
	}
}
