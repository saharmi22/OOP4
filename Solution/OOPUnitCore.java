package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;
import org.junit.rules.ExpectedException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class OOPUnitCore {

    public static void assertEquals(Object expected, Object actual){
        if (expected == null){
            if (actual == null)
                return;
            else
                throw new OOPAssertionFailure(null, actual);
        }
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
        //OOPExpectedException expected = findExpectedExceptionVariable(testClass, test_object);

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
        Map<String, OOPResult> oopResultMap = runTests(testClass, test_object, Before_methods, After_methods, test_methods);


        return new OOPTestSummary(oopResultMap);
    }



    private static Map<String, OOPResult> runTests(Class<?> testClass, Object testObject,
                                                   ArrayList<Method> beforeMethods, ArrayList<Method> afterMethods,
                                                   ArrayList<Method> testMethods) {
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
                OOPResult result = new OOPResultImpl(before_success.getCause(), null);
                //OOPResult result = OOPResult.ERROR;
                oopResultMap.put(test.getName(), result);
                continue;
            }
            try {
                noneExpectedExceptionVariable(testClass, testObject);
                test.setAccessible(true);
                test.invoke(testObject); //run the test!
            } catch (Throwable t){
                e = t;
            }
            finally {
                OOPResult result;
                OOPExpectedException expected = findExpectedExceptionVariable(testClass, testObject);
                if (e != null)
                    result = new OOPResultImpl(e.getCause(), expected);
                else
                    result = new OOPResultImpl(null, expected);
                oopResultMap.put(test.getName(), result);
            }
            if (testObject != null)
                backupFields = backupObject(testObject);
            after_success = invokeMethodList_After(testObject, afterMethods, test);
            if (after_success != null && testObject != null) {
                restoreObject(testObject, backupFields);
                OOPResult err = new OOPResultImpl(new Exception(), null);
                oopResultMap.put(test.getName(), err);
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

    private static void noneExpectedExceptionVariable(Class<?> testClass, Object test_object) {
        Field expected = findExpectedExceptionField(testClass);
        if (expected!=null) {
            expected.setAccessible(true);
            try {
                expected.set(test_object, OOPExpectedExceptionImpl.none());
            } catch (IllegalAccessException ignored) {}
        }
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
                    beforeMethods.get(i).setAccessible(true);
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
                if (Arrays.asList(m.getAnnotation(OOPAfter.class).value()).contains(test.getName())) {
                    m.setAccessible(true);
                    m.invoke(testObject);
                }
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

    static class TestsComparator implements Comparator<Method> {

        // override the compare() method
        public int compare(Method t1, Method t2)
        {
            int order1 = t1.getAnnotation(OOPTest.class).order();
            int order2 = t2.getAnnotation(OOPTest.class).order();
            if (order1 == order2) {
                if (t1.getDeclaringClass().isAssignableFrom(t2.getDeclaringClass()))
                    return -1;
                return 1;
            }
            else if (order1 > order2)
                return 1;
            else
                return -1;
        }
    }
    public static ArrayList<Method> sortTestMethods (ArrayList<Method> testsArray){
        /*ArrayList<Method> sortedArray = new ArrayList<>();
        Boolean updated_in_round = false;
        for (int i = 1; i <= testsArray.size(); i++){
            for (Method m : testsArray){
                if (m.getAnnotation(OOPTest.class).order() == i){
                    sortedArray.add(m);
                    updated_in_round = true;
                    break;
                }
            }
            if (!updated_in_round)
                break;
            updated_in_round = false;
        }
        return sortedArray;*/

        testsArray.sort(new TestsComparator());
        return testsArray;
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
        Field[] fields = repairMe.getClass().getDeclaredFields();
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
                setupMethods.get(i).setAccessible(true);
                setupMethods.get(i).invoke(testObject);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }
    }

    private static Method methodListContains(ArrayList<Method> Methods, String name) {
        for (Method Method : Methods) {
            if (Method.getName().equals(name)) {
                return Method;
            }
        }
        return null;
    }

}
