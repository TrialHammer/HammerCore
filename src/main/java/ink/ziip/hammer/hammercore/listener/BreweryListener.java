package ink.ziip.hammer.hammercore.listener;

import com.dre.brewery.api.BreweryApi;
import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import com.dre.brewery.recipe.BRecipe;
import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import ink.ziip.hammer.hammercore.api.object.user.HammerUser;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import ink.ziip.hammer.hammercore.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class BreweryListener extends BaseListener {

    @EventHandler
    public void onPlayerBrew(BrewDrinkEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        HammerUser hammerUser = HammerUser.getUser(player);
        hammerUser.sendMessage(MessageManager.DRINKING_ALCOHOL_IS_BAD_FOR_YOUR_HEALTH);
        BRecipe recipe = event.getBrew().getCurrentRecipe();
        if (recipe == null) return;

        String qualityName = recipe.getName(event.getBrew().getQuality());
        if (qualityName.equals(ConfigManager.BREWERY_PREMIUM_MENG_PO_RECIPE)) {
            runConsole(ConfigManager.BREWERY_MENG_PO_BEND_REMOVE_COMMAND, player);
            runConsole(ConfigManager.BREWERY_MENG_PO_WARP_COMMAND, player);
            Bukkit.broadcastMessage(hammerUser.setPlaceholders(MessageManager.MENG_PO_SOUP_TIPS));
            return;
        }

        if (qualityName.equals(ConfigManager.BREWERY_WINE_RECIPE) && event.getItemMeta().hasLore()) {
            for (String lore : event.getItemMeta().getLore()) {
                if (Pattern.matches(ConfigManager.BREWERY_WINE_LORE_PATTERN, ChatColor.stripColor(lore))) {
                    runConsole(ConfigManager.BREWERY_WINE_REWARD_COMMAND, player);
                    return;
                }
            }
        }
    }

    private void runConsole(String template, Player player) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                template.replace("%player%", player.getName()));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CAULDRON
                && event.getClickedBlock().getType() != Material.WATER_CAULDRON) return;
        if (event.getItem() == null || event.getItem().getType() != Material.POTION) return;
        if (!BreweryApi.isBrew(event.getItem())) return;

        event.setCancelled(true);
        event.getItem().setType(Material.GLASS_BOTTLE);
        ItemStack itemStack = new ItemStack(Material.GLASS_BOTTLE);
        event.getItem().setItemMeta(itemStack.getItemMeta());
    }
}
