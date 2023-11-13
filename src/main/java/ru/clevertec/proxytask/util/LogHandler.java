package ru.clevertec.proxytask.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LogHandler implements InvocationHandler {
    private Object proxyObject;
    private List<Method> annotatedMethods;

    public LogHandler(Object proxyObject, List<Method> annotatedMethods) {
        this.proxyObject = proxyObject;
        this.annotatedMethods = annotatedMethods;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean isAnnotatedMethod = annotatedMethods.stream().anyMatch(m -> isMethodEquals(m, method));
        if (isAnnotatedMethod) {
            String argsString = Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", "));
            System.out.println("Invoked method " + method.getName() + " of class " + proxyObject.getClass().getName()
                    + " with arguments: " + argsString);
            Object result = method.invoke(proxyObject, args);
            System.out.println("Method finished with result: " + result);
            return result;
        } else {
            return method.invoke(proxyObject, args);
        }
    }

    private boolean isMethodEquals(Method m1, Method m2) {
        boolean equal = m1.getName().equals(m2.getName());
        if (equal) {
            Class[] p1 = m1.getParameterTypes();
            Class[] p2 = m1.getParameterTypes();
            if (p1.length == p2.length) {
                for (int i = 0; i < p1.length; i++) {
                    if (p1[i] != p2[i]) {
                        equal = false;
                        break;
                    }
                }
            } else {
                equal = false;
            }
        }
        return equal;
    }


}
