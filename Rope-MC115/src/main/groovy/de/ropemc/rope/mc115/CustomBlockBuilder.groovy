package de.ropemc.rope.mc115

import de.ropemc.rope.loader.ReflectionHelper
import de.ropemc.rope.loader.mapping.Mapping

import java.lang.reflect.Method

class CustomBlockBuilder {

    Class clazz
    Object internal

    CustomBlockBuilder(Material material){
        clazz = Class.forName(Mapping.getClassName('net.minecraft.world.level.block.Block$Properties'))
        Class materialClass = Class.forName(Mapping.getClassName('net.minecraft.world.level.material.Material'))
        Method method = clazz.getMethod(Mapping.getMethodName('net.minecraft.world.level.block.Block$Properties', 'of', ['net.minecraft.world.level.material.Material']), materialClass)
        internal = method.invoke(null, ReflectionHelper.getStaticFieldValue('net.minecraft.world.level.material.Material', material.name()))
    }

    Object createBlock(ResourceLocation id){
        Object block = ReflectionHelper.construct('net.minecraft.world.level.block.Block', ['net.minecraft.world.level.block.Block$Properties'], [internal])
        Registry.BLOCK.register(id, block)
        return block
    }

}
