package OOP.Tests.test3;

import OOP.Solution.OOPAfter;
import OOP.Solution.OOPBefore;
import OOP.Solution.OOPTest;
import OOP.Solution.OOPTestClass;

/**
 * Created by elran on 08/01/17.
 */
@OOPTestClass(OOPTestClass.OOPTestClassType.ORDERED)
public class TestOrderedInher extends TestOrdered {

	@Override
	@OOPTest(order = 16)
	protected void test16() //we override
	{
		c=2;
	}

	@OOPTest(order = 18)
	private void test18()
	{
		TestFunctions.shouldPass(2,c);//make sure this ran insted of father's function
	}

	@OOPBefore({"test17"})
	private void SbeforeTest17_1(){
		TestFunctions.shouldPass(1,d);
		d++;
	}
	@OOPTest(order = 17)
	public void test17()
	{
		TestFunctions.shouldPass(2,d);
		d++;
	}
	@OOPAfter({"test17"})
	public void SafterTest17_1()
	{
		TestFunctions.shouldPass(3,d);
		d++;
	}
}
