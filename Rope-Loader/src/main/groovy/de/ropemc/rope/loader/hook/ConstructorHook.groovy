package de.ropemc.rope.loader.hook

import java.util.function.Consumer

class ConstructorHook {
    private static int NEXT = 0
    final int id
    final String className
    final List<String> paramTypes
    final List<Consumer<HookCall>> after = []

    ConstructorHook(String className, List<String> paramTypes){
        this.id = NEXT
        NEXT++
        this.className = className
        this.paramTypes = paramTypes
    }
}
