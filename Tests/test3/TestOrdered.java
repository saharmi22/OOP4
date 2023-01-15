package OOP.Tests.test3;

import OOP.Solution.*;
import OOP.Tests.test3.TestFunctions.*;
import OOP.Provided.*;

/**
 * Created by elran on 08/01/17.
 */
@OOPTestClass(OOPTestClass.OOPTestClassType.ORDERED)
public class TestOrdered {
	private int a = 0;
	public int b = 0;
	protected int c = 0;
	public int d = 0;
	protected int testBackup = 0;
	private int testMulti = 0;
	private boolean[] ran = new boolean[6];

	WrapperAllOptions allOptionsA = new WrapperAllOptions(1),allOptionsB = null;
	WrapperCloneable cloneableA = new WrapperCloneable(2),cloneableB = null;
	WrapperCopyCtor copyCtorA = new WrapperCopyCtor(3),copyCtorB = null;
	WrapperNoOther noOtherA = new WrapperNoOther(4),noOtherB = null;

	WrapThis<WrapperAllOptions> allOptionsT = new WrapThis<>(null);
	WrapThis<WrapperCloneable> cloneableT = new WrapThis<>(null);
	WrapThis<WrapperCopyCtor> copyCtorT = new WrapThis<>(null);
	WrapThis<WrapperNoOther> noOtherT = new WrapThis<>(null);

	@OOPExceptionRule
	private OOPExpectedException expected = OOPExpectedExceptionImpl.none();
	private String field = "";

	@OOPTest(order = 4)
	public void test4()
	{
		TestFunctions.shouldPass(4,a); /// check if nothing else ran between test4 and test3
		a++;
	}
	@OOPTest(order = 2)
	private void test2()
	{
		TestFunctions.shouldPass(1,a); // check if setup runs only once
		a++;
		TestFunctions.fail(); //throws exception
	}
	@OOPTest(order = 1)
	private void test1()
	{
		TestFunctions.shouldPass(1,a); // check if setup runs first
		TestFunctions.success();
	}
	@OOPBefore({"test3"})
	protected void beforetest3_1()
	{
		TestFunctions.shouldPass(2,a); // check if test1 and test2 ran first
		a++;
	}
	@OOPTest(order = 3)
	protected void test3()
	{
		TestFunctions.shouldPass(3,a); // check if before ran
		a++;
	}
	@OOPSetup
	private void setup()
	{
		TestFunctions.shouldPass(0,a); // check if nothing ran yet
		a++;

		for (int i = 0; i < ran.length; i++) {
			ran[i] = false;
		}
	}
	@OOPAfter({"test4"})
	public void aftertest4_1()
	{
		TestFunctions.shouldPass(5,a); // check if test4 ran
		a++;
	}
	@OOPTest(order = 5)
	public void test5()
	{
		TestFunctions.shouldPass(6,a); // check if after was ran
		TestFunctions.shouldPass(1,b);
		b++;
	}
	@OOPBefore({"test5","test6"})
	private void beforeTests5and6_1()
	{
		b++;
	}
	@OOPAfter({"test5","test6"})
	private void afterTests5and6_1()
	{
		b++;
	}
	@OOPTest(order = 7)
	public void test7()
	{
		TestFunctions.shouldPass(6,b);
	}
	@OOPTest(order = 6)
	public void test6()
	{
		TestFunctions.shouldPass(4,b);
		b++;
	}

	@OOPTest(order = 8)
	public void test8() throws Exception  // this should be a success test
	{
		expected = OOPExpectedExceptionImpl.none();
		expected.expect(ExceptionDummy1.class);
		throw new ExceptionDummy1();
	}
	@OOPTest(order = 9)
	public void test9() throws Exception // we expect exception dummy1 but get AssertionFail therefore should fail
	{
		expected = OOPExpectedExceptionImpl.none();
		expected.expect(ExceptionDummy1.class);
		TestFunctions.fail();
	}
	@OOPTest(order = 10)
	public void test10() throws Exception// we expect an exception but get another therefore should Mismatch
	{
		expected = OOPExpectedExceptionImpl.none();
		expected.expect(ExceptionDummy1.class);
		//this is for the next tests
		noOtherT.a = noOtherA;
		cloneableT.a = cloneableA;
		copyCtorT.a = copyCtorA;
		allOptionsT.a = allOptionsA;
		throw new ExceptionDummy2();
	}

