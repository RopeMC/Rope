package de.ropemc.rope.loader

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Log {
    static Logger logger
    static void init(){
        logger = LogManager.logger
    }
    static info(Object o){logger.info(o)}
    static debug(Object o){logger.debug(o)}
    static trace(Object o){logger.trace(o)}
    static warn(Object o){logger.warn(o)}
    static error(Object o){logger.error(o)}
    static fatal(Object o){logger.fatal(o)}
}
