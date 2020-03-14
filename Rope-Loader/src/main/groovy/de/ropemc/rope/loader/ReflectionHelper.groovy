package de.ropemc.rope.loader

import de.ropemc.rope.loader.mapping.Mapping

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

class ReflectionHelper {

    static Object getStaticFieldValue(String className, String fieldName){
        return getFieldValue(className, fieldName, null)
    }

    static Object getFieldValue(String className, String fieldName, Object instance){
        Class clazz = Class.forName(Mapping.getClassName(className))
        Field field = clazz.getDeclaredField(Mapping.getFieldName(className, fieldName))
        if(!field.accessible)
            field.accessible = true
        return field.get(instance)
    }

    static Object callStaticMethod(String className, String methodName, List<String> types, List<Object> values){
        return callMethod(className, methodName, types, null, values)
    }

    static Object callMethod(String className, String methodName, List<String> types, Object instance, List<Object> values){
        Class clazz = Class.forName(Mapping.getClassName(className))
        Class[] typeClasses = new Class[types.size()]
        for(int i=0; i<typeClasses.length; i++){
            typeClasses[i] = Class.forName(Mapping.getClassName(types.get(i)))
        }
        Method method = clazz.getDeclaredMethod(Mapping.getMethodName(className, methodName, types), typeClasses)
        if(!method.accessible)
            method.accessible = true
        return method.invoke(instance, values.toArray())
    }

    static Object construct(String className, List<String> types, List<Object> values){
        Class clazz = Class.forName(Mapping.getClassName(className))
        Class[] typeClasses = new Class[types.size()]
        for(int i=0; i<typeClasses.length; i++){
            typeClasses[i] = Class.forName(Mapping.getClassName(types.get(i)))
        }
        Constructor constructor = clazz.getDeclaredConstructor(typeClasses)
        if(!constructor.accessible)
            constructor.accessible = true
        return constructor.newInstance(values.toArray())
    }

}
