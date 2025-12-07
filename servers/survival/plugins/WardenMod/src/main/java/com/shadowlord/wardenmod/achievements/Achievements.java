package com.shadowlord.wardenmod.achievements;

import org.bukkit.entity.Player;
import org.bukkit.Statistic;

/**
 * Achievement system for WardenMod
 * Tracks player accomplishments with the Warden and related items
 */
public class Achievements {
    
    /**
     * Award Sneak 100 achievement
     * Given when player crouches while holding Skulk or Skulk Sensor
     */
    public static void giveSneak100(Player player) {
        if (!hasAchievement(player, "sneak_100")) {
            player.incrementStatistic(Statistic.SNEAK_TIME, 1);
            player.sendMessage("§6✓ Achievement Unlocked: §eSneak 100");
            player.sendMessage("§7You mastered the art of stealth with Skulk blocks");
            setAchievement(player, "sneak_100", true);
        }
    }
    
    /**
     * Award "Warden Slayer" achievement
     * Given when player defeats a Warden
     */
    public static void giveWardenSlayer(Player player) {
        if (!hasAchievement(player, "warden_slayer")) {
            player.sendMessage("§6✓ Achievement Unlocked: §cWarden Slayer");
            player.sendMessage("§7You have defeated the mighty Warden!");
            setAchievement(player, "warden_slayer", true);
        }
    }
    
    /**
     * Award "Deep Dark Explorer" achievement
     * Given when player collects all three Skulk-related items
     */
    public static void giveDeepDarkExplorer(Player player) {
        if (!hasAchievement(player, "deep_dark_explorer")) {
            player.sendMessage("§6✓ Achievement Unlocked: §9Deep Dark Explorer");
            player.sendMessage("§7You have gathered all Deep Dark blocks!");
            setAchievement(player, "deep_dark_explorer", true);
        }
    }
    
    /**
     * Award "Shrieker's Warning" achievement
     * Given when player places a Shrieker block
     */
    public static void giveShriekersWarning(Player player) {
        if (!hasAchievement(player, "shriekers_warning")) {
            player.sendMessage("§6✓ Achievement Unlocked: §5Shrieker's Warning");
            player.sendMessage("§7You have placed the Shrieker block!");
            setAchievement(player, "shriekers_warning", true);
        }
    }
    
    /**
     * Award "Vibration Detector" achievement
     * Given when player places a Skulk Sensor
     */
    public static void giveVibrationDetector(Player player) {
        if (!hasAchievement(player, "vibration_detector")) {
            player.sendMessage("§6✓ Achievement Unlocked: §aVibration Detector");
            player.sendMessage("§7You have placed the Skulk Sensor!");
            setAchievement(player, "vibration_detector", true);
        }
    }
    
    /**
     * Award "Block Cultist" achievement
     * Given when player places 10 Skulk blocks
     */
    public static void giveBlockCultist(Player player) {
        if (!hasAchievement(player, "block_cultist")) {
            player.sendMessage("§6✓ Achievement Unlocked: §8Block Cultist");
            player.sendMessage("§7You have placed 10 Skulk blocks!");
            setAchievement(player, "block_cultist", true);
        }
    }
    
    /**
     * Award "Sonic Boom Survivor" achievement
     * Given when player survives a Warden attack with low health
     */
    public static void giveSonicBoomSurvivor(Player player) {
        if (!hasAchievement(player, "sonic_boom_survivor")) {
            player.sendMessage("§6✓ Achievement Unlocked: §6Sonic Boom Survivor");
            player.sendMessage("§7You survived a Warden's sonic boom!");
            setAchievement(player, "sonic_boom_survivor", true);
        }
    }
    
    /**
     * Award "Warden Hunter" achievement
     * Given when player defeats 5 Wardens
     */
    public static void giveWardenHunter(Player player) {
        if (!hasAchievement(player, "warden_hunter")) {
            player.sendMessage("§6✓ Achievement Unlocked: §cWarden Hunter");
            player.sendMessage("§7You have defeated 5 Wardens!");
            setAchievement(player, "warden_hunter", true);
        }
    }
    
    /**
     * Award "Legendary Collector" achievement
     * Given when player collects a Warden skull drop
     */
    public static void giveLegendaryCollector(Player player) {
        if (!hasAchievement(player, "legendary_collector")) {
            player.sendMessage("§6✓ Achievement Unlocked: §bLegendary Collector");
            player.sendMessage("§7You have collected a Warden skull!");
            setAchievement(player, "legendary_collector", true);
        }
    }
    
    // Helper methods for achievement tracking
    private static void setAchievement(Player player, String achievementId, boolean unlocked) {
        // Store achievement in player metadata
        player.setMetadata("achievement_" + achievementId, 
            new org.bukkit.metadata.FixedMetadataValue(
                org.bukkit.Bukkit.getPluginManager().getPlugin("WardenMod"), 
                unlocked
            )
        );
    }
    
    private static boolean hasAchievement(Player player, String achievementId) {
        return player.hasMetadata("achievement_" + achievementId) &&
               player.getMetadata("achievement_" + achievementId).get(0).asBoolean();
    }
}
