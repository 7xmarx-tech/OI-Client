# TRAP CLIENT

TRAP CLIENT is a client-side Fabric mod for Minecraft 26.1.2. It provides a
violet Click GUI and configurable HUD widgets using information available to
the local client.

## Install

1. Install Fabric Loader 0.20.1 for Minecraft 26.1.2 and Fabric API
   0.155.3+26.1.2.
2. Download `TrapClient-26.1.2.jar` from the [TRAP CLIENT mod release](https://github.com/7xmarx-tech/OI-Client/releases/tag/mod-v1.0.0).
3. Put the JAR in your Minecraft `mods` folder and start the Fabric profile.
4. Press **Right Shift** to open or close the Click GUI. You can change the
   key in Minecraft's Controls menu under **TRAP CLIENT**.

TRAP CLIENT requires Java 25. Minecraft and Fabric API are separate downloads;
neither is bundled in the mod JAR.

## Build

```sh
./gradlew build
```

The Fabric mod JAR is written to `build/libs/TrapClient-26.1.2.jar`.

Feature toggles, accent colors, HUD positions, and edge offsets are saved to
`config/trapclient.properties` and restored on the next launch.

All features are client-only and use information exposed to the local player.
TRAP CLIENT does not inspect hidden entities, send gameplay commands, or alter
server-authoritative game state.
