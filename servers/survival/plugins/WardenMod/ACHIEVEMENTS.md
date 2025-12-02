# 🏆 WardenMod Achievements System

## Overview

The WardenMod plugin now includes **8 unique achievements** that players can unlock by interacting with Warden-related content. All achievements are tracked automatically and announce when unlocked!

---

## 🎖️ Achievements List

### 1. **Sneak 100** (Stealth Master)
```
Description: Crouch while holding a Skulk item
How to Get:  Equip Skulk, Shrieker, or Skulk Sensor block, then press SHIFT
Color: §6 (Gold)
Message: "You mastered the art of stealth with Skulk blocks"
```

### 2. **Warden Slayer** (Combat)
```
Description: Defeat a Warden boss
How to Get:  Kill any Warden spawned with /warden command
Color: §c (Red)
Message: "You have defeated the mighty Warden!"
Effect: Unique color highlighting
```

### 3. **Warden Hunter** (Hardcore Combat)
```
Description: Defeat 5 Wardens
How to Get:  Kill 5 different Wardens (cumulative)
Color: §c (Red)
Message: "You have defeated 5 Wardens!"
Unlocks After: 5 Warden kills
```

### 4. **Sonic Boom Survivor** (Survival)
```
Description: Survive a Warden's sonic boom attack with low health
How to Get:  Get hit by a Warden when you have 0.5-5 health remaining
Color: §6 (Gold)
Message: "You survived a Warden's sonic boom!"
Trigger: Health between 0.5 and 5 HP after Warden attack
```

### 5. **Deep Dark Explorer** (Collection)
```
Description: Collect all three Deep Dark block types
How to Get:  Have Shrieker, Skulk, and Skulk Sensor in inventory simultaneously
Color: §9 (Blue)
Message: "You have gathered all Deep Dark blocks!"
Items Needed: Prismarine, Purple Wool, Purpur Pillar
```

### 6. **Shrieker's Warning** (Block Placement)
```
Description: Place your first Shrieker block
How to Get:  Use /shrieker to get the block, then place it
Color: §5 (Purple)
Message: "You have placed the Shrieker block!"
Visual: Note particles when placed
```

### 7. **Vibration Detector** (Block Placement)
```
Description: Place your first Skulk Sensor block
How to Get:  Use /skulksensor to get the block, then place it
Color: §a (Green)
Message: "You have placed the Skulk Sensor!"
Visual: Redstone particles when placed
```

### 8. **Block Cultist** (Builder)
```
Description: Place 10 Skulk blocks
How to Get:  Use /skulk to get blocks, place them 10 times
Color: §8 (Dark Gray)
Message: "You have placed 10 Skulk blocks!"
Counter: Displays current count on placement
```

### 9. **Legendary Collector** (Rare Drops)
```
Description: Collect a Warden skull drop
How to Get:  Defeat a Warden and pick up the skull item it drops
Color: §b (Aqua)
Message: "You have collected a Warden skull!"
Item: Skull item (dropped by Warden on death)
```

---

## 📊 Achievement Stats

| Achievement | Type | Difficulty | Trigger |
|-------------|------|-----------|---------|
| Sneak 100 | Stealth | Easy | Press SHIFT with item |
| Warden Slayer | Combat | Medium | Kill 1 Warden |
| Warden Hunter | Combat | Hard | Kill 5 Wardens |
| Sonic Boom Survivor | Survival | Hard | Survive low-HP hit |
| Deep Dark Explorer | Collection | Medium | Have all 3 blocks |
| Shrieker's Warning | Building | Easy | Place Shrieker |
| Vibration Detector | Building | Easy | Place Sensor |
| Block Cultist | Building | Medium | Place 10 Skulks |
| Legendary Collector | Collecting | Medium | Pick up skull |

---

## 🎯 How Achievements Work

### Automatic Tracking
- Achievements are tracked automatically in player metadata
- Each player's achievements are stored individually
- Once unlocked, cannot be re-unlocked (one-time only)

### Notifications
When an achievement is unlocked:
1. **Chat Message**: Gold-colored "Achievement Unlocked" message
2. **Description**: Gold-colored achievement description
3. **Color**: Each achievement has a unique color
4. **Sound**: Server announces to nearby players

### Achievement Classes
All achievements are defined in `achievements/Achievements.java`:
- Each achievement has a unique ID
- Checksum prevents double-awarding
- Player metadata stores completion status

---

## 💻 Technical Implementation

