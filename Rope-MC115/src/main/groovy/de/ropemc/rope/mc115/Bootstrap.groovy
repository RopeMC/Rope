package de.ropemc.rope.mc115

import de.ropemc.rope.loader.Log
import de.ropemc.rope.loader.hook.HookTransformer
import de.ropemc.rope.loader.mapping.Mapping

import java.lang.instrument.Instrumentation
import java.lang.reflect.Field;

class Bootstrap {

    static void premain(String args, Instrumentation instrumentation){
        instrumentation.addTransformer(new HookTransformer())
        String version = readArgs().version
        Log.init()
        Log.info('Rope v3.0.0 on MC('+version+')')
        Mapping.init(version)
        HookTransformer.before('net.minecraft.client.resources.SplashManager', 'getSplash', []){ call ->
            call.returnValue = 'Forge sucks!'
        }
        HookTransformer.beforeStaticInitializer('net.minecraft.world.level.block.Blocks'){
            Object block = new CustomBlockBuilder(Material.METAL).createBlock(new ResourceLocation('test', 'twitch_block'))
            Object blockItem = new CustomItemBuilder().tab(CreativeModeTab.TAB_FOOD).createBlockItem(new ResourceLocation('test', 'twitch_block'), block)
        }
        HookTransformer.afterStaticInitializer('net.minecraft.world.item.Items'){
            Object item = new CustomItemBuilder().tab(CreativeModeTab.TAB_FOOD).createItem(new ResourceLocation('test', 'twitch'))
        }
        HookTransformer.afterConstructor('net.minecraft.client.resources.DefaultClientResourcePack', ['net.minecraft.client.resources.AssetIndex']){ call ->
            Class clazz = Class.forName(Mapping.getClassName('net.minecraft.server.packs.VanillaPack'))
            Field field = clazz.getField(Mapping.getFieldName('net.minecraft.server.packs.VanillaPack', 'namespaces'))
            field.setAccessible(true)
            field.set(call.instance, ['minecraft', 'realms', 'test'].toSet())
        }
        File assetsFolder = new File('C:\\Users\\Jan\\AppData\\Roaming\\.minecraft\\resourcepacks\\server\\assets')
        HookTransformer.before('net.minecraft.client.resources.DefaultClientResourcePack', 'hasResource', ['net.minecraft.server.packs.PackType', 'net.minecraft.resources.ResourceLocation']){ call ->
            ResourceLocation rl = new ResourceLocation(call.params[1])
            File resourceFile = new File(new File(assetsFolder, rl.getNamespace()), rl.getPath())
            if(resourceFile.exists()){
                call.returnValue = true
            }
        }
        HookTransformer.before('net.minecraft.client.resources.DefaultClientResourcePack', 'getResourceAsStream', ['net.minecraft.server.packs.PackType', 'net.minecraft.resources.ResourceLocation']){ call ->
            ResourceLocation rl = new ResourceLocation(call.params[1])
            File resourceFile = new File(new File(assetsFolder, rl.getNamespace()), rl.getPath())
            if(resourceFile.exists()){
                call.returnValue = resourceFile.newInputStream()
            }
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
