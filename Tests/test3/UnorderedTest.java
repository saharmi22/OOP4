package OOP.Tests.test3;

import OOP.Solution.*;


/**
 * Created by elran on 05/01/17.
 */
@OOPTestClass(OOPTestClass.OOPTestClassType.UNORDERED)
public class UnorderedTest {
	protected int a = 0;
	private int b = 0;
	protected int c = 0;
	protected int testSetup = 0;

	@OOPSetup()
	private void setupFather(){
		TestFunctions.shouldPass(0,testSetup);
		testSetup++;
	}
	@OOPBefore({"overRideThis"})
	private void beforeFather(){
		TestFunctions.shouldPass(0,a);
		a++;
	}
	@OOPTest()
	protected void overRideThis(){
		a=2;
	}
	@OOPAfter("overRideThis")
	private void afterFather(){
		if(!this.getClass().equals(UnorderedTest.class))
			TestFunctions.shouldPass(4,a);
		else
			TestFunctions.shouldPass(2,a);
	}

	@OOPTest()
	private void testNotOverRided()
	{
		TestFunctions.shouldPass(0,b);
	}
	@OOPBefore({"testAfterBeforeOverRide"})
	protected void beforeFather2(){
		TestFunctions.fail();
	}
	@OOPTest()
	protected void testAfterBeforeOverRide()
	{
		TestFunctions.shouldPass(1,c);
		c++;
	}
	@OOPAfter({"testAfterBeforeOverRide"})
	protected void afterFather2(){
		TestFunctions.fail();
	}
}
