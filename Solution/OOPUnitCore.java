package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;
import org.junit.rules.ExpectedException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        //A- create instance of test class
        Object test_object = null;
        try{
            Method con = testClass.getDeclaredMethod(testClass.getName());
            con.setAccessible(true);
            test_object = con.invoke(testClass);
        }
        catch(NoSuchMethodException | IllegalAccessException ignored){} catch (Exception e) {
            throw new RuntimeException(e);
        }
        //check ExpectedException variable
        Field expectedExceptions = findExpectedExceptionField(testClass);
        OOPExpectedException expected = null;
        if (expectedExceptions == null) {
            expectedExceptions.setAccessible(true);
            try {
                expected = (OOPExpectedException) expectedExceptions.get(test_object);
            } catch (IllegalAccessException ignored) {}
        }
        //B- run setup functions
        ArrayList<Method> setup_methods = createMethodListByAnnotation(testClass, OOPSetup.class);
        invokeMethodList_setup(test_object, setup_methods);
        //C+D+E- run OOPBefore functions then test and then OOPAfter functions while checking results and creating a map
        ArrayList<Method> Before_methods = createMethodListByAnnotation(testClass, OOPBefore.class);
        ArrayList<Method> After_methods = createMethodListByAnnotation(testClass, OOPAfter.class);
        ArrayList<Method> test_methods = createMethodListByAnnotation(testClass, OOPTest.class);
        Map<String, OOPResult> oopResultMap = new HashMap<>();
        for (Method test : test_methods){
            Throwable e = null;
            invokeMethodList_Before(test_object, Before_methods, test);
            try {
                test.invoke(test_object);
            } catch (Throwable t){
                e = t;
            }
            finally {
                OOPResult result = new OOPResultImpl(e, expected);
                oopResultMap.put(test.getName(), result);
            }
            invokeMethodList_After(test_object, After_methods, test);
        }
        return new OOPTestSummary(oopResultMap);
    }

    private static Field findExpectedExceptionField(Class<?> testClass) {
        Field[] fields = testClass.getFields();
        for (Field f : fields){
            if (f.getType() == OOPExpectedExceptionImpl.class){
                return f;
            }
        }
        return null;
    }

    private static void invokeMethodList_Before(Object testObject, ArrayList<Method> beforeMethods, Method test) {
        for (int i= beforeMethods.size()-1; i>=0; i--){
            Method m = beforeMethods.get(i);
            try {
                //check if test name is in value parameter
                if (Arrays.asList(m.getAnnotation(OOPBefore.class).value()).contains(test.getName()))
                    beforeMethods.get(i).invoke(testObject);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }
    }

    private static void invokeMethodList_After(Object testObject, ArrayList<Method> afterMethods, Method test) {
        for (Method m : afterMethods) {
            try {
                //check if test name is in value parameter
                if (Arrays.asList(m.getAnnotation(OOPAfter.class).value()).contains(test.getName()))
                    m.invoke(testObject);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }
    }

    private static ArrayList<Method> createMethodListByAnnotation(Class<?> testClass, Class<? extends Annotation> annotation){
        Class<?> temp = testClass;
        ArrayList<Method> annotationMethods = new ArrayList<>();
        do {
            for (Method m : testClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(annotation)){
                    //check if method already in list -> if it is we found it in a subclass - by java conformance don't need to invoke
                    Method dup = methodListContains(annotationMethods, m.getName());
                    if (dup == null){
                        annotationMethods.add(m);
                    }
                    //setup Annotation is only present in a single function
                    break;
                }
            }
            temp = temp.getSuperclass();
        }while (temp != null);
        return annotationMethods;
    }

    private static void invokeMethodList_setup(Object testObject, ArrayList<Method> setupMethods) {
        for (int i= setupMethods.size()-1; i>=0; i--){
            try {
                setupMethods.get(i).invoke(testObject);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }
    }

    private static Method methodListContains(ArrayList<Method> setupMethods, String name) {
        for (Method setupMethod : setupMethods) {
            if (setupMethod.getName().equals(name)) {
                return setupMethod;
            }
        }
        return null;
    }

}
