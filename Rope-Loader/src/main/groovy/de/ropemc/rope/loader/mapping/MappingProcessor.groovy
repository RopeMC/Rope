package de.ropemc.rope.loader.mapping

interface MappingProcessor {
    boolean processClassMapping(String className, String obfClassName)
    void processFieldMapping(String className, String fieldName, String obfFieldName)
    void processMethodMapping(String className, String methodName, String methodArguments, String obfMethodName)
}