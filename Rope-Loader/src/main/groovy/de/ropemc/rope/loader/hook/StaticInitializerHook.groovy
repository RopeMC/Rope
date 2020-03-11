package de.ropemc.rope.loader.hook

class StaticInitializerHook {
    private static int NEXT = 0
    final int id
    final String className
    final List<Closure> callbacks = []

    StaticInitializerHook(String className){
        this.id = NEXT
        NEXT++
        this.className = className
    }
}
