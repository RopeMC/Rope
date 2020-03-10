package de.ropemc.rope.loader.hook

class HookCall {
    final Object instance
    final Object[] params
    HookCall(Object instance, Object[] params){
        this.instance = instance
        this.params = params
    }
    boolean returned = false
    Object returnValue
    void setReturnValue(Object value){
        this.returnValue = value
        returned = true
    }
}
