package de.ropemc.rope.loader.mapping

class NMSRemapper implements MappingProcessor {
    MappingProcessor target
    String version
    NMSRemapper(MappingProcessor target, String version){
        this.target = target
        this.version = version
    }
    boolean processClassMapping(String className, String obfClassName){
        target.processClassMapping(className, 'net.minecraft.server.'+version+'.'+className.split(/\./).last())
        return true
    }
    void processFieldMapping(String className, String fieldName, String obfFieldName){
        target.processFieldMapping(className, fieldName, obfFieldName)
    }
    void processMethodMapping(String className, String methodName, String methodArguments, String obfMethodName){
        target.processMethodMapping(className, methodName, methodArguments, obfMethodName)
    }
}
