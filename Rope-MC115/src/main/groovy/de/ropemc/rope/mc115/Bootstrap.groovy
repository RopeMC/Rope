package de.ropemc.rope.mc115

import de.ropemc.rope.Rope
import de.ropemc.rope.RopeAPI
import de.ropemc.rope.TargetVersion
import de.ropemc.rope.loader.Log
import de.ropemc.rope.loader.VersionDetector
import de.ropemc.rope.loader.hook.HookTransformer
import de.ropemc.rope.loader.mapping.Mapping

import java.lang.instrument.Instrumentation
import java.lang.reflect.Field

class Bootstrap implements RopeAPI {

    static void premain(String args, Instrumentation instrumentation){
        TargetVersion version = VersionDetector.detect()
        Log.init()
        if(version == TargetVersion.UNSUPPORTED){
            Log.warn '==============================================\n  ROPE COULD NOT DETECT A SUPPORTED VERSION!\n=============================================='
            return
        }
        Bootstrap bootstrap = new Bootstrap(version)
        Rope.rope = bootstrap
        bootstrap.run(instrumentation)
    }

    final TargetVersion targetVersion

    Bootstrap(TargetVersion targetVersion){
        this.targetVersion = targetVersion
    }

    void run(Instrumentation instrumentation){
        Log.init()
        if(!Mapping.init(targetVersion)){
            Log.warn "Mapping for Rope couldn't be loaded!"
            return
        }
        Log.info('Rope v3.0.0 on MC('+ targetVersion.minecraftVersion.versionString+')')
        instrumentation.addTransformer(new HookTransformer())
        HookTransformer.beforeStaticInitializer('net.minecraft.world.level.block.Blocks'){
            Object block = new CustomBlockBuilder(Material.METAL).createBlock(new ResourceLocation('test', 'twitch_block'))
            Object blockItem = new CustomItemBuilder().tab(CreativeModeTab.TAB_FOOD).createBlockItem(new ResourceLocation('test', 'twitch_block'), block)
        }
        HookTransformer.afterStaticInitializer('net.minecraft.world.item.Items'){
            Object item = new CustomItemBuilder().tab(CreativeModeTab.TAB_FOOD).createItem(new ResourceLocation('test', 'twitch'))
        }
        if(targetVersion.isClient()){
            HookTransformer.before('net.minecraft.client.resources.SplashManager', 'getSplash', []){ call ->
                call.returnValue = 'Forge sucks!'
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
    }

}
