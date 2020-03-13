package de.ropemc.rope

import groovy.transform.TupleConstructor

@TupleConstructor
enum MinecraftVersion {
    MC1_15('1.15'),
    MC1_15_1('1.15.1'),
    MC1_15_2('1.15.2'),
    UNKNOWN(null)
    final String versionString
    static MinecraftVersion byVersionString(String version){
        if(version == null)
            return UNKNOWN
        for(v in values()){
            if(v.versionString == version){
                return v
            }
        }
        return UNKNOWN
    }
}
