# WardenMod Plugin - Complete Index & Quick Reference

## 🎯 Start Here

### 📖 Documentation Files (in order of importance)

1. **QUICKSTART.md** ⭐ START HERE
   - 30-second setup guide
   - Basic commands
   - Troubleshooting
   - What you need to know to get started

2. **README.md**
   - Technical overview
   - Feature descriptions
   - Installation info
   - Stats and specs

3. **INSTALLATION.md**
   - Detailed installation steps
   - How to use commands
   - File structure
   - Recompiling instructions

4. **BUILD_SUMMARY.md**
   - What's included in the build
   - Stats breakdown
   - File listing
   - Technical specs

5. **COMPLETE_GUIDE.md**
   - Comprehensive reference
   - All features explained
   - Customization guide
   - Full technical details

6. **SOURCE_FILES.txt**
   - File structure visualization
   - Source code breakdown
   - Statistics

---

## 📦 Files & Locations

```
/servers/survival/plugins/
└── WardenMod-1.0.0.jar ................. Ready-to-use compiled plugin (16 KB)

/servers/survival/plugins/WardenMod/
├── src/main/java/com/shadowlord/wardenmod/
│   ├── WardenModPlugin.java ............. Main plugin class
│   ├── commands/
│   │   ├── WardenCommand.java
│   │   ├── ShriekerCommand.java
│   │   ├── SkulkCommand.java
│   │   └── SkulkSensorCommand.java
│   ├── entity/
│   │   └── Warden.java
│   └── listeners/
│       ├── WardenListener.java
│       ├── ShriekerListener.java
│       └── SkulkSensorListener.java
├── pom.xml ............................ Maven build config
├── plugin.yml ......................... Plugin manifest
└── Documentation:
    ├── QUICKSTART.md .................. Quick setup (30 sec)
    ├── README.md ..................... Technical docs
    ├── INSTALLATION.md ............... Setup guide
    ├── BUILD_SUMMARY.md .............. Build info
    ├── COMPLETE_GUIDE.md ............. Full reference
    ├── SOURCE_FILES.txt .............. File structure
    └── INDEX.md ...................... This file
```

---

## 🎮 Commands Quick Reference

| Command | Item | Permission | What It Does |
|---------|------|-----------|-------------|
| `/warden` | Warden Boss | wardenmod.warden | Spawn 500 HP Warden with 21 damage attacks |
| `/shrieker` | Shrieker | wardenmod.shrieker | Get Prismarine block with effects |
| `/skulk` | Skulk | wardenmod.skulk | Get Purple Wool block |
| `/skulksensor` | Sensor | wardenmod.skulksensor | Get Purpur Pillar block with effects |

**All require OP by default**

---

## ⚙️ Configuration Quick Reference

### Entity Stats (editable)

**Warden Health**
- File: `entity/Warden.java:13`
- Current: `500.0` HP (250 hearts)
- Type: Double

**Warden Damage**
- File: `listeners/WardenListener.java:62`
- Current: `21.0` damage per attack
- Type: Double

**Attack Cooldown**
- File: `entity/Warden.java:15`
- Current: `8` ticks
- Type: Integer

**Sensor Detection Range**
- File: `listeners/SkulkSensorListener.java:21`
- Current: `16.0` blocks
- Type: Double

### Permissions (in plugin.yml)

```yaml
wardenmod.warden       # Use /warden
wardenmod.shrieker     # Use /shrieker
wardenmod.skulk        # Use /skulk
wardenmod.skulksensor  # Use /skulksensor
```

---

## 🚀 Setup Checklist

- [ ] Verify JAR at `/servers/survival/plugins/WardenMod-1.0.0.jar`
- [ ] Restart server (systemctl restart survival-server or /reload confirm)
- [ ] Check console for "WardenMod enabled"
- [ ] Run `/plugins` command to verify loading
- [ ] Test `/warden` command
- [ ] Test other commands: `/shrieker`, `/skulk`, `/skulksensor`
- [ ] Verify permissions working (OP requirement)
- [ ] Optional: Customize stats if desired

---

## 📊 Statistics

### Code Metrics
- Total Java Classes: 9
- Total Lines of Code: ~450
- Commands Implemented: 4
- Event Listeners: 3
- Particle Systems: 3

### Build Information
- Compilation Status: ✓ SUCCESS
- JAR Size: 16 KB
- Java Version: 1.8+
- Spigot Version: 1.12.2
- Dependencies: None (Bukkit API only)

### Documentation
- Markdown Files: 6
- Total Documentation: ~45 KB
- Code Comments: Extensive

---

## 🎨 Features Summary

### Warden Boss
- Health: 500 HP (250 hearts)
- Damage: 21 per attack (10.5 hearts)
- Type: Iron Golem with custom properties
- Behavior: Attacks all nearby entities
- Effects: Particle explosions on attack
- Death: Explosive particles + smoke + magic + loot drops

### Shrieker Block
- Material: Prismarine (1.12.2 placeholder)
- Effects: Musical note particles on placement
- Use: Building Deep Dark atmosphere
- Features: Collectible, stackable, buildable

### Skulk Block
- Material: Purple Wool (1.12.2 placeholder)
- Color: Purple (durability 10)
- Use: Building deep dark structures
- Features: Fully buildable, authentic appearance

### Skulk Sensor Block
- Material: Purpur Pillar (1.12.2 placeholder)
- Detection: 16 block radius
- Effects: Redstone particles on activation
- Use: Create detection systems

---

## 🔧 Building from Source

