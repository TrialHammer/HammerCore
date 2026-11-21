package ink.ziip.hammer.hammercore.listener;

import com.dre.brewery.Brew;
import com.dre.brewery.api.BreweryApi;
import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import com.dre.brewery.recipe.BRecipe;
import ink.ziip.hammer.hammercore.HammerCore;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class BreweryListener extends BaseListener {

    /**
     * BreweryX stores recipe names verbatim from {@code recipes.yml}. Server owners
     * commonly prefix them with colour codes (e.g. {@code &6优质孟婆汤}) and may
     * define three quality variants separated by {@code /}. Comparing the raw
     * value against a plain-text config value silently never matched, so the
     * configured commands were never dispatched.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerBrew(BrewDrinkEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        HammerUser hammerUser = HammerUser.getUser(player);
        hammerUser.sendMessage(MessageManager.DRINKING_ALCOHOL_IS_BAD_FOR_YOUR_HEALTH);

        Brew brew = event.getBrew();
        if (brew == null) return;
        BRecipe recipe = brew.getCurrentRecipe();
        if (recipe == null) {
            debug("Brew drank by " + player.getName() + " has no resolvable recipe ("
                    + BreweryApi.getRecipeName(brew) + ")");
            return;
        }

        if (matches(recipe, ConfigManager.BREWERY_MENG_PO_RECIPE_ID,
                ConfigManager.BREWERY_PREMIUM_MENG_PO_RECIPE)) {
            runConsole(ConfigManager.BREWERY_MENG_PO_BEND_REMOVE_COMMAND, player);
            runConsole(ConfigManager.BREWERY_MENG_PO_WARP_COMMAND, player);
            Bukkit.broadcastMessage(hammerUser.setPlaceholders(MessageManager.MENG_PO_SOUP_TIPS));
            return;
        }

        if (matches(recipe, ConfigManager.BREWERY_WINE_RECIPE_ID, ConfigManager.BREWERY_WINE_RECIPE)
                && event.getItemMeta() != null && event.getItemMeta().hasLore()) {
            for (String lore : event.getItemMeta().getLore()) {
                String stripped = ChatColor.stripColor(lore);
                if (stripped != null && ConfigManager.BREWERY_WINE_LORE_PATTERN != null
                        && Pattern.matches(ConfigManager.BREWERY_WINE_LORE_PATTERN, stripped)) {
                    runConsole(ConfigManager.BREWERY_WINE_REWARD_COMMAND, player);
                    return;
                }
            }
        }

        debug("Brew by " + player.getName() + " matched no HammerCore recipe: id=" + recipe.getId()
                + ", names=" + String.join("|", recipeNames(recipe)));
    }

    /**
     * BreweryX keeps colour codes and quality variants in the recipe name array.
     * Strip formatting, trim and compare case-insensitively against every
     * configured name, and also allow matching by the {@code recipes.yml} id.
     */
    static boolean matches(BRecipe recipe, String configuredId, String... configuredNames) {
        if (configuredId != null && !configuredId.isBlank()
                && recipe.getId() != null && recipe.getId().equalsIgnoreCase(configuredId.trim())) {
            return true;
        }

        List<String> wanted = new ArrayList<>();
        for (String name : configuredNames) {
            String normalized = normalize(name);
            if (!normalized.isEmpty()) {
                wanted.add(normalized);
            }
        }
        if (wanted.isEmpty()) {
            return false;
        }

        for (String candidate : recipeNames(recipe)) {
            String normalized = normalize(candidate);
            for (String name : wanted) {
                if (normalized.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Every name variant the recipe declares, including the middle "recipe name". */
    static List<String> recipeNames(BRecipe recipe) {
        List<String> names = new ArrayList<>();
        String[] declared = recipe.getName();
        if (declared != null) {
            for (String name : declared) {
                if (name != null && !name.isBlank()) {
                    names.add(name);
                }
            }
        }
        String recipeName = recipe.getRecipeName();
        if (recipeName != null && !recipeName.isBlank() && !names.contains(recipeName)) {
            names.add(recipeName);
        }
        return names;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String stripped = ChatColor.stripColor(value);
        return (stripped == null ? value : stripped).trim().toLowerCase(Locale.ROOT);
    }

    private void runConsole(String template, Player player) {
        if (template == null || template.isBlank()) {
            return;
        }
        String command = template
                .replace("%player_name%", player.getName())
                .replace("%player%", player.getName())
                .trim();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        final String finalCommand = command;
        // Run after the consume event so plugins handling /bend or /warp are not
        // re-entered while BreweryX is still processing the drink.
        Bukkit.getScheduler().runTask(HammerCore.getInstance(), () -> {
            try {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
            } catch (RuntimeException exception) {
                HammerCore.getInstance().getLogger().warning(
                        "Failed to run brewery command '" + finalCommand + "': " + exception.getMessage());
            }
        });
    }

    private void debug(String message) {
        if (Boolean.TRUE.equals(ConfigManager.SETTINGS_DEBUG)) {
            HammerCore.getInstance().getLogger().info("[brewery] " + message);
        }
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
