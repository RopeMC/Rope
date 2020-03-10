package de.ropemc.rope.mc115.mapping

import de.ropemc.rope.mc115.RopeFileManager
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
    static String getClassName(String name){
        MAPPING.classNames[name]
    }
    boolean processClassMapping(String className, String obfClassName){
        classNames[className] = obfClassName
        return true
    }
    void processFieldMapping(String className, String fieldType, String fieldName, String obfClassName, String obfFieldName){

    }
    void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String obfClassName, int obfFirstLineNumber, int obfLastLineNumber, String obfMethodName){

    }
}
