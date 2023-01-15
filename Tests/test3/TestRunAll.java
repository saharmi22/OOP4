package OOP.Tests.test3;

import org.junit.Test;

/**
 * Created by elran on 08/01/17.
 */
public class TestRunAll {
	@Test
	public void test() {
		//TestFunctions.launchTest(TestOrdered.class,11,2,3);
		TestFunctions.launchTest(TestOrderedInher.class,13,2,3);
		TestFunctions.launchTest(UnorderedTest.class,2,0,1);
		TestFunctions.launchTest(UnorderedInheriting.class,4,1,2);
	}
}
