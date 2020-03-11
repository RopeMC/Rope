package de.ropemc.rope.mc115

import de.ropemc.rope.loader.ReflectionHelper
import de.ropemc.rope.loader.mapping.Mapping

import java.lang.reflect.Method

class CustomItemBuilder {

    Class clazz
    Object internal

    CustomItemBuilder(){
        clazz = Class.forName(Mapping.getClassName('net.minecraft.world.item.Item$Properties'))
        internal = clazz.newInstance()
    }

    CustomItemBuilder tab(CreativeModeTab tab){
        Class creativeModeTabClass = Class.forName(Mapping.getClassName('net.minecraft.world.item.CreativeModeTab'))
        Method method = clazz.getMethod(Mapping.getMethodName('net.minecraft.world.item.Item$Properties', 'tab', ['net.minecraft.world.item.CreativeModeTab']), new Class[]{creativeModeTabClass})
        method.invoke(internal, ReflectionHelper.getStaticFieldValue('net.minecraft.world.item.CreativeModeTab', tab.name()))
        return this
    }

    Object createItem(ResourceLocation id){
        Object item = ReflectionHelper.construct('net.minecraft.world.item.Item', ['net.minecraft.world.item.Item$Properties'], [internal])
        Registry.ITEM.register(id, item)
        return item
    }

    Object createBlockItem(ResourceLocation id, Object block){
        Object item = ReflectionHelper.construct('net.minecraft.world.item.BlockItem', ['net.minecraft.world.level.block.Block','net.minecraft.world.item.Item$Properties'], [block, internal])
        Registry.ITEM.register(id, item)
        return item
    }

}
