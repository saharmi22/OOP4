package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OOPUnitCore {

    static void assertEquals(Object expected, Object actual){
        if (!expected.equals(actual)){
            throw new OOPAssertionFailure();
        }
    }

    static void fail(){
        throw new OOPAssertionFailure();
    }

    static OOPTestSummary runClass(Class<?> testClass){
        if (testClass == null || testClass.isAnnotationPresent(OOPTestClass.class)){
            throw new IllegalArgumentException();
        }
        Object test_object = null;
        try{
            Method con = testClass.getDeclaredMethod(testClass.getName());
            //Constructor<?> c = testClass.getConstructor();
            con.setAccessible(true);
            test_object = con.invoke(testClass);
        }
        catch(NoSuchMethodException ignored){} catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        for (Method m : testClass.getDeclaredMethods()){
            if (m.getAnnotation(OOPSetup.class)!= null){
                try {
                    m.invoke(test_object); ///todo: needs to invoke it to the fathers also?
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }
}
