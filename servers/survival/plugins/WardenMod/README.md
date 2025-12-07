# WardenMod Plugin

A comprehensive Minecraft plugin that adds Warden-themed content to your Spigot 1.12.2 server, including the Warden boss mob, Shriekers, Skulk blocks, and Skulk Sensors.

## Features

### Warden Boss Mob
- Spawnable iron golem with Warden-like properties
- **Health**: 500 HP (250 hearts) - matches vanilla Warden
- **Damage**: 21 damage per attack (10.5 hearts)
- Particle effects for attacks including:
  - Explosion particles for impact
  - Witch spell trails along attack path
  - Sonic boom visual effects
- Death particles and loot on destruction

### Shriekers
- Get Shrieker blocks with `/shrieker` command
- Visual particle effects when placed
- Note-based particle animations

### Skulk Blocks
- Get Skulk blocks with `/skulk` command
- Purple wool placeholder with authentic appearance
- Can be collected and placed

### Skulk Sensors
- Get Skulk Sensor blocks with `/skulksensor` command
- Purpur pillar visual representation
- Redstone particle effects when activated
- Detects nearby movements within 16 block radius

## Commands

### `/warden`
Summons a Warden (Iron Golem with Warden properties) at your location.
- Permission: `wardenmod.warden`
- Requires OP by default

### `/shrieker`
Gives you a Shrieker block (Prismarine).
- Permission: `wardenmod.shrieker`
- Requires OP by default

### `/skulk`
Gives you a Skulk block (Purple Wool).
- Permission: `wardenmod.skulk`
- Requires OP by default

### `/skulksensor`
Gives you a Skulk Sensor block (Purpur Pillar).
- Permission: `wardenmod.skulksensor`
- Requires OP by default

## Permissions

- `wardenmod.warden` - Permission to summon Warden (default: OP)
- `wardenmod.shrieker` - Permission to get Shrieker (default: OP)
- `wardenmod.skulk` - Permission to get Skulk (default: OP)
- `wardenmod.skulksensor` - Permission to get Skulk Sensor (default: OP)

## Installation

1. Place `WardenMod-1.0.0.jar` in your `plugins/` folder
2. Restart your server or use `/reload confirm`
3. Configure permissions as needed

## Technical Details

### Warden Stats
- Max Health: 500 HP
- Attack Damage: 21
- Attack Cooldown: 8 ticks
- Behavior: Attacks all entities like a normal iron golem

### Block Materials
Since 1.12.2 doesn't have native Warden blocks, the plugin uses these placeholders:
- **Shrieker**: Prismarine
- **Skulk**: Purple Wool (Durability 10)
- **Skulk Sensor**: Purpur Pillar

## Particle Effects

### Attack Particles
- Large explosion effects at impact point
- Normal explosion particles surrounding target
- Witch spell trail from Warden to target

### Death Particles
- Massive explosion on death
- Smoke clouds
- Magical particle effects

### Block Placement Particles
- **Shrieker**: Musical note particles
- **Skulk**: Purple/dark particles
- **Skulk Sensor**: Red stone particles with spell effects

## Configuration

The plugin uses default permissions. To customize, edit your permission plugin's configuration or modify the default OP permissions in your `ops.json` file.

## Compatibility

- **Server Version**: Spigot 1.12.2
- **Java Version**: 1.8+
- **Dependencies**: None (uses only Bukkit API)

## Version

- **Plugin Version**: 1.0.0
- **Author**: ShadowLord
- **Built with**: Maven 3.8.1+

## Support

For issues or feature requests, contact the server administrator.

---

**Note**: This plugin simulates Warden functionality using Iron Golems and custom properties to match as closely as possible to Minecraft's vanilla Warden mob introduced in 1.19+.