	@OOPBefore({"test11"})
	public void beforeTest11_1() throws ExceptionDummy1
	{
		TestFunctions.shouldPass(0,testBackup);
		testBackup++;
		throw new ExceptionDummy1();
	}
	@OOPTest(order = 11)
	public void test11()
	{
		TestFunctions.fail(); // this test shouldnt run
	}
	@OOPTest(order = 12)
	public void test12()
	{
		TestFunctions.shouldPass(0,testBackup); // make sure the backup worked
		testBackup++;
	}
	@OOPAfter({"test12"})
	public void aftertest12_1() throws ExceptionDummy1{
		TestFunctions.shouldPass(1,testBackup); // make sure the backup worked
		testBackup++;
		throw new ExceptionDummy1();
	}
	@OOPTest(order = 13)
	public void test13()
	{
		if(this.getClass().equals(TestOrdered.class))
		{ //relevant only for father
			TestFunctions.shouldPass(1,testBackup); // make sure the backup was right after the after test failed
			TestFunctions.shouldPass(6,a);
			TestFunctions.shouldPass(6,b); // make sure other fields didnt change
			TestFunctions.shouldPass(null,noOtherB);
			if(noOtherT.a != noOtherA)
				TestFunctions.shouldPass(0,1);//pointers should be the same
			TestFunctions.shouldPass(1,cloneableT.a.wasCloned); // make sure clone was called
			TestFunctions.shouldPass(null,cloneableB);
			if(cloneableT.a == cloneableA)
				TestFunctions.shouldPass(0,1);//pointers shouldn't be the same
			TestFunctions.shouldPass(1,copyCtorA.wasCopied);// make sure copy ctor was called
			TestFunctions.shouldPass(null,copyCtorB);
			if(copyCtorT.a == copyCtorA)
				TestFunctions.shouldPass(0,1);//pointers shouldn't be the same
			TestFunctions.shouldPass(1,allOptionsT.a.whoUsed); // cloneable has priority
			TestFunctions.shouldPass(null,allOptionsB);
			if(allOptionsT.a == allOptionsA)
				TestFunctions.shouldPass(0,1);//pointers shouldn't be the same
		}
		else {
			//if son all my fields should be shallow?
			if(noOtherT.a != noOtherA)
				TestFunctions.shouldPass(0,1);//pointers should be the same
			if(cloneableT.a != cloneableA)
				TestFunctions.shouldPass(0,1);//pointers should be the same
			if(copyCtorT.a != copyCtorA)
				TestFunctions.shouldPass(0,1);//pointers should be the same
			if(allOptionsT.a != allOptionsA)
				TestFunctions.shouldPass(0,1);//pointers should be the same
		}

	}
	@OOPBefore({"test14"})
	public void beforeTest14_1()
	{
		ran[0] = true;
	}
	@OOPBefore({"test14"})
	public void beforeTest14_2()
	{
		ran[1] = true;
	}
	@OOPBefore({"test14"})
	public void beforeTest14_3()
	{
		ran[2] = true;
	}
	@OOPTest(order = 14)
	public void test14()
	{
		for (int i = 0; i < 3; i++) {
			TestFunctions.shouldPass(true,ran[i]); // test if multiple befores ran
		}
	}
	@OOPAfter({"test14"})
	public void afterTest14_1()
	{
		ran[3] = true;
	}
	@OOPAfter({"test14"})
	public void afterTest14_2()
	{
		ran[4] = true;
	}
	@OOPAfter({"test14"})
	public void afterTest14_3()
	{
		ran[5] = true;
	}

	@OOPTest(order = 15)
	public void test15()
	{
		for (int i = 0; i < 6; i++) {
			TestFunctions.shouldPass(true,ran[i]); // test if multiple befores and after ran
		}
	}

	@OOPTest(order = 16)
	protected void test16()
	{
		c=1;
	}
	@OOPBefore({"test17"})
	private void FbeforeTest17_1(){
		TestFunctions.shouldPass(0,d);
		d++;
	}
	@OOPAfter({"test17"})
	public void FafterTest17_1()
	{
		TestFunctions.shouldPass(4,d);
		d++;
	}
}
