package de.ropemc.rope.loader.mapping

class ProguardMappingReader {
    private final File mappingFile

    ProguardMappingReader(File mappingFile) {
        this.mappingFile = mappingFile
    }
    void read(Mapping mapping) throws IOException {
        LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(mappingFile)))
        try {
            String className = null
            while (true) {
                String line = reader.readLine()
                if (line == null) {
                    break
                }
                line = line.trim()
                if (!line.startsWith("#")) {
                    if (line.endsWith(":")) {
                        className = processClassMapping(line, mapping)
                    } else if (className != null) {
                        processClassMemberMapping(className, line, mapping)
                    }
                }
            }
        } catch (IOException ex) {
            throw new IOException("Can't process mapping file (" + ex.getMessage() + ")")
        } finally {
            try {
                reader.close()
            } catch (IOException ex) {
            }
        }
    }

    private String processClassMapping(String line, Mapping mapping) {
        int arrowIndex = line.indexOf("->")
        if (arrowIndex < 0)
            return null
        int colonIndex = line.indexOf(':', arrowIndex + 2)
        if (colonIndex < 0)
            return null
        String className = line.substring(0, arrowIndex).trim()
        String newClassName = line.substring(arrowIndex + 2, colonIndex).trim()
        boolean interested = mapping.processClassMapping(className, newClassName)
        return interested ? className : null
    }

    private void processClassMemberMapping(String className, String line, Mapping mapping) {
        int colonIndex1 = line.indexOf(':')
        int colonIndex2 = colonIndex1 < 0 ? -1 : line.indexOf(':', colonIndex1 + 1)
        int spaceIndex = line.indexOf(' ', colonIndex2 + 2)
        int argumentIndex1 = line.indexOf('(', spaceIndex + 1)
        int argumentIndex2 = argumentIndex1 < 0 ? -1 : line.indexOf(')', argumentIndex1 + 1)
        int colonIndex3 = argumentIndex2 < 0 ? -1 : line.indexOf(':', argumentIndex2 + 1)
        int colonIndex4 = colonIndex3 < 0 ? -1 : line.indexOf(':', colonIndex3 + 1)
        int arrowIndex = line.indexOf("->", (colonIndex4 >= 0 ? colonIndex4 : colonIndex3 >= 0 ? colonIndex3 : argumentIndex2 >= 0 ? argumentIndex2 : spaceIndex) + 1)
        if (spaceIndex < 0 || arrowIndex < 0)
            return
        String type = line.substring(colonIndex2 + 1, spaceIndex).trim()
        String name = line.substring(spaceIndex + 1, argumentIndex1 >= 0 ? argumentIndex1 : arrowIndex).trim()
        String newName = line.substring(arrowIndex + 2).trim()
        int dotIndex = name.lastIndexOf('.')
        if (dotIndex >= 0) {
            className = name.substring(0, dotIndex)
            name = name.substring(dotIndex + 1)
        }
        if (type.length() > 0 && name.length() > 0 && newName.length() > 0) {
            if (argumentIndex2 < 0) {
                mapping.processFieldMapping(className, name, newName)
            } else {
                String arguments = line.substring(argumentIndex1 + 1, argumentIndex2).trim()
                mapping.processMethodMapping(className, name, arguments, newName)
            }
        }
    }
}