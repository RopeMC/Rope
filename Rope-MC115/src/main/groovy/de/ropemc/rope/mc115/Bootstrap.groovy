package de.ropemc.rope.mc115

import de.ropemc.rope.Rope
import de.ropemc.rope.RopeAPI
import de.ropemc.rope.TargetVersion
import de.ropemc.rope.loader.Log
import de.ropemc.rope.loader.ReflectionHelper
import de.ropemc.rope.loader.VersionDetector
import de.ropemc.rope.loader.hook.HookTransformer
import de.ropemc.rope.loader.mapping.Mapping

import java.lang.instrument.Instrumentation
import java.lang.reflect.Field
import java.lang.reflect.Method

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
        Object block
        HookTransformer.beforeStaticInitializer('net.minecraft.world.level.block.Blocks'){
            Log.info 'BLOCKS'
            block = new CustomBlockBuilder(Material.METAL).createBlock(new ResourceLocation('test', 'twitch_block'))
        }
        HookTransformer.afterStaticInitializer('net.minecraft.world.item.Items'){
            Log.info 'ITEMS'
            Object blockItem = new CustomItemBuilder().tab(CreativeModeTab.TAB_FOOD).createBlockItem(new ResourceLocation('test', 'twitch_block'), block)
            Map BY_BLOCK = ReflectionHelper.getStaticFieldValue('net.minecraft.world.item.Item', 'BY_BLOCK')
            BY_BLOCK.put(block, blockItem)
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
        File assetsFolder = new File('C:\\Users\\Jan\\AppData\\Roaming\\.minecraft\\resourcepacks\\server\\assets')
        if(targetVersion.isServer()){
            HookTransformer.before('net.minecraft.client.resources.SplashManager', 'getSplash', []){ call ->
                call.returnValue = 'Forge sucks!'
            }
            HookTransformer.afterConstructor('net.minecraft.client.resources.DefaultClientResourcePack', ['net.minecraft.client.resources.AssetIndex']){ call ->
                Class clazz = Class.forName(Mapping.getClassName('net.minecraft.server.packs.VanillaPack'))
                Field field = clazz.getField(Mapping.getFieldName('net.minecraft.server.packs.VanillaPack', 'namespaces'))
                field.setAccessible(true)
                field.set(call.instance, ['minecraft', 'realms', 'test'].toSet())
            }
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
        HookTransformer.before('net.minecraft.server.packs.resources.SimpleReloadableResourceManager', 'getNamespaces', []){ call ->
            Set<String> namespaces = new HashSet<>(ReflectionHelper.getFieldValue('net.minecraft.server.packs.resources.SimpleReloadableResourceManager', 'namespaces', call.instance) as Set<String>)
            // -> Add own namespaces if not present
            call.returnValue = namespaces
        }
        HookTransformer.before('net.minecraft.server.packs.resources.SimpleReloadableResourceManager', 'listResources', ['java.lang.String','java.util.function.Predicate']){ call ->
            List resourceLocations = new ArrayList()
            Map<String, Object> namespacedPacks = ReflectionHelper.getFieldValue('net.minecraft.server.packs.resources.SimpleReloadableResourceManager', 'namespacedPacks', call.instance)
            namespacedPacks.values().each {
                resourceLocations.addAll(ReflectionHelper.callMethod('net.minecraft.server.packs.resources.FallbackResourceManager', 'listResources', ['java.lang.String','java.util.function.Predicate'], it, Arrays.asList(call.params)))
            }
            String path = call.params[0]
            // -> Add own resource locations
            Collections.sort(resourceLocations)
            call.returnValue = resourceLocations
        }
        // -> SimpleReloadableResourceManager needs to be overwritten to load data
    }

}
