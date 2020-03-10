package de.ropemc.rope.mc115.hook

class Hook {
    private static int NEXT = 0
    final int id
    final String className
    final String methodName
    final List<String> paramTypes
    final List<Closure> before = []
    final List<Closure> after = []
    Hook(String className, String methodName, List<String> paramTypes){
        this.id = NEXT
        NEXT++
        this.className = className
        this.methodName = methodName
        this.paramTypes = paramTypes
    }
}
