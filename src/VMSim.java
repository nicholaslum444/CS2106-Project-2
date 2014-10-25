import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * 
 * 1024 frames in PM
 * 512 words per frame
 *
 */
public class VMSim {
	static final String outFile = "A0108358B1.txt";
	static final String outFileTLB = "A0108358B2.txt";
	
	static final int READ = 0;
	static final int WRITE = 1;
	
	static final int mask_W = 0b00000000000000000000000111111111;
	static final int mask_P = 0b00000000000001111111111000000000;
	static final int mask_S = 0b00001111111110000000000000000000;
	
	boolean useTLB = false;
	
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
			if (args[3].toLowerCase().equals("tlb")) {
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
		int word = va & mask_W;
			debug("w = " + word);
		
		int ptAddress = getPtAddress(segment);
		int pgAddress = getPgAddress(ptAddress, page);
		int value = pm[pgAddress + word];
			debug(value);
	}
	
	private void writeVA(int va) {
		
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

	private void setSegment(int segment, int ptAddress) {
		//TODO
		if (isFrameOccupied(ptAddress)) {
				debug("sg frame already occupied, cannot set");
		} else {
			pm[segment] = ptAddress;
			setFramePT(ptAddress);
		}
	}

	private void setPage(int page, int segment, int pgAddress) {
		//TODO
			debug(pgAddress);
		if (isFrameOccupied(pgAddress)) {
				debug("pg frame already occupied, cannot set");
		} else {
			int ptAddress = getPtAddress(segment);
				debug(ptAddress);
			pm[ptAddress + page] = pgAddress;
			setFramePage(pgAddress);
		}
	}
	
	public boolean isFrameOccupied(int address) {
		if (address == -1) {
			return false;
		}
		int frame = getFrame(address);
			debug("frame number = " + frame);
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
		return address / 512;
	}

	private int getPtAddress(int segmentNumber) {
		return pm[segmentNumber];
	}
	
	private int getPgAddress(int ptAddress, int pageNumber) {
		return pm[ptAddress + pageNumber];
	}
	
	
	
	
	
	
	
	

	/*public static void main(String[] args) {
		new VMSim().run(args);
	}*/
	
	public void debug(Object s) {
		System.err.println("DEBUG: " + s);
	}
	
	public void println(Object s) {
		System.out.println(s);
	}
}
