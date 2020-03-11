package de.ropemc.rope.loader

import de.ropemc.rope.loader.mapping.Mapping

import java.lang.reflect.Constructor
import java.lang.reflect.Field

class ReflectionHelper {

    static Object getStaticFieldValue(String className, String fieldName){
        Class clazz = Class.forName(Mapping.getClassName(className))
        Field field = clazz.getDeclaredField(Mapping.getFieldName(className, fieldName))
        if(!field.accessible)
            field.accessible = true
        return field.get(null)
    }

    static Object construct(String className, List<String> types, List<Object> values){
        Class clazz = Class.forName(Mapping.getClassName(className))
        Class[] typeClasses = new Class[types.size()]
        for(int i=0; i<typeClasses.length; i++){
            typeClasses[i] = Class.forName(Mapping.getClassName(types.get(i)))
        }
        Constructor constructor = clazz.getDeclaredConstructor(typeClasses)
        return constructor.newInstance(values.toArray())
    }

}
