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

    public static void assertEquals(Object expected, Object actual){
        if (!expected.equals(actual)){
            throw new OOPAssertionFailure(expected, actual);
        }
    }

    static public void fail(){
        throw new OOPAssertionFailure();
    }

    static public OOPTestSummary runClass(Class<?> testClass){
        return runClass(testClass, "");
    }
    static public OOPTestSummary runClass(Class<?> testClass, String tag){
        if (testClass == null || !testClass.isAnnotationPresent(OOPTestClass.class) || tag == null)
            throw new IllegalArgumentException();

        //A- create instance of test class
        Object test_object = createTestObject(testClass);

        //find ExpectedException variable
        OOPExpectedException expected = findExpectedExceptionVariable(testClass, test_object);

        //B- run setup functions
        ArrayList<Method> setup_methods = createMethodListByAnnotation(testClass, OOPSetup.class);
        invokeMethodList_setup(test_object, setup_methods);

        //create before, tests and after array functions
        ArrayList<Method> Before_methods = createMethodListByAnnotation(testClass, OOPBefore.class);
        ArrayList<Method> After_methods = createMethodListByAnnotation(testClass, OOPAfter.class);
        ArrayList<Method> test_methods = createMethodListByAnnotation(testClass, OOPTest.class);
        if (testClass.getAnnotation(OOPTestClass.class).value() == OOPTestClass.OOPTestClassType.ORDERED)
            test_methods = sortTestMethods(test_methods);
        test_methods = filterByTag(test_methods, tag);

        //C+D+E- run OOPBefore functions then test and then OOPAfter functions while checking results and creating a map
        Map<String, OOPResult> oopResultMap = runTests(testClass, test_object, expected, Before_methods, After_methods, test_methods);


        return new OOPTestSummary(oopResultMap);
    }



    private static Map<String, OOPResult> runTests(Class<?> testClass, Object testObject, OOPExpectedException expected,
                                                   ArrayList<Method> beforeMethods, ArrayList<Method> afterMethods, ArrayList<Method> testMethods) {
        Map<String, OOPResult> oopResultMap = new HashMap<>();
        HashMap<String, Object> backupFields = new HashMap<>();
        Exception before_success, after_success;

        for (Method test : testMethods){
            Throwable e = null;
            if (testObject != null)
                backupFields = backupObject(testObject);
            before_success = invokeMethodList_Before(testObject, beforeMethods, test);
            //if a before function sends an exception, we need to restore the object and continue to the next step
            if (before_success != null && testObject != null) {
                restoreObject(testObject, backupFields);
                OOPResult result = new OOPResultImpl(before_success, null);
                oopResultMap.put(test.getName(), result);
                continue;
            }
            try {
                test.invoke(testObject); //run the test!
            } catch (Throwable t){
                e = t;
            }
            finally {
                OOPResult result = new OOPResultImpl(e, expected);
                oopResultMap.put(test.getName(), result);
            }
            if (testObject != null)
                backupFields = backupObject(testObject);
            after_success = invokeMethodList_After(testObject, afterMethods, test);
            if (after_success != null && testObject != null) {
                restoreObject(testObject, backupFields);
            }
        }
        return oopResultMap;
    }

    private static OOPExpectedException findExpectedExceptionVariable(Class<?> testClass, Object test_object) {
        Field expectedExceptions = findExpectedExceptionField(testClass);
        OOPExpectedException expected = null;
        if (expectedExceptions != null) {
            expectedExceptions.setAccessible(true);
            try {
                expected = (OOPExpectedException) expectedExceptions.get(test_object);
            } catch (IllegalAccessException ignored) {}
        }
        return expected;
    }

    private static Object createTestObject(Class<?> testClass) {
        try{

            //Method con = testClass.getDeclaredMethod(testClass.getName());

            //return con.newInstance();
            return testClass.newInstance();
            //return con.invoke(testClass);
        }
        catch(IllegalAccessException e){
            try{
                Constructor<?> con = testClass.getConstructor(testClass);
                con.setAccessible(true);
                return testClass.newInstance();
            }
            catch (java.lang.NoSuchMethodException | java.lang.InstantiationException | java.lang.IllegalAccessException ignored){}

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
        return null;
    }

    private static ArrayList<Method> filterByTag(ArrayList<Method> testMethods, String tag) {
        ArrayList<Method> filtered = new ArrayList<>();
        for (Method m : testMethods){
            if (m.getAnnotation(OOPTest.class).tag().contains(tag))
                filtered.add(m);
        }
        return filtered;
    }

    private static Field findExpectedExceptionField(Class<?> testClass) {
        Field[] fields = testClass.getDeclaredFields();
        for (Field f : fields){
            if (f.getType() == OOPExpectedException.class){
                return f;
            }
        }
        return null;
    }

    private static  Exception invokeMethodList_Before(Object testObject, ArrayList<Method> beforeMethods, Method test) {
        for (int i= beforeMethods.size()-1; i>=0; i--){
            Method m = beforeMethods.get(i);
            try {
                //check if test name is in value parameter
                if (Arrays.asList(m.getAnnotation(OOPBefore.class).value()).contains(test.getName())){
                    beforeMethods.get(i).invoke(testObject);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                return e;
            }
        }
        return null;
    }

    private static Exception invokeMethodList_After(Object testObject, ArrayList<Method> afterMethods, Method test) {
        for (Method m : afterMethods) {

            try {
                //check if test name is in value parameter
                if (Arrays.asList(m.getAnnotation(OOPAfter.class).value()).contains(test.getName()))
                    m.invoke(testObject);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return e;
            }
        }
        return null;
    }

    private static ArrayList<Method> createMethodListByAnnotation(Class<?> testClass, Class<? extends Annotation> annotation){
        Class<?> temp = testClass;
        ArrayList<Method> annotationMethods = new ArrayList<>();

        do {
            Method[] declaredMethods = temp.getDeclaredMethods();
            for (Method m : declaredMethods) {
                if (m.isAnnotationPresent(annotation)){
                    //check if method already in list -> if it is we found it in a subclass - by java conformance don't need to invoke
                    Method dup = methodListContains(annotationMethods, m.getName());
                    if (dup == null){
                        annotationMethods.add(m);
                    }
                }
            }
            temp = temp.getSuperclass();
        }while (temp != null);
        return annotationMethods;
    }

    public static ArrayList<Method> sortTestMethods (ArrayList<Method> testsArray){
        ArrayList<Method> sortedArray = new ArrayList<>();
        for (int i = 1; i <= testsArray.size(); i++){
            for (Method m : testsArray){
                if (m.getAnnotation(OOPTest.class).order() == i){
                    sortedArray.add(m);
                    break;
                }

            }
        }
        return sortedArray;
    }

    private static HashMap<String, Object> backupObject (Object toBackup) {
        HashMap<String, Object> fieldsBackup = new HashMap<>();
        Field[] fields = toBackup.getClass().getDeclaredFields();
        Object value = null;
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                Method m_clone = (f.get(toBackup)).getClass().getDeclaredMethod("clone");
                m_clone.setAccessible(true);
                value = m_clone.invoke(f.get(toBackup));
            } catch (NoSuchMethodException| SecurityException e) {
                try{
                    //value = f.getClass().n;
                    Constructor<?>[] ctors = (f.get(toBackup)).getClass().getConstructors();
                    for (Constructor<?> ctor : ctors) {
                        if (ctor.getParameterTypes().length == 1 && ctor.getParameterTypes()[0] == f.getType()) {
                            value = ctor.newInstance(f.get(toBackup));
                            break;
                        }
                    }
                    if (value == null)
                        value = f.get(toBackup);
                } catch(Exception ignore){}
            } catch (Exception ignore) {}
            fieldsBackup.put(f.getName(), value);
            value = null;
        }
        return fieldsBackup;
    }

    private static void restoreObject (Object repairMe, HashMap<String, Object> storedValues){
        Field[] fields = repairMe.getClass().getFields();
        for (Field f : fields){
            try{
                f.setAccessible(true);
                f.set(repairMe, storedValues.get(f.getName()));
            }
            catch(Exception ignore){}
        }
        storedValues.clear();
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
