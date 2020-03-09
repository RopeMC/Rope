# Rope
Third generation of Rope, a javaagent based minecraft modding api

## Compatibility
| Minecraft Version | Supported          |
|-------------------|--------------------|
| >= 1.16           | :warning:          |
| 1.15.x            | :heavy_check_mark: |
| <= 1.14.x         | :x:                |

:x: = unsupported, :warning: = untested, :heavy_check_mark: = supported
## Usage
### Building
Just run `mvn clean package`  
*You need maven in order to build the project.*
### Installation
Edit your profile and add `-javaagent:path/to/Rope-MC115-1.0.jar` to the jvm arguments.
Thats literally all you need to do and you are ready to launch.
Rope will automatically generate a `Rope` directory in your minecraft directory containing all Rope related data (mods, configs, ...).

## Contribution
If you want to contribute consider joining our [Discord]() because we discuss further development decisions over there.
