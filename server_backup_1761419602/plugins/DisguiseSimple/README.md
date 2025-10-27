# DisguiseSimple Plugin

## Requirements
- ProtocolLib plugin (place ProtocolLib.jar in your server's plugins folder)
- Bukkit/Spigot/Paper 1.12.2

## Installation
1. Download ProtocolLib for 1.12.2: https://www.spigotmc.org/resources/protocollib.1997/
2. Place ProtocolLib.jar in `/workspaces/EaglerServer-1.12/server/plugins/`
3. Build DisguiseSimple with Maven and place the resulting jar in the same plugins folder.
4. Restart your server.

## Usage
- `/disguise mob <mobname>`: Disguise as a mob (e.g., zombie, creeper, skeleton)
- `/disguise player <playername>`: Disguise as another player
- `/undisguise`: Remove disguise

## Notes
- Visual disguises require ProtocolLib. Without it, only chat feedback is provided.
- For advanced disguises (skin changes, custom mobs), further packet logic can be added.