### File Structure
```
WardenMod/src/main/java/com/shadowlord/wardenmod/
├── achievements/
│   └── Achievements.java (Achievement definitions)
├── listeners/
│   ├── WardenListener.java (Combat achievements)
│   ├── ShriekerListener.java (Shrieker achievement)
│   ├── SkulkSensorListener.java (Sensor achievement)
│   ├── SkulkListener.java (Skulk & collection achievements)
│   └── SneakListener.java (Sneak 100 achievement)
└── WardenModPlugin.java (Registers all listeners)
```

### Achievement Tracking Methods

**Method 1: Player Metadata**
```java
player.setMetadata("achievement_[id]", 
    new FixedMetadataValue(plugin, true)
);
```

**Method 2: Kill Counter**
```java
player.setMetadata("warden_kills", 
    new FixedMetadataValue(plugin, killCount)
);
```

**Method 3: Block Counter**
```java
player.setMetadata("skulk_blocks_placed", 
    new FixedMetadataValue(plugin, blockCount)
);
```

---

## 🎮 Player Interaction Flow

### Obtaining Achievements

**Sneak 100:**
```
1. /skulk → get Purple Wool block
2. Hold in hand
3. Press SHIFT to crouch
4. ✓ Achievement unlocked!
```

**Warden Slayer:**
```
1. /warden → spawn Warden boss
2. Fight and defeat the Warden
3. ✓ Achievement unlocked!
```

**Block Cultist:**
```
1. /skulk → get Skulk block
2. Place block (1/10)
3. Get more skulks: /skulk
4. Place blocks until 10/10
5. ✓ Achievement unlocked!
```

**Deep Dark Explorer:**
```
1. /skulk → get Skulk
2. /shrieker → get Shrieker
3. /skulksensor → get Sensor
4. Have all 3 in inventory
5. Pick up any item
6. ✓ Achievement unlocked!
```

---

## 📈 Achievement Progression

### Easy Tier (5 minutes)
- Sneak 100
- Shrieker's Warning
- Vibration Detector

### Medium Tier (15-30 minutes)
- Warden Slayer
- Deep Dark Explorer
- Block Cultist
- Legendary Collector

### Hard Tier (1+ hours)
- Warden Hunter (requires 5 kills)
- Sonic Boom Survivor (requires specific conditions)

---

## 🔧 Customization

### Adding New Achievements

Edit `achievements/Achievements.java`:

```java
public static void giveCustomAchievement(Player player) {
    if (!hasAchievement(player, "custom_id")) {
        player.sendMessage("§6✓ Achievement Unlocked: §eCustom Achievement");
        player.sendMessage("§7Your achievement description");
        setAchievement(player, "custom_id", true);
    }
}
```

Then call in appropriate listener:
```java
Achievements.giveCustomAchievement(player);
```

### Color Codes Used
- §6 = Gold (primary)
- §c = Red (combat)
- §5 = Purple (shrieker)
- §a = Green (sensors)
- §9 = Blue (collection)
- §b = Aqua (legendary)
- §8 = Dark Gray (builder)

---

## 📝 Achievement Messages

### Sneak 100
```
§6✓ Achievement Unlocked: §eSneak 100
§7You mastered the art of stealth with Skulk blocks
```

### Warden Slayer
```
§6✓ Achievement Unlocked: §cWarden Slayer
§7You have defeated the mighty Warden!
```

### Warden Hunter
```
§6✓ Achievement Unlocked: §cWarden Hunter
§7You have defeated 5 Wardens!
```

### Sonic Boom Survivor
```
§6✓ Achievement Unlocked: §6Sonic Boom Survivor
§7You survived a Warden's sonic boom!
```

### Deep Dark Explorer
```
§6✓ Achievement Unlocked: §9Deep Dark Explorer
§7You have gathered all Deep Dark blocks!
```

### Shrieker's Warning
```
§6✓ Achievement Unlocked: §5Shrieker's Warning
§7You have placed the Shrieker block!
```

### Vibration Detector
```
§6✓ Achievement Unlocked: §aVibration Detector
§7You have placed the Skulk Sensor!
```

### Block Cultist
```
§6✓ Achievement Unlocked: §8Block Cultist
§7You have placed 10 Skulk blocks!
```

### Legendary Collector
```
§6✓ Achievement Unlocked: §bLegendary Collector
§7You have collected a Warden skull!
```

---

## 🎯 Achievement Unlock Triggers

