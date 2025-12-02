# WardenMod Plugin - Installation & Usage Guide

## Quick Start

### Installation
The plugin is already compiled and ready to use:
- **File**: `WardenMod-1.0.0.jar`
- **Location**: `/servers/survival/plugins/WardenMod-1.0.0.jar`

To activate:
1. If server is running, use `/reload confirm` or restart the server
2. Check console for: `[WardenMod] WardenMod enabled.`

### Quick Commands

```
/warden              - Summon a powerful Warden boss
/shrieker            - Get a Shrieker block
/skulk               - Get a Skulk block
/skulksensor         - Get a Skulk Sensor block
```

## Warden Boss Battle

The Warden is a powerful boss with these properties:
- **500 HP** (250 hearts)
- **21 Damage** per attack
- Stunning visual particle effects
- Drops loot on death
- Spawns in front of where you're standing

### How to Fight
1. Use `/warden` to summon the boss
2. The Warden will be an Iron Golem with enhanced stats
3. It will attack entities nearby
4. Has impressive particle effects for attacks
5. Drops rare loot when defeated

## Building with Skulk Blocks

Create Warden's deep dark dimension aesthetic:

```
1. Get blocks with commands:
   /skulk              -> Get purple skulk blocks
   /shrieker           -> Get shrieker blocks (makes noise effects)
   /skulksensor        -> Get red stone detectors

2. Build your deep dark structure

3. Skulk Sensors detect movement and trigger effects
   within 16 blocks
```

## File Structure

```
WardenMod/
├── WardenMod-1.0.0.jar          (compiled plugin)
├── README.md                     (full documentation)
├── pom.xml                       (Maven build config)
└── src/main/
    ├── java/com/shadowlord/wardenmod/
    │   ├── WardenModPlugin.java        (main plugin class)
    │   ├── commands/
    │   │   ├── WardenCommand.java
    │   │   ├── ShriekerCommand.java
    │   │   ├── SkulkCommand.java
    │   │   └── SkulkSensorCommand.java
    │   ├── entity/
    │   │   └── Warden.java             (warden entity class)
    │   └── listeners/
    │       ├── WardenListener.java     (attack & particle effects)
    │       ├── ShriekerListener.java   (shrieker effects)
    │       └── SkulkSensorListener.java (sensor effects)
    └── resources/
        ├── plugin.yml                  (plugin configuration)
        └── config.yml                  (empty, can be customized)
```

## Recompiling (If Needed)

To rebuild from source:

```bash
cd /workspaces/EaglerServer-1.12/EaglerServer-1.12/servers/survival/plugins/WardenMod
mvn clean package
cp target/WardenMod-1.0.0.jar ../WardenMod-1.0.0.jar
```

Then restart the server.

## Customization

### Changing Warden Health
Edit `src/main/java/com/shadowlord/wardenmod/entity/Warden.java`:
```java
private static final double WARDEN_MAX_HEALTH = 500.0; // Change this value
```

### Changing Warden Damage
Edit `src/main/java/com/shadowlord/wardenmod/listeners/WardenListener.java`:
```java
event.setDamage(21.0); // Change this value
```

### Changing Attack Cooldown
Edit `src/main/java/com/shadowlord/wardenmod/entity/Warden.java`:
```java
private static final int ATTACK_COOLDOWN = 8; // In ticks (lower = faster)
```

## Troubleshooting

### Plugin won't load
- Check that Spigot 1.12.2 is running
- Check console for error messages
- Verify `plugin.yml` is correct

### Commands not working
- Ensure you have OP permissions
- Check that plugin is loaded: `/plugins`
- Verify command names are correct

### No particle effects
- Particle rendering depends on client settings
- Try adjusting particle settings in game
- Some particles may not render in all distances

## Features Breakdown

| Feature | Visual | Interactive |
|---------|--------|-------------|
| Warden Boss | Iron Golem | Summon with /warden |
| Shrieker | Prismarine | Place & see effects |
| Skulk | Purple Wool | Buildable block |
| Skulk Sensor | Purpur Pillar | Detects movement |

## Performance

- Minimal performance impact
- No tick loops running constantly
- Only triggers effects on events
- Particle effects optimized for visibility

---

**Enjoy the Warden!** 🖤⚫
