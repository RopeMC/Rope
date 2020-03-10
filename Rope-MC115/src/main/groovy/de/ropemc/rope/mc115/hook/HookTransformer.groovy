package de.ropemc.rope.mc115.hook

import de.ropemc.rope.mc115.Log
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod

import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.IllegalClassFormatException
import java.lang.reflect.Method
import java.security.ProtectionDomain

class HookTransformer implements ClassFileTransformer {
    private static List<Hook> hooks = []
    byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace('/', '.')
        ClassPool cp = ClassPool.getDefault()
        cp.importPackage('de.ropemc.rope.mc115.hook')
        CtClass ctClass = cp.get(className)
        boolean hasHook = false
        hooks.each {
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
                HookTransformer.callHook(${it.id},hookCall);
                ${method.returnType!=CtClass.voidType?'return ('+method.returnType.name+')hookCall.getReturnValue();':''}
                }"""
                try {
                    method.setBody(body)
                }catch(Throwable t){
                    Log.error t.message
                }
                ctClass.addMethod(method)
            }
        }
        if(hasHook)
            return ctClass.toBytecode()
        return classfileBuffer
    }
    static void before(String className, String methodName, List<String> paramTypes, Closure handler){
        findHook(className, methodName, paramTypes).before.add(handler)
    }
    static void after(String className, String methodName, List<String> paramTypes, Closure handler){
        findHook(className, methodName, paramTypes).after.add(handler)
    }
    private static Hook findHook(String className, String methodName, List<String> paramTypes){
        for(hook in hooks){
            if(hook.className == className && hook.methodName == methodName && hook.paramTypes.size() == paramTypes.size()){
                boolean paramsEqual = true
                for(int i=0; i<paramTypes.size(); i++){
                    if(hook.paramTypes[i] != paramTypes[i]){
                        paramsEqual = false
                        break
                    }
                }
                if(paramsEqual)
                    return hook
            }
        }
        Hook hook = new Hook(className, methodName, paramTypes)
        hooks.add(hook)
        return hook
    }
    private static Hook getHook(int id){
        for(hook in hooks){
            if(hook.id == id){
                return hook
            }
        }
        return null
    }
    static void callHook(int id, HookCall call){
        Hook hook = getHook(id)
        for(handler in hook.before){
            handler(call)
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
            call.returnValue = method.invoke(call.instance, call.params)
        }
        for(handler in hook.after){
            handler(call)
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
