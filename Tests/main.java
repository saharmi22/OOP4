package OOP.Tests;

import OOP.Solution.OOPTestSummary;
import OOP.Solution.OOPUnitCore;

public class main {
    public static void main (String args[]){

        //OOPTestSummary sum = OOPUnitCore.runClass(ExampleTest.class);
        //System.out.println(sum.getNumSuccesses());

        ExampleTest testing = new ExampleTest();
        testing.testForExample();

    }
}
