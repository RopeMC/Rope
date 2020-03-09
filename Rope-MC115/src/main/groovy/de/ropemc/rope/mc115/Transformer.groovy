package de.ropemc.rope.mc115

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod

import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.IllegalClassFormatException
import java.security.ProtectionDomain

class Transformer implements ClassFileTransformer {
    private Map<String, Map<String, List<Map<String, Object>>>> data = [
            before: [:],
            insert: [:],
            after: [:]
    ]
    byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace('/', '.')
        ClassPool cp = ClassPool.getDefault()
        CtClass ctClass = cp.get(className)
        boolean modified = false
        if(data.insert.containsKey(className)){
            modified = true
            data.insert[className].each { transform ->
                CtMethod ctMethod = resolveMethod(cp, ctClass, transform)
                ctMethod.insertAt(transform.line as int, transform.modify as boolean, transform.code as String)
            }
        }
        if(data.before.containsKey(className)){
            modified = true
            data.before[className].each { transform ->
                CtMethod ctMethod = resolveMethod(cp, ctClass, transform)
                ctMethod.insertBefore(transform.code as String)
            }
        }
        if(data.after.containsKey(className)){
            modified = true
            data.after[className].each { transform ->
                CtMethod ctMethod = resolveMethod(cp, ctClass, transform)
                ctMethod.insertBefore(transform.code as String)
            }
        }
        if(modified)
            return ctClass.toBytecode()
        classfileBuffer
    }
    private CtMethod resolveMethod(ClassPool cp, CtClass ctClass, Map<String, Object> transform){
        CtClass[] ctParams = new CtClass[transform.params.size()]
        for(int i=0; i<ctParams.length; i++){
            ctParams[i] = cp.get(transform.params as List<String>[i])
        }
        return ctClass.getDeclaredMethod(transform.method as String, ctParams)
    }
    void before(String className, String methodName, List<String> params, String code){
        add('before', className, [
                method: methodName,
                params: params,
                code: code
        ])
    }
    void after(String className, String methodName, List<String> params, String code){
        add('after', className, [
                method: methodName,
                params: params,
                code: code
        ])
    }
    void insert(String className, String methodName, List<String> params, int line, String code){
        add('insert', className, [
                method: methodName,
                params: params,
                line: line,
                code: code,
                modify: false
        ])
    }
    void replace(String className, String methodName, List<String> params, int line, String code){
        add('insert', className, [
                method: methodName,
                params: params,
                line: line,
                code: code,
                modify: true
        ])
    }
    private void add(String type, String className, Map<String, Object> transform){
        List<Map<String, Object>> transforms = data[type].getOrDefault(className, [])
        transforms.add(transform)
        data[type][className] = transforms
    }
}