| Achievement | Trigger Event | Condition |
|-------------|---------------|-----------|
| Sneak 100 | PlayerToggleSneakEvent | Holding Skulk-related item |
| Warden Slayer | EntityDeathEvent | Iron Golem (Warden) entity |
| Warden Hunter | EntityDeathEvent | 5+ Warden kills tracked |
| Sonic Boom Survivor | EntityDamageByEntityEvent | Player health 0.5-5 HP |
| Deep Dark Explorer | PlayerPickupItemEvent | All 3 items in inventory |
| Shrieker's Warning | BlockPlaceEvent | Prismarine block |
| Vibration Detector | BlockPlaceEvent | Purpur Pillar block |
| Block Cultist | BlockPlaceEvent | 10+ Purple Wool blocks |
| Legendary Collector | PlayerPickupItemEvent | Skull_Item pickup |

---

## 📊 Achievement Statistics

### By Category
- **Combat**: 3 achievements (Slayer, Hunter, Survivor)
- **Building**: 3 achievements (Shrieker, Sensor, Cultist)
- **Collection**: 2 achievements (Explorer, Collector)
- **Stealth**: 1 achievement (Sneak 100)

### By Difficulty
- **Easy**: 3 achievements (< 5 min)
- **Medium**: 4 achievements (5-30 min)
- **Hard**: 2 achievements (30+ min)

### By Time to Complete
- **Instant**: Sneak 100, Shrieker, Sensor
- **Minutes**: Warden Slayer, Explorer, Collector
- **Hours**: Block Cultist, Warden Hunter

---

## 💡 Tips for Players

1. **Get Sneak 100 First**: Easiest achievement, just crouch with the items
2. **Collect All Items**: Having all 3 blocks gives you Deep Dark Explorer
3. **Farm Wardens**: Kill multiple Wardens to get Hunter achievement
4. **Collect Skulls**: Each Warden death gives you a skull to collect
5. **Build with Skulk**: Place 10 Skulk blocks for Block Cultist
6. **Take Strategic Damage**: Get hit when you have low health for Sonic Boom Survivor

---

## 🔄 Updates & Future

### Planned Achievements (Future)
- Warden Master (defeat 10 Wardens)
- Deep Dark Architect (place 50 Deep Dark blocks)
- Artifact Collector (collect 5 Warden skulls)

### Customization Options
- Achievement names are customizable
- Messages can be modified in Achievements.java
- Colors can be changed easily
- New achievements can be added without recompiling (with custom system)

---

## 🐛 Troubleshooting

### Achievement Won't Unlock
- Ensure conditions are met exactly
- Check player is in correct game mode
- Verify player metadata is enabled
- Restart server and try again

### Double-Unlock Prevention
- Each achievement checks with `hasAchievement()` first
- Metadata prevents re-unlocking
- Message only sends once per achievement

### Tracking Issues
- Player metadata persists across sessions
- Achievements stored until server restart
- Consider persistent storage for long-term tracking

---

## 📚 Related Files

- **achievements/Achievements.java** - All achievement definitions
- **listeners/WardenListener.java** - Combat achievements
- **listeners/ShriekerListener.java** - Shrieker placement
- **listeners/SkulkListener.java** - Skulk & collection tracking
- **listeners/SkulkSensorListener.java** - Sensor placement
- **listeners/SneakListener.java** - Sneak 100 achievement
- **WardenModPlugin.java** - Main plugin loader

---

## 🎉 Achievement Unlocked Guide

**Complete Guide to Unlocking All 9 Achievements:**

```
1. Sneak 100
   └─ /skulk → hold → SHIFT ✓ (2 minutes)

2. Shrieker's Warning
   └─ /shrieker → place ✓ (1 minute)

3. Vibration Detector
   └─ /skulksensor → place ✓ (1 minute)

4. Warden Slayer
   └─ /warden → defeat ✓ (5-10 minutes)

5. Sonic Boom Survivor
   └─ Get to <5 HP → Get hit by Warden ✓ (during Slayer fight)

6. Deep Dark Explorer
   └─ Collect all 3 blocks → pick up item ✓ (5 minutes)

7. Legendary Collector
   └─ Defeat Warden → pick up skull ✓ (during Slayer fight)

8. Block Cultist
   └─ /skulk × 10 → place all ✓ (10 minutes)

9. Warden Hunter
   └─ /warden × 5 → defeat all ✓ (1+ hour)

Total Time: ~2 hours for all achievements
```

---

**Version**: 1.0.0  
**Status**: ✓ All Achievements Active  
**Last Updated**: December 2, 2025
