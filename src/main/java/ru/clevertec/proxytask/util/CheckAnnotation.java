package ru.clevertec.proxytask.util;

import ru.clevertec.proxytask.exception.UnsupportedClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CheckAnnotation {
    public static List<Method> getAnnotatedMethod(Class clazz, Class annotationClass) {
        if (clazz.isPrimitive()) {
            throw new UnsupportedClass("Primitive can't be processed");
        }
        Method[] methods = clazz.getDeclaredMethods();
        List<Method> annotatedMethods = new ArrayList<>(Arrays.stream(methods).filter(it -> it.getAnnotation(annotationClass) != null).toList());
        Class parent = clazz.getSuperclass();
        if (parent != Object.class) {
            annotatedMethods.addAll(getAnnotatedMethod(parent, annotationClass));
        }
        return annotatedMethods;
    }

    public static Map<Method, Boolean> getAllAnnotatedMethod(Class clazz, Class annotationClass, Map<Method, Boolean> annotatedMethods) {
        if (clazz.isPrimitive()) {
            throw new UnsupportedClass("Primitive can't be processed");
        }
        Method[] methods = clazz.getDeclaredMethods();
        Arrays.stream(methods).filter(it->!( it.isSynthetic())).forEach(it -> annotatedMethods.put(it, it.getAnnotation(annotationClass) != null));
        Class parent = clazz.getSuperclass();
        if (parent != Object.class) {
            getAllAnnotatedMethod(parent, annotationClass, annotatedMethods);
        }
        return annotatedMethods;
    }


}
