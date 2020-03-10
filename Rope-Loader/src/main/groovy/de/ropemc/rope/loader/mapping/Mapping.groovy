package de.ropemc.rope.loader.mapping

import de.ropemc.rope.loader.RopeFileManager
import groovy.json.JsonSlurper

class Mapping {
    static Mapping MAPPING = new Mapping()
    static boolean init(String version){
        File mappingFile = RopeFileManager.getMappingFile(version)
        if(!mappingFile.exists()) {
            if (!download(version)) {
                return false
            }
        }
        new MappingReader(mappingFile).read(MAPPING)
        return true
    }
    private static boolean download(String version){
        File mappingFile = RopeFileManager.getMappingFile(version)
        File jsonFile = RopeFileManager.getMinecraftVersionJson(version)
        if(!jsonFile.exists())
            return false
        String url = new JsonSlurper().parse(jsonFile, 'utf-8').downloads.client_mappings.url
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
    void processFieldMapping(String className, String fieldType, String fieldName, String obfClassName, String obfFieldName){
        fieldNames[className+'@'+fieldName] = obfFieldName
    }
    void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String obfClassName, int obfFirstLineNumber, int obfLastLineNumber, String obfMethodName){
        methodNames[className+'@'+methodName+'('+methodArguments+')'] = obfMethodName
    }
}
