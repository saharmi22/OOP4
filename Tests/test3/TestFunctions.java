package OOP.Tests.test3;

import OOP.Solution.OOPTestSummary;
import OOP.Solution.OOPUnitCore;
import OOP.Provided.*;
import static OOP.Solution.OOPUnitCore.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by elran on 08/01/17.
 */
public class TestFunctions {
	public static class WrapperCloneable implements Cloneable{
		Integer a;
		int wasCloned = 0;
		WrapperCloneable(int a)
		{
			this.a = a;
		}
		@Override
		protected WrapperCloneable clone()
		{
			wasCloned = 1;
			return new WrapperCloneable(Integer.valueOf(a));
		}
	}
	public static class WrapperCopyCtor
	{
		Integer a;
		int wasCopied = 0;
		WrapperCopyCtor(int a)
		{
			this.a = a;
		}
		WrapperCopyCtor(WrapperCopyCtor another)
		{
				wasCopied = 1;
				this.a = Integer.valueOf(another.a);
		}
	}
	public static class WrapThis<E>
	{
		E a;
		WrapThis(E a)
		{
			this.a = a;
		}
	}
	public static class WrapperNoOther
	{
		Integer a;
		WrapperNoOther(int a)
		{
			this.a = a;
		}
	}
	public static class WrapperAllOptions implements Cloneable
	{
		Integer a;
		int whoUsed = 0;
		WrapperAllOptions(int a)
		{
			this.a = a;
		}
		@Override
		protected WrapperAllOptions clone()
		{
			whoUsed = 1;
			return new WrapperAllOptions(Integer.valueOf(a));
		}
		WrapperAllOptions(WrapperAllOptions another)
		{
			if(this != another)
			{
				whoUsed = 2;
				this.a = another.a;
			}
		}
	}
	public static class ExceptionDummy1 extends Exception{}
	public static class ExceptionDummy2 extends Exception{}
	public static void fail()
	{
		testEquals(0,1);
	}
	public static void success()
	{
		testEquals(0,0);
	}
	public static void testEquals(Object a,Object b) throws OOPAssertionFailure
	{
		OOPUnitCore.assertEquals(a,b);
	}
	public static int shouldPass(Object a,Object b)
	{
		try{
			testEquals(a,b);
			return 1;
		}catch (OOPAssertionFailure e)
		{
			testFailed(a.toString()+" is not "+b.toString());
		}
		return 0;
	}
	public static int shouldntPass(Object a,Object b)
	{
		boolean throwed = false;
		try{
			testEquals(a,b);
		}catch (OOPAssertionFailure e)
		{
			throwed = true;
		}
		if(throwed)
		{
			assert false;
			testFailed(a.toString()+" is not "+b.toString());
			return 0;
		}
		return 1;
	}
	public static void testFailed(String msg)
	{
		System.out.println(String.format("\u001B[31mTest failed in file: %s, line: %d, msg: %s\u001B[0m",new Throwable().getStackTrace()[2].getFileName(),new Throwable().getStackTrace()[2].getLineNumber(),msg));
	}
	public static void launchTest(Class<?> aClass,int successNum,int failNum,int errorNum)
	{
		System.out.println("Launching test on class: "+aClass.getName());
		OOPTestSummary result = OOPUnitCore.runClass(aClass);
		assertNotNull(result);
		assertEquals(successNum, result.getNumSuccesses());
		assertEquals(failNum, result.getNumFailures() + result.getNumExceptionMismatches());
		assertEquals(errorNum, result.getNumErrors());
		assertEquals(0, result.getNumExceptionMismatches());
		int successCount = 0;
		successCount+=shouldPass(successNum,result.getNumSuccesses());
		successCount+=shouldPass(failNum,result.getNumFailures());
		successCount+=shouldPass(errorNum,result.getNumErrors());
		if(successCount == 3)
		{
			System.out.println("\u001B[32mTest numbers matched on class: "+aClass.getName()+"\u001B[0m");
		}
		else
		{
			System.out.println("\u001B[31mTest numbers mismatch on class: "+aClass.getName()+"\u001B[0m");
		}
	}
}
