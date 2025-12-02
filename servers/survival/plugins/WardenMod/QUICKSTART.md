# ⚫ WardenMod - QUICK START

## 🎉 Installation Complete!

Your WardenMod plugin is **READY TO USE** on your Spigot 1.12.2 survival server.

---

## 📍 Plugin Location
```
/servers/survival/plugins/WardenMod-1.0.0.jar  ✓ (16 KB)
```

---

## ⚡ Quick Setup (30 seconds)

1. **Restart your server:**
   ```bash
   systemctl restart survival-server
   # OR use in-game: /reload confirm
   ```

2. **Verify plugin loaded:**
   ```
   In-game: /plugins
   Console should show: "WardenMod enabled"
   ```

3. **Start using:**
   ```
   /warden              → Spawn Warden boss!
   /shrieker            → Get Shrieker block
   /skulk               → Get Skulk block
   /skulksensor         → Get Skulk Sensor
   ```

---

## 🎮 Commands Cheat Sheet

| Command | What It Does | Permission |
|---------|-------------|-----------|
| `/warden` | Spawns a 500 HP Warden boss | wardenmod.warden |
| `/shrieker` | Gives Prismarine block | wardenmod.shrieker |
| `/skulk` | Gives Purple Wool block | wardenmod.skulk |
| `/skulksensor` | Gives Purpur Pillar block | wardenmod.skulksensor |

**All require OP by default** (or specific permission)

---

## 🖤 Warden Boss Stats

```
Health:          500 HP (250 hearts)
Damage/Attack:   21 (10.5 hearts)
Type:            Iron Golem with Warden powers
Attacks:         All entities nearby
Particles:       YES - Impressive effects!
Loot:            Drops on death
```

---

## ✨ What You Get

### Warden Boss (`/warden`)
- Summoned right in front of you
- Attacks with 21 damage
- Has 500 HP (super tough!)
- Amazing particle effects
- Drops rare loot when defeated

### Shrieker (`/shrieker`)
- Prismarine block
- Musical note particle effects
- Stack with other Shriekers
- Build aesthetic structures

### Skulk (`/skulk`)
- Purple Wool block
- Authentic deep dark appearance
- Fully buildable
- Collectible

### Skulk Sensor (`/skulksensor`)
- Purpur Pillar block
- Detects nearby movement (16 blocks)
- Red stone particle effects
- Can be chained for systems

---

## 🎯 Example Uses

### 1. Epic Boss Fight
```
/warden
(Combat!)
(See particle explosions)
(Collect loot)
```

### 2. Build Deep Dark Theme
```
/skulk
/shrieker
/skulksensor
(Build awesome structures)
(Use effects for ambiance)
```

### 3. Adventure Map Challenge
```
(Place Wardens around)
(Build with Skulk blocks)
(Use Sensors as traps)
(Players navigate danger)
```

---

## 📚 Documentation

- **README.md** - Technical documentation
- **INSTALLATION.md** - Detailed setup guide
- **BUILD_SUMMARY.md** - What's included
- **COMPLETE_GUIDE.md** - Full reference
- **SOURCE_FILES.txt** - File structure

---

## 🔧 Customize (Optional)

### Change Warden Health
Edit: `WardenMod/src/main/java/com/shadowlord/wardenmod/entity/Warden.java`
```java
private static final double WARDEN_MAX_HEALTH = 500.0;  // Change this
```

### Change Warden Damage
Edit: `WardenMod/src/main/java/com/shadowlord/wardenmod/listeners/WardenListener.java`
```java
event.setDamage(21.0);  // Change this
```

Then rebuild:
```bash
cd /workspaces/EaglerServer-1.12/EaglerServer-1.12/servers/survival/plugins/WardenMod
mvn clean package
cp target/WardenMod-1.0.0.jar ../WardenMod-1.0.0.jar
```

---

## ⚙️ Permissions (Optional)

If using a permission plugin like LuckPerms:

```
/lp user [player] permission set wardenmod.warden true
/lp user [player] permission set wardenmod.shrieker true
/lp user [player] permission set wardenmod.skulk true
/lp user [player] permission set wardenmod.skulksensor true
```

---

## 🆘 Troubleshooting

### Plugin won't load
- Check Spigot 1.12.2 is running
- Check console for errors
- Verify JAR is in `/plugins/` folder

### Commands don't work
- Are you OP? (`/op your_username`)
- Did you restart after installing?
- Try `/plugins` to confirm loading

### No particle effects
- Check your particle settings in-game
- Effects are more visible up close
- Some are subtle at distance

---

## 📊 What's Inside

```
✓ 9 Java classes (~450 lines)
✓ 4 Commands (Warden, Shrieker, Skulk, Sensor)
✓ 3 Event Listeners (Attack, Blocks)
✓ Multiple Particle Systems
✓ Full Documentation
✓ Source Code Included
```

---

## 🚀 You're All Set!

Your plugin is **ready to go**. Just restart your server and start using the commands!

**Enjoy the Warden!** 🖤⚫

---

## 📍 Next Steps

1. Restart server
2. Try `/warden` command
3. Fight the Warden boss
4. Build with Skulk blocks
5. Have fun!

---

**Questions?** Check the documentation files in the WardenMod folder.

**Version:** 1.0.0  
**Status:** ✓ Production Ready  
**Date:** December 2, 2025
