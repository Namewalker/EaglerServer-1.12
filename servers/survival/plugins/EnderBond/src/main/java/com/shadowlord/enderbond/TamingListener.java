package shadowlord.enderbond;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.UUID;

public class TamingListener implements Listener {

    private final EnderBond plugin;
    private final Random rnd = new Random();

    public TamingListener(EnderBond plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent ev) {
        if (!(ev.getRightClicked() instanceof Enderman)) return;
        Player p = ev.getPlayer();
        Enderman e = (Enderman) ev.getRightClicked();

        ItemStack inHand = p.getItemInHand();
        if (inHand == null || inHand.getType() != Material.CHORUS_FRUIT) return; // Ender Treat simplified

        // consume item
        if (inHand.getAmount() > 1) inHand.setAmount(inHand.getAmount() - 1);
        else p.setItemInHand(null);

        // attempt tame
        double chance = plugin.getConfig().getDouble("taming.success_chance", 0.3);
        if (rnd.nextDouble() <= chance) {
            // tame success
            UUID owner = p.getUniqueId();
            UUID eid = e.getUniqueId();
            plugin.getCompanionMap().put(owner, eid);
            plugin.getBondMap().put(owner, Math.max(plugin.getBondMap().getOrDefault(owner, 0), 10));
            e.setCustomName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("taming.companion_name", "&5Ender Companion")));
            e.setCustomNameVisible(true);
            e.setRemoveWhenFarAway(false);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have bonded with the Enderman! Bond +10."));
            e.getWorld().spawnParticle(org.bukkit.Particle.SPELL_WITCH, e.getLocation(), 20, 0.5,0.5,0.5,0.02);
        } else {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe Enderman resists your offering..."));
        }
    }
}
