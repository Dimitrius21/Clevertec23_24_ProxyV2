package ru.clevertec.proxytask.util;

import ru.clevertec.proxytask.exception.ProxyCreationException;
import ru.clevertec.proxytask.util.CheckAnnotation;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;


public class ProxyFabric {

    private List<Method> methods;
    private Map<Method, Boolean> methodsInfo;
    private String className;

    private StringWriter generateProxy(Object obj) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        Class clazz = obj.getClass();
        className = "Proxy$" + clazz.getSimpleName();
        Class[] interfaces = clazz.getInterfaces();
        String interfacesString = "";
        if (interfaces.length > 0) {
            interfacesString = " implements " + Arrays.stream(interfaces).map(it -> it.getSimpleName()).collect(Collectors.joining(", "));
        }
        String interfacesList = Arrays.stream(interfaces).map(it -> "import " + it.getName() + ";").collect(Collectors.joining("\n "));

        pw.println(interfacesList);
        pw.println("import " + clazz.getName() + ";");
        pw.println("import java.util.List;");
        pw.println("import java.lang.reflect.Method;");
        pw.println("import java.lang.reflect.InvocationTargetException;");
        pw.println("public class " + className + interfacesString + " {");
        pw.println("private Object obj;");
        pw.println("private List<Method> methods;");
        pw.println("public " + className + " (Object obj, List<Method> methods) { this.obj = obj;\n this.methods = methods; }");

        for (int j = 0; j < methods.size(); j++) {
            Method method = methods.get(j);
            Class returnType = method.getReturnType();
            Parameter[] parameters = method.getParameters();
            Class[] exceptions = method.getExceptionTypes();
            String parameterString = Arrays.stream(parameters).map(it -> it.getType().getTypeName() + " " + it.getName())
                    .collect(Collectors.joining(", "));
            String exceptionString = exceptions.length > 0 ?
                    " throws " + Arrays.stream(exceptions).map(it -> it.getName()).collect(Collectors.joining(", "))
                    : "";
            String argsString = Arrays.stream(parameters).map(it -> it.getName())
                    .collect(Collectors.joining(", "));
            String methodBegin = Modifier.toString(method.getModifiers()) + " " + returnType.getName() + " " +
                    method.getName() + " (" + parameterString + " ) " + exceptionString + " {\n Object res = null;\n " +
                    "try{ ";
            String methodMain;
            if (methodsInfo.get(method)) {
                methodMain = beforeMethod(method, clazz) + "res = methods.get(" + j + ").invoke(obj, " + argsString + ");\n" +
                        afterMethod(method, clazz) +
                        "return (" + returnType.getName() + ") res;";
            } else {
                methodMain = "return (" + returnType.getName() + ") methods.get(" + j + ").invoke(obj, " + argsString + ");";
            }
            String methodEnd = "} catch (IllegalAccessException | InvocationTargetException e) { throw new RuntimeException(e); } \n}";
            pw.println(methodBegin);
            pw.println(methodMain);
            pw.println(methodEnd);
        }
        pw.println("}");
        pw.close();
        //System.out.println(writer);
        return writer;
    }

    public Object proxyCreator(Object obj, Class annotation) {
        methodsInfo = new HashMap<>();
        CheckAnnotation.getAllAnnotatedMethod(obj.getClass(), annotation, methodsInfo);
        methods = new ArrayList<>(methodsInfo.keySet());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StringWriter writer = generateProxy(obj);

        JavaFileObject file = new JavaSourceFromString(className, writer.toString());

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, compilationUnits);

        boolean success = task.call();
        if (success) {
            try {
                MyClassLoader loader = new MyClassLoader();
                Class my = loader.getClassFromFile(new File(className + ".class"));
                return my.getConstructor(Object.class, List.class).newInstance(obj, methods);
            } catch (ProxyCreationException e) {
                throw new ProxyCreationException(e);
            } catch (Exception e) {
                throw new ProxyCreationException("Error of proxy creation", e);
            }
        } else {
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                System.err.println(diagnostic.getCode());
                System.err.println(diagnostic.getKind());
                System.err.println(diagnostic.getPosition());
                System.err.println(diagnostic.getStartPosition());
                System.err.println(diagnostic.getEndPosition());
                System.err.println(diagnostic.getSource());
                System.err.println(diagnostic.getMessage(null));
            }
            throw new ProxyCreationException("Incorrect Proxy file");
        }
    }

    class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    static class MyClassLoader extends ClassLoader {

        public Class getClassFromFile(File f) {
            byte[] raw = new byte[(int) f.length()];
            InputStream in = null;
            try {
                in = new FileInputStream(f);
                in.read(raw);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                throw new ProxyCreationException("Can't read proxy file", e);
            }
            return defineClass(null, raw, 0, raw.length);
        }
    }

    private String beforeMethod(Method method, Class clazz) {
        Parameter[] parameters = method.getParameters();
        String argsString = Arrays.stream(parameters).map(it -> it.getName())
                .collect(Collectors.joining(" + \", \" + "));
        return "System.out.println(\"Invoked method: " + method.getName() + " of class: " + clazz.getName()
                + " with arguments: \" +" + argsString + ");\n";
    }

    private String afterMethod(Method method, Class clazz) {
        return "System.out.println(\"Method finished with result: \" + res);\n";
    }

}
