package de.ropemc.rope.mc115

import de.ropemc.rope.loader.Log
import de.ropemc.rope.loader.ReflectionHelper
import de.ropemc.rope.loader.hook.HookCall
import de.ropemc.rope.loader.hook.HookTransformer
import de.ropemc.rope.loader.mapping.Mapping
import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtMethod

import java.lang.instrument.Instrumentation;

class Bootstrap {

    static void premain(String args, Instrumentation instrumentation){
        instrumentation.addTransformer(new HookTransformer())
        String version = readArgs().version
        Log.init()
        Log.info('Rope v3.0.0 on MC('+version+')')
        Mapping.init(version)
        HookTransformer.before('net.minecraft.client.resources.SplashManager', 'getSplash', []){ HookCall call ->
            call.returnValue = 'Forge sucks!'
        }
        HookTransformer.staticInitializer('net.minecraft.world.item.CreativeModeTab'){
            ClassPool cp = ClassPool.getDefault()
            CtClass newTabClass = cp.makeClass('de.ropemc.rope.generated.tabs.Test', cp.get(Mapping.getClassName('net.minecraft.world.item.CreativeModeTab')))
            CtMethod makeIcon = new CtMethod(cp.get(Mapping.getClassName('net.minecraft.world.item.ItemStack')), Mapping.getMethodName('net.minecraft.world.item.CreativeModeTab','makeIcon', []), new CtClass[0], newTabClass)
            makeIcon.setBody("{return new ${Mapping.getClassName('net.minecraft.world.item.ItemStack')}(${Mapping.getClassName('net.minecraft.world.item.Items')}.${Mapping.getFieldName('net.minecraft.world.item.Items', 'TNT')});}")
            newTabClass.addMethod(makeIcon)
            Class cl = newTabClass.toClass()
            cl.getConstructor(int.class, String.class).newInstance(4, 'test')
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
