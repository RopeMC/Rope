package de.ropemc.rope.loader.hook

import java.util.function.Consumer

class MethodHook {
    private static int NEXT = 0
    final int id
    final String className
    final String methodName
    final List<String> paramTypes
    final List<Consumer<HookCall>> before = []
    final List<Consumer<HookCall>> after = []

    MethodHook(String className, String methodName, List<String> paramTypes){
        this.id = NEXT
        NEXT++
        this.className = className
        this.methodName = methodName
        this.paramTypes = paramTypes
    }
}
