package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;

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
        return runClass(testClass, "");
    }
    static OOPTestSummary runClass(Class<?> testClass, String tag){
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
        if (testClass.getAnnotation(OOPTestClass.class).value() == OOPTestClass.OOPTestClassType.ORDERED)
            test_methods = sortTestMethods(test_methods);
        test_methods = filterByTag(test_methods, tag);
        Map<String, OOPResult> oopResultMap = new HashMap<>();
        HashMap<String, Object> backupFields = new HashMap<>();
        int before = 0, after = 0;
        for (Method test : test_methods){
            Throwable e = null;

            if (test_object != null)
                backupFields = backupObject(test_object);
            before = invokeMethodList_Before(test_object, Before_methods, test);
            if (before == 0 && test_object != null) {
                restoreObject(test_object, backupFields);
            }


            try {
                test.invoke(test_object);
            } catch (Throwable t){
                e = t;
            }
            finally {
                OOPResult result = new OOPResultImpl(e, expected);
                oopResultMap.put(test.getName(), result);
            }

            if (test_object != null)
                backupFields = backupObject(test_object);
            after = invokeMethodList_After(test_object, After_methods, test);
            if (after == 0 && test_object != null) {
                restoreObject(test_object, backupFields);
            }
        }
        return new OOPTestSummary(oopResultMap);
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
        Field[] fields = testClass.getFields();
        for (Field f : fields){
            if (f.getType() == OOPExpectedExceptionImpl.class){
                return f;
            }
        }
        return null;
    }

    private static int invokeMethodList_Before(Object testObject, ArrayList<Method> beforeMethods, Method test) {
        for (int i= beforeMethods.size()-1; i>=0; i--){
            Method m = beforeMethods.get(i);
            try {
                //check if test name is in value parameter
                if (Arrays.asList(m.getAnnotation(OOPBefore.class).value()).contains(test.getName())){
                    beforeMethods.get(i).invoke(testObject);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                return 0;
            }
        }
        return 1;
    }

    private static int invokeMethodList_After(Object testObject, ArrayList<Method> afterMethods, Method test) {
        for (Method m : afterMethods) {

            try {
                //check if test name is in value parameter
                if (Arrays.asList(m.getAnnotation(OOPAfter.class).value()).contains(test.getName()))
                    m.invoke(testObject);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return 0;
            }
        }
        return 1;
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

    public static ArrayList<Method> sortTestMethods (ArrayList<Method> testsArray){
        ArrayList<Method> sortedArray = new ArrayList<>();
        for (int i = 1; i <= testsArray.size(); i++){
            for (Method m : testsArray){
                if (m.getAnnotation(OOPTest.class).order() == i)
                    sortedArray.add(m);
            }
        }
        return sortedArray;
    }

    private static HashMap<String, Object> backupObject (Object toBackup) {
        HashMap<String, Object> fieldsBackup = new HashMap<>();
        Field[] fields = toBackup.getClass().getFields();
        Object value = null;
        for (Field f : fields) {
            try {
                value = (f.get(toBackup)).clone();//todo
            } catch (CloneNotSupportedException e) {
                Method[] methods = f.getClass().getMethods();
                for (Method mtd : methods) {
                    if (mtd.getName().equals(f.getClass().getName()) && mtd.getParameterTypes().length == 1
                            && mtd.getParameterTypes()[0] == f.getClass()) {
                        try {
                            value = mtd.invoke(f);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                if (value == null) {
                    try {
                        value = f.get(toBackup);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            fieldsBackup.put(f.getName(), value);
        }
        return fieldsBackup;
    }

    private static void restoreObject (Object repairMe, HashMap<String, Object> storedValues){
        Field[] fields = repairMe.getClass().getFields();
        for (Field f : fields){
            try{
                f.set(repairMe, storedValues.get(f.getName()));
            }
            catch(Exception ex){
                throw new RuntimeException(ex);
            }
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