### Prerequisites
- Maven 3.8.1+
- Java 1.8+
- Spigot 1.12.2 API (handled by Maven)

### Build Commands
```bash
cd /workspaces/EaglerServer-1.12/EaglerServer-1.12/servers/survival/plugins/WardenMod
mvn clean package
cp target/WardenMod-1.0.0.jar ../WardenMod-1.0.0.jar
```

### Build Output
- Location: `target/WardenMod-1.0.0.jar`
- Size: 16 KB
- Ready to deploy immediately

---

## 💡 Use Cases

### Boss Battle Arena
```
1. Use /warden to spawn boss
2. Players engage in combat
3. Boss uses 21 DMG attacks
4. Particle effects for immersion
5. Collect loot when defeated
```

### Building Deep Dark
```
1. Use /skulk to get blocks
2. Use /shrieker to get shriekers
3. Use /skulksensor to get sensors
4. Build structures with blocks
5. Use particle effects for ambiance
```

### Adventure Map
```
1. Place Wardens as bosses/challenges
2. Build with Skulk blocks for environment
3. Use Sensors for detection/triggers
4. Create immersive experiences
5. Challenge players through danger
```

---

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| Plugin won't load | Check Spigot 1.12.2, verify plugin.yml, check console |
| Commands don't work | Check OP status, verify reload, check /plugins |
| No permissions | Ensure OP or use permission plugin to assign |
| No particle effects | Check settings, effects more visible up close |
| Build fails | Clean Maven cache: `rm -rf ~/.m2/repository` |

See QUICKSTART.md for more troubleshooting.

---

## 🎯 Customization Guide

### Change Warden Health
```java
// File: entity/Warden.java, Line 13
private static final double WARDEN_MAX_HEALTH = 500.0;
// Change 500.0 to desired value
```

### Change Warden Damage
```java
// File: listeners/WardenListener.java, Line 62
event.setDamage(21.0);
// Change 21.0 to desired value
```

### Change Detection Range
```java
// File: listeners/SkulkSensorListener.java, Line 21
private static final double DETECTION_RANGE = 16.0;
// Change 16.0 to desired value
```

### Recompile After Changes
```bash
cd /workspaces/EaglerServer-1.12/EaglerServer-1.12/servers/survival/plugins/WardenMod
mvn clean package
cp target/WardenMod-1.0.0.jar ../WardenMod-1.0.0.jar
# Restart server to load new JAR
```

---

## 📝 File Descriptions

### Source Code Files

**WardenModPlugin.java**
- Entry point for the plugin
- Registers all commands
- Registers all event listeners
- ~25 lines

**WardenCommand.java**
- Handles `/warden` command
- Spawns Iron Golem with Warden properties
- ~40 lines

**ShriekerCommand.java**
- Handles `/shrieker` command
- Gives Prismarine block
- ~40 lines

**SkulkCommand.java**
- Handles `/skulk` command
- Gives Purple Wool block
- ~45 lines

**SkulkSensorCommand.java**
- Handles `/skulksensor` command
- Gives Purpur Pillar block
- ~40 lines

**Warden.java**
- Manages Warden entity properties
- Stores Iron Golem reference
- Handles health and cooldown
- ~60 lines

**WardenListener.java**
- Handles attack events
- Creates attack particles
- Creates death particles
- Manages entity tracking
- ~120 lines

**ShriekerListener.java**
- Handles block placement
- Creates placement effects
- ~40 lines

**SkulkSensorListener.java**
- Handles block placement
- Creates activation effects
- ~30 lines

### Configuration Files

**pom.xml**
- Maven build configuration
- Defines dependencies
- Configures compiler
- ~64 lines

**plugin.yml**
- Bukkit plugin manifest
- Defines commands
- Defines permissions
- ~35 lines

---

## 🔐 Permission Nodes

### Default Permissions
All permissions default to OP level.

### Permission List
```
wardenmod.warden       - Summon Warden
wardenmod.shrieker     - Get Shrieker
wardenmod.skulk        - Get Skulk
wardenmod.skulksensor  - Get Skulk Sensor
```

### Using Permission Plugins

**LuckPerms Example:**
```
/lp user [player] permission set wardenmod.warden true
```

**PermissionsEx Example:**
```
/pex user [player] add wardenmod.warden
```

---

## 📈 Version Information

- **Plugin Version**: 1.0.0
- **Spigot Version**: 1.12.2
- **Java Compatibility**: 1.8+
- **Build Date**: December 2, 2025
- **Author**: ShadowLord
- **Status**: Production Ready

---

## 🎁 Package Contents

✓ Compiled JAR (ready to deploy)
✓ Complete source code
✓ Maven build configuration
✓ Bukkit plugin manifest
✓ Comprehensive documentation
✓ Quick start guide
✓ Technical reference
✓ Installation guide
✓ Customization guide
✓ Full API reference

---

## 📞 Support & Documentation

1. **Quick Help**: See QUICKSTART.md
2. **Technical Details**: See README.md
3. **Installation**: See INSTALLATION.md
4. **Full Reference**: See COMPLETE_GUIDE.md
5. **File Structure**: See SOURCE_FILES.txt

---

## 🎉 Next Steps

1. Verify plugin location: `/servers/survival/plugins/WardenMod-1.0.0.jar`
2. Restart your server
3. Run `/warden` to test
4. Enjoy!

---

**This plugin is production-ready and fully documented. Start with QUICKSTART.md!**

**Version**: 1.0.0  
**Status**: ✓ Ready to Deploy  
**Date**: December 2, 2025
