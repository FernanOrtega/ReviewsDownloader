package es.us.lsi.fogallego.reviewsdownloader.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UtilsJavaRefl {

    /** The Constant GET_INSTANCE_METHOD_NAME. */
    private static final String GET_INSTANCE_METHOD_NAME = "getInstance";

    /**
     * Creates the object.
     *
     * @param classname
     *            the classname
     * @return the object
     */
    public static Object createObject(final String classname) {
        Class<?> theClass;
        Object instance = null;
        try {
            theClass = Class.forName(classname);
            instance = theClass.newInstance();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found"+ e.getMessage());
        } catch (InstantiationException e) {
            System.err.println("Instantiation error"+ e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("Illegal access"+ e.getMessage());
        }

        return instance;
    }

    /**
     * Gets the single instance of UtilsJavaRefl.
     *
     * @param classname
     *            the classname
     * @param parameters
     *            the parameters
     * @return single instance of UtilsJavaRefl
     */
    public static Object getInstance(final String classname,
                                     final Object[] parameters) {
        Class<?> theClass;
        Object instance = null;
        Method getInstance;
        Class<?>[] argClass = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Object obj = parameters[i];
            argClass[i] = obj.getClass();
        }
        try {
            theClass = Class.forName(classname);
            getInstance = theClass.getDeclaredMethod(GET_INSTANCE_METHOD_NAME,
                    argClass);
            instance = getInstance.invoke(null, parameters);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found"+ e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("Illegal access"+ e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Illegal argument"+ e.getMessage());
        } catch (InvocationTargetException e) {
            System.err.println("Invocation target error"+ e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security error"+ e.getMessage());
        } catch (NoSuchMethodException e) {
            System.err.println("No such method"+ e.getMessage());
        }

        return instance;
    }

    public static String getCallerClassName(String className) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement elem = trace[2];
        if (className != null && !className.isEmpty()) {
            for (int i = 2; i < trace.length
                    && elem.getClassName().equals(className); i++) {
                elem = trace[i];
            }
        }

        return elem.getClassName();
    }

    public static String getPathFromClass(String callerClassName) {
        String path = null;

        try {
            path = Class.forName(callerClassName).getProtectionDomain().getCodeSource()
                    .getLocation().getPath();
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFound "+e.getMessage());
        }

        return path;
    }

}
