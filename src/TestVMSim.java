import static org.junit.Assert.*;

import org.junit.Test;


public class TestVMSim {
	String[] testfiles = {"input1.txt", "input2.txt"};
	String[] testfilesTLB = {"input1.txt", "input2.txt", "tlb"};

	@Test
	public void test() {
		VMSim vm = new VMSim();
		vm.run(testfiles);
	}
	
	@Test
	public void testTLB() {
		VMSim vm = new VMSim();
		vm.run(testfilesTLB);
	}

}
