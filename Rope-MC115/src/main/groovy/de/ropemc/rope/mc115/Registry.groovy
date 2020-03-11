package de.ropemc.rope.mc115

import de.ropemc.rope.loader.ReflectionHelper
import de.ropemc.rope.loader.mapping.Mapping

import java.lang.reflect.Method

enum Registry {

    BLOCK,
    ITEM
    void register(ResourceLocation id, Object block){
        Class writableRegistryClass = Class.forName(Mapping.getClassName('net.minecraft.core.WritableRegistry'))
        Class resourceLocationClass = Class.forName(Mapping.getClassName('net.minecraft.resources.ResourceLocation'))
        Method method = writableRegistryClass.getDeclaredMethod(Mapping.getMethodName('net.minecraft.core.WritableRegistry', 'register', ['net.minecraft.resources.ResourceLocation', 'java.lang.Object']), new Class[]{resourceLocationClass, Object.class})
        method.setAccessible(true)
        method.invoke(ReflectionHelper.getStaticFieldValue('net.minecraft.core.Registry', name()), id.internal, block)
    }

}
