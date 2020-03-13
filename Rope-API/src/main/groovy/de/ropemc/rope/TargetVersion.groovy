package de.ropemc.rope

import groovy.transform.TupleConstructor

@TupleConstructor
enum TargetVersion {
    CLIENT_VANILLA_1_15_2(MinecraftVersion.MC1_15_2),
    SERVER_VANILLA_1_15_2(MinecraftVersion.MC1_15_2),
    SERVER_SPIGOT_1_15_2(MinecraftVersion.MC1_15_2),
    UNSUPPORTED(MinecraftVersion.UNKNOWN)
    final MinecraftVersion minecraftVersion
    boolean isServer(){
        name().startsWith('SERVER_')
    }
    boolean isSpigotServer(){
        name().startsWith('SERVER_SPIGOT_')
    }
    boolean isVanillaServer(){
        name().startsWith('SERVER_VANILLA_')
    }
    boolean isClient(){
        !server
    }
    static TargetVersion getClient(MinecraftVersion version){
        for(v in values()){
            if(v.minecraftVersion == version && v.isClient()){
                return v
            }
        }
        return UNSUPPORTED
    }
    static TargetVersion getSpigot(MinecraftVersion version){
        for(v in values()){
            if(v.minecraftVersion == version && v.isSpigotServer()){
                return v
            }
        }
        return UNSUPPORTED
    }
    static TargetVersion getServer(MinecraftVersion version){
        for(v in values()){
            if(v.minecraftVersion == version && v.isVanillaServer()){
                return v
            }
        }
        return UNSUPPORTED
    }
}