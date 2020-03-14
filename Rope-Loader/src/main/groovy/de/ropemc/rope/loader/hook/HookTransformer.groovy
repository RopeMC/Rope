package de.ropemc.rope.loader.hook

import de.ropemc.rope.loader.Log
import de.ropemc.rope.loader.mapping.Mapping
import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtMethod
import org.codehaus.groovy.runtime.InvokerInvocationException

import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.IllegalClassFormatException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.security.ProtectionDomain
import java.util.function.Consumer

class HookTransformer implements ClassFileTransformer {
    private static List<MethodHook> methodHooks = []
    private static List<ConstructorHook> constructorHooks = []
    private static List<StaticInitializerHook> staticInitializerHooks = []
    byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace('/', '.')
        ClassPool cp = ClassPool.getDefault()
        cp.importPackage('de.ropemc.rope.loader.hook')
        CtClass ctClass = cp.get(className)
        boolean hasHook = false
        methodHooks.each {
            if(it.className == className){
                hasHook = true
                CtClass[] ctParams = new CtClass[it.paramTypes.size()]
                for(int i=0; i<ctParams.length; i++){
                    ctParams[i] = cp.get(it.paramTypes[i])
                }
                CtMethod oldMethod = ctClass.getDeclaredMethod(it.methodName, ctParams)
                oldMethod.setName('__'+it.methodName)
                CtMethod method = new CtMethod(oldMethod.returnType, it.methodName, oldMethod.parameterTypes, ctClass)
                method.modifiers = oldMethod.modifiers
                StringBuilder sb = new StringBuilder()
                for(int i=0; i<it.paramTypes.size(); i++){
                    sb.append('params[')
                    sb.append(i)
                    sb.append('] = $')
                    sb.append(i+1)
                    sb.append(';')
                }
                String body = """{
                Object[] params = new Object[${it.paramTypes.size()}];${sb.toString()}
                HookCall hookCall=new HookCall(\$0,params);
                HookTransformer.callMethodHook(${it.id},hookCall);
                """
                switch (method.returnType){
                    case CtClass.voidType:
                        break
                    case CtClass.booleanType:
                        body+='return ((java.lang.Boolean)hookCall.getReturnValue()).booleanValue();'
                        break
                    default:
                        body+='return ('+method.returnType.name+')hookCall.getReturnValue();'
                        break
                }
                body += '}'
                try {
                    method.setBody(body)
                }catch(Throwable t){
                    Log.error t.message
                }
                ctClass.addMethod(method)
            }
        }
        staticInitializerHooks.each {
            if(it.className == className){
                hasHook = true
                CtConstructor method = ctClass.getClassInitializer()
                try {
                    method.insertBefore("HookTransformer.callStaticInitializerBeforeHook(${it.id});")
                    method.insertAfter("HookTransformer.callStaticInitializerAfterHook(${it.id});")
                }catch(Throwable t){
                    Log.error t.message
                }
            }
        }
        constructorHooks.each {
            if(it.className == className){
                hasHook = true
                try {
                    CtClass[] ctParams = new CtClass[it.paramTypes.size()]
                    for(int i=0; i<ctParams.length; i++){
                        ctParams[i] = cp.get(it.paramTypes[i])
                    }
                    CtConstructor constructor = ctClass.getDeclaredConstructor(ctParams)
                    StringBuilder sb = new StringBuilder()
                    for(int i=0; i<it.paramTypes.size(); i++){
                        sb.append('params[')
                        sb.append(i)
                        sb.append('] = $')
                        sb.append(i+1)
                        sb.append(';')
                    }
                    constructor.insertAfter("""
                    Object[] params = new Object[${it.paramTypes.size()}];${sb.toString()}
                    HookCall hookCall=new HookCall(\$0,params);
                    HookTransformer.callConstructorAfterHook(${it.id},hookCall);
                    """)
                }catch(Throwable t){
                    Log.error t.message
                }
            }
        }
        if(hasHook)
            return ctClass.toBytecode()
        return classfileBuffer
    }
    static void before(String className, String methodName, List<String> paramTypes, Consumer<HookCall> handler){
        findMethodHook(className, methodName, paramTypes).before.add(handler)
    }
    static void after(String className, String methodName, List<String> paramTypes, Consumer<HookCall> handler){
        findMethodHook(className, methodName, paramTypes).after.add(handler)
    }
    static void afterConstructor(String className, List<String> paramTypes, Consumer<HookCall> handler){
        findConstructorHook(className, paramTypes).after.add(handler)
    }
    static void afterStaticInitializer(String className, Closure handler){
        findStaticInitializerHook(className).after.add(handler)
    }
    static void beforeStaticInitializer(String className, Closure handler){
        findStaticInitializerHook(className).before.add(handler)
    }
    private static StaticInitializerHook findStaticInitializerHook(String className){
        className = Mapping.getClassName(className)
        for(h in staticInitializerHooks){
            if(h.className == className){
                return h
            }
        }
        StaticInitializerHook hook = new StaticInitializerHook(className)
        staticInitializerHooks.add(hook)
        return hook
    }
    private static MethodHook findMethodHook(String className, String methodName, List<String> paramTypes){
        methodName = Mapping.getMethodName(className, methodName, paramTypes)
        className = Mapping.getClassName(className)
        List<String> realParamTypes = []
        for(type in paramTypes){
            int depth = type.count('[')
            realParamTypes.add(Mapping.getClassName(type.substring(0, type.length()-(2*depth)))+('[]'*depth))
        }
        for(hook in methodHooks){
            if(hook.className == className && hook.methodName == methodName && hook.paramTypes.size() == paramTypes.size()){
                boolean paramsEqual = true
                for(int i=0; i<paramTypes.size(); i++){
                    if(hook.paramTypes[i] != realParamTypes[i]){
                        paramsEqual = false
                        break
                    }
                }
                if(paramsEqual)
                    return hook
            }
        }
        MethodHook hook = new MethodHook(className, methodName, realParamTypes)
        methodHooks.add(hook)
        return hook
    }
    private static ConstructorHook findConstructorHook(String className, List<String> paramTypes){
        className = Mapping.getClassName(className)
        List<String> realParamTypes = []
        for(type in paramTypes){
            int depth = type.count('[')
            realParamTypes.add(Mapping.getClassName(type.substring(0, type.length()-(2*depth)))+('[]'*depth))
        }
        for(hook in constructorHooks){
            if(hook.className == className && hook.paramTypes.size() == paramTypes.size()){
                boolean paramsEqual = true
                for(int i=0; i<paramTypes.size(); i++){
                    if(hook.paramTypes[i] != realParamTypes[i]){
                        paramsEqual = false
                        break
                    }
                }
                if(paramsEqual)
                    return hook
            }
        }
        ConstructorHook hook = new ConstructorHook(className, realParamTypes)
        constructorHooks.add(hook)
        return hook
    }
    private static MethodHook getMethodHook(int id){
        for(hook in methodHooks){
            if(hook.id == id){
                return hook
            }
        }
        return null
    }
    private static ConstructorHook getConstructorHook(int id){
        for(hook in constructorHooks){
            if(hook.id == id){
                return hook
            }
        }
        return null
    }
    static void callMethodHook(int id, HookCall call){
        MethodHook hook = getMethodHook(id)
        for(handler in hook.before){
            handler.accept(call)
        }
        if(!call.isReturned()){
            Class<?> clazz = Class.forName(convertName(hook.className))
            Class[] paramTypes = new Class[hook.paramTypes.size()]
            for(int i=0; i<paramTypes.length; i++){
                paramTypes[i] = Class.forName(convertName(hook.paramTypes[i]))
            }
            Method method = clazz.getDeclaredMethod('__'+hook.methodName, paramTypes)
            if(!method.isAccessible())
                method.setAccessible(true)
            try {
                Log.info method.getName()
                call.returnValue = method.invoke(call.instance, call.params)
            }catch(InvocationTargetException ex){
                ex.targetException.printStackTrace()
            }
        }
        for(handler in hook.after){
            handler.accept(call)
        }
    }
    static void callStaticInitializerBeforeHook(int id){
        for(hook in staticInitializerHooks){
            if(hook.id == id){
                hook.before.each {
                    it()
                }
            }
        }
    }
    static void callStaticInitializerAfterHook(int id){
        for(hook in staticInitializerHooks){
            if(hook.id == id){
                hook.after.each {
                    it()
                }
            }
        }
    }
    static void callConstructorAfterHook(int id, HookCall call){
        for(hook in constructorHooks){
            if(hook.id == id){
                hook.after.each {
                    it.accept(call)
                }
            }
        }
    }
    static String convertName(String name){
        int depth = name.count('[')
        if(depth == 0)
            return name
        StringBuilder sb = new StringBuilder()
        depth.times {
            sb.append('[')
        }
        sb.append('L')
        sb.append(name.substring(0, name.length()-(depth*2)))
        sb.append(';')
        sb.toString()
    }
}
