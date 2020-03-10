package de.ropemc.rope.mc115

import de.ropemc.rope.mc115.hook.HookCall
import de.ropemc.rope.mc115.hook.HookTransformer

import java.lang.instrument.Instrumentation;

class Bootstrap {

    static void premain(String args, Instrumentation instrumentation){
        instrumentation.addTransformer(new HookTransformer())
        Log.init()
        Log.info('Rope v3.0.0 on MC('+readArgs().version+')')
        HookTransformer.before('eba', 'a', []){ HookCall call ->
            call.returnValue = 'Forge sucks!'
        }
    }

    private static Map<String, String> readArgs(){
        Map<String, String> options = [:]
        String[] args = System.getProperty('sun.java.command').split(' ')
        for(int i=1; i<args.length; i++){
            if(args[i].startsWith('--')){
                options[args[i].substring(2)] = args[i+1]
            }
        }
        return options
    }

}
