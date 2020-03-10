package de.ropemc.rope.mc115

class RopeFileManager {

    static File ROPE, MAPPING

    static {
        ROPE = new File('Rope')
        MAPPING = new File(ROPE, 'Mapping')
        if(!ROPE.exists())
            ROPE.mkdir()
        if(!MAPPING.exists())
            MAPPING.mkdir()
    }

    static File getMinecraftVersion(String version){
        return new File('versions', version)
    }

    static File getMinecraftVersionJson(String version){
        return new File(getMinecraftVersion(version), version+'.json')
    }

    static File getMappingFile(String version){
        return new File(MAPPING, version+'.txt')
    }

}
