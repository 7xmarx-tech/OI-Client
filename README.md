# TRAP CLIENT

TRAP CLIENT is a Windows Minecraft launcher and a client-side Fabric client for
Minecraft 26.1.2. The launcher signs in through Microsoft device-code
authentication, installs Minecraft and Fabric, installs TRAP CLIENT and Fabric
API, and launches the game. The mod adds a dark violet Click GUI and configurable
HUD widgets for information available to the local client: coordinates, FPS,
local time, the local player's status and armor rating, and the current world's
time of day.

## Requirements

- Minecraft Java Edition 26.1.2
- Fabric Loader 0.20.1 or newer
- Fabric API 0.155.3+26.1.2 or newer for Minecraft 26.1.2
- Java 25

Minecraft itself is not included. The launcher obtains the game files through
the user's normal Microsoft account flow. A valid Minecraft: Java Edition
entitlement is required.

## Windows launcher

Download `TrapClient-Setup-26.1.2.exe` from a TRAP CLIENT GitHub Release and run
the installer. The launcher stores its game files and settings under your
Windows Local AppData directory.

The launcher requires a Microsoft Entra public application client ID to enable
device-code sign-in. Enter the ID in the launcher; it is saved locally in
`%LOCALAPPDATA%\TRAP CLIENT\launcher.json`. The launcher does not request or
store your Microsoft password or a client secret. Authentication can only be
fully verified with a correctly registered public client application and an
eligible Microsoft account.

## Build

```sh
./gradlew build
```

Build produces both:

- `build/libs/TrapClient-26.1.2.jar` — the Fabric mod.
- `build/distributions/TrapClient-26.1.2.mrpack` — an importable launcher pack
  containing TRAP CLIENT and a checksummed Fabric API download entry.

The Windows launcher is built by `.github/workflows/build-launcher.yml`. Run the
workflow manually to download the portable `TrapClientLauncher.exe` and
installer as workflow artifacts. To publish both as downloadable release
assets, push a tag named `launcher-v*` (for example `launcher-v1.0.0`).
Building the launcher also builds and embeds the Fabric mod JAR.

## Use the Modrinth pack

Import the `.mrpack` into a launcher that supports Modrinth modpacks, such as
the Modrinth App or Prism Launcher. The launcher installs Minecraft 26.1.2,
Fabric Loader, and Fabric API; the pack supplies TRAP CLIENT. Launch the
resulting profile and press **Right Shift** to open or close the Click GUI.
The key can also be changed in Minecraft's Controls menu.

Feature toggles, accent colors, HUD positions, and edge offsets are saved to
`config/trapclient.properties` and restored on the next launch.

All features are client-only and use information exposed to the local player.
TRAP CLIENT does not inspect hidden entities, send gameplay commands, or alter
server-authoritative game state.
