package de.ropemc.rope.mc115

import de.ropemc.rope.loader.mapping.Mapping

class ResourceLocation {

    Object internal

    ResourceLocation(Object internal){
        this.internal = internal
    }

    ResourceLocation(String path){
        this('minecraft', path)
    }

    ResourceLocation(String namespace, String path){
        this.internal = Class.forName(Mapping.getClassName('net.minecraft.resources.ResourceLocation')).getConstructor(String.class, String.class).newInstance(namespace, path)
    }

    String getPath(){
        internal.class.getMethod(Mapping.getMethodName('net.minecraft.resources.ResourceLocation', 'getPath', [])).invoke(internal)
    }

    String getNamespace(){
        internal.class.getMethod(Mapping.getMethodName('net.minecraft.resources.ResourceLocation', 'getNamespace', [])).invoke(internal)
    }

}
