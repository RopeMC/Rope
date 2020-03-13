package de.ropemc.rope.loader

import de.ropemc.rope.MinecraftVersion
import de.ropemc.rope.TargetVersion
import groovy.json.JsonSlurper
import javassist.ClassPool

import java.util.jar.JarFile

class VersionDetector {
    static TargetVersion detect(){
        ClassPool cp = ClassPool.default
        try {
            cp.get('org.spigotmc.SpigotConfig')
            return TargetVersion.getSpigot(findServerVersion())
        }catch(Exception ex){}
        try {
            cp.get('net.minecraft.realms.Realms')
            def args = readClientArgs()
            if(args.version == null)
                return TargetVersion.UNSUPPORTED
            return TargetVersion.getClient(MinecraftVersion.byVersionString(args.version))
        }catch(Exception ex){}
        return TargetVersion.getServer(findServerVersion())
    }
    private static MinecraftVersion findServerVersion(){
        def serverJar = findServerJar()
        if(serverJar == null)
            return MinecraftVersion.UNKNOWN
        serverJar = new File(serverJar)
        if(!serverJar.exists())
            return MinecraftVersion.UNKNOWN
        serverJar = new JarFile(serverJar)
        def entry = serverJar.getEntry('version.json')
        if(entry == null)
            return MinecraftVersion.UNKNOWN
        MinecraftVersion.byVersionString(new JsonSlurper().parse(serverJar.getInputStream(entry)).id as String)
    }
    private static String findServerJar(){
        for(s in System.getProperty('java.class.path').split(System.getProperty("os.name").contains('win')?';':':')){
            if(s.endsWith('.jar')){
                return s
            }
        }
        return null
    }
    private static Map<String, String> readClientArgs(){
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
