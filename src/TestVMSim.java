import static org.junit.Assert.*;

import org.junit.Test;


public class TestVMSim {
	String[] testfiles = {"input1.txt", "input2.txt"};

	@Test
	public void testInit() {
		VMSim vm = new VMSim();
		vm.run(testfiles);
		
	}

}
