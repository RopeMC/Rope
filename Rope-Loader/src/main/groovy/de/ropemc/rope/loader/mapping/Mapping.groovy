package de.ropemc.rope.loader.mapping

import de.ropemc.rope.MinecraftVersion
import de.ropemc.rope.TargetVersion
import de.ropemc.rope.loader.RopeFileManager
import groovy.json.JsonSlurper

class Mapping implements MappingProcessor {
    static Mapping MAPPING = new Mapping()
    static boolean init(TargetVersion version){
        if(version.isClient()){
            File mappingFile = RopeFileManager.getMappingFile(version.minecraftVersion.versionString)
            if(!mappingFile.exists()) {
                if (!downloadClient(version.minecraftVersion.versionString)) {
                    return false
                }
            }
            new ProguardMappingReader(mappingFile).read(MAPPING)
            return true
        }
        if(version.isVanillaServer()){
            File mappingFile = RopeFileManager.getMappingFile(version.minecraftVersion.versionString)
            if(!mappingFile.exists()) {
                if (!downloadServer(version.minecraftVersion)) {
                    return false
                }
            }
            new ProguardMappingReader(mappingFile).read(MAPPING)
            return true
        }
        return false
    }
    private static boolean downloadClient(String version){
        File mappingFile = RopeFileManager.getMappingFile(version)
        File jsonFile = RopeFileManager.getMinecraftVersionJson(version)
        if(!jsonFile.exists())
            return false
        String url = new JsonSlurper().parse(jsonFile, 'utf-8').downloads.client_mappings.url
        mappingFile.bytes = url.toURL().bytes
        return true
    }
    private static boolean downloadServer(MinecraftVersion version){
        File mappingFile = RopeFileManager.getMappingFile(version.versionString)
        String url = null
        switch (version){
            case MinecraftVersion.MC1_15:
                url = 'https://launcher.mojang.com/v1/objects/42894100f8b63029631b9bb54f9b233108f91650/server.txt'
                break
            case MinecraftVersion.MC1_15_1:
                url = 'https://launcher.mojang.com/v1/objects/47f8a03f5492223753f5f2b531d4938813903684/server.txt'
                break
            case MinecraftVersion.MC1_15_2:
                url = 'https://launcher.mojang.com/v1/objects/59c55ae6c2a7c28c8ec449824d9194ff21dc7ff1/server.txt'
                break
        }
        if(url == null)
            return false
        mappingFile.bytes = url.toURL().bytes
        return true
    }
    private Map<String, String> classNames = [:]
    private Map<String, String> fieldNames = [:]
    private Map<String, String> methodNames = [:]
    static String getClassName(String name){
        MAPPING.classNames.getOrDefault(name, name)
    }
    static String getFieldName(String className, String name){
        MAPPING.fieldNames.getOrDefault(className+'@'+name, name)
    }
    static String getMethodName(String className, String name, List<String> paramTypes){
        MAPPING.methodNames.getOrDefault(className+'@'+name+'('+paramTypes.join(',')+')', name)
    }
    boolean processClassMapping(String className, String obfClassName){
        classNames[className] = obfClassName
        return true
    }
    void processFieldMapping(String className, String fieldName, String obfFieldName){
        fieldNames[className+'@'+fieldName] = obfFieldName
    }
    void processMethodMapping(String className, String methodName, String methodArguments, String obfMethodName){
        methodNames[className+'@'+methodName+'('+methodArguments+')'] = obfMethodName
    }
}
