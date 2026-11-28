package ink.ziip.hammer.hammercore.feature.nexo;

import com.nexomc.libs.gui.guis.Gui;
import com.nexomc.libs.gui.guis.GuiItem;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.recipes.CustomRecipe;
import com.nexomc.nexo.recipes.RecipesManager;
import com.nexomc.nexo.utils.inventories.NexoInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** A read-only, one-level category browser backed by Nexo's live item and recipe tables. */
final class NexoRecipeCatalog implements CommandExecutor {
    private static final int PAGE_SIZE = 45;
    private static final int PREVIOUS = 45;
    private static final int BACK = 49;
    private static final int NEXT = 53;
    private final JavaPlugin core;
    private final Path itemsDirectory;
    private final Method createPaperInventory;

    NexoRecipeCatalog(JavaPlugin core, File nexoDirectory) {
        this.core = core;
        this.itemsDirectory = nexoDirectory.toPath().toAbsolutePath().normalize().resolve("items");
        try {
            // This optional integration runs on Paper, but the main project compiles against
            // the Spigot API. Preserve Nexo's Adventure font styles in the inventory title.
            createPaperInventory = Bukkit.class.getMethod("createInventory", InventoryHolder.class, int.class, Component.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Paper's Adventure inventory factory is unavailable", e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("请在游戏内打开配方目录。");
            return true;
        }
        String filter = String.join("_", args).toLowerCase(Locale.ROOT);
        List<Category> categories = categories(player, filter);
        if (categories.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + (filter.isEmpty() ? "没有可查看的自定义物品配方。" : "没有找到匹配且有配方的物品。"));
            return true;
        }
        openCategories(player, categories, 0);
        return true;
    }

    List<Category> categories(Player player, String filter) {
        Map<String, List<CustomRecipe>> recipes = new HashMap<>();
        RecipesManager.INSTANCE.getRecipes().values().stream()
                .filter(recipe -> recipe.hasPermission(player))
                .sorted(Comparator.comparing(recipe -> recipe.getKey().asString()))
                .forEach(recipe -> {
                    String id = NexoItems.idFromItem(recipe.getResult());
                    if (id != null) recipes.computeIfAbsent(id, ignored -> new ArrayList<>()).add(recipe);
                });
        ConfigurationSection layout = NexoInventory.LAYOUT.toSection();
        List<Category> categories = new ArrayList<>();
        for (File file : NexoItems.itemMap().keySet()) {
            // Each existing Nexo item file is a category, regardless of its depth on disk.
            String name = file.getName();
            int extension = name.lastIndexOf('.');
            String key = extension == -1 ? name : name.substring(0, extension);
            ConfigurationSection settings = categorySettings(layout, file.toPath(), key);
            String title = settings == null ? null : settings.getString("itemname");
            Component label = title == null ? Component.text(key, NamedTextColor.GREEN) : MiniMessage.miniMessage().deserialize(title);
            String icon = settings == null ? null : settings.getString("icon");
            int slot = settings == null ? Integer.MAX_VALUE : settings.getInt("slot", Integer.MAX_VALUE);
            List<Entry> entries = NexoItems.unexcludedItems(file).stream()
                    .map(NexoItems::idFromItem)
                    .filter(Objects::nonNull)
                    .distinct()
                    .filter(recipes::containsKey)
                    .filter(id -> matches(filter, key, title, id))
                    .map(id -> new Entry(id, List.copyOf(recipes.get(id))))
                    .toList();
            if (!entries.isEmpty()) categories.add(new Category(file.getPath(), label, icon, slot, entries));
        }
        categories.sort(Comparator.comparingInt(Category::slot).thenComparing(Category::path));
        return List.copyOf(categories);
    }

    private ConfigurationSection categorySettings(ConfigurationSection layout, Path file, String key) {
        if (layout == null) return null;
        Path absolute = file.toAbsolutePath().normalize();
        if (absolute.startsWith(itemsDirectory)) {
            Path parent = itemsDirectory.relativize(absolute).getParent();
            if (parent != null) {
                String path = parent.toString().replace(File.separatorChar, '.') + "." + key;
                ConfigurationSection specific = layout.getConfigurationSection(path);
                if (specific != null) return specific;
            }
        }
        return layout.getConfigurationSection(key);
    }

    private static boolean matches(String filter, String key, String title, String id) {
        return filter.isEmpty() || id.toLowerCase(Locale.ROOT).contains(filter)
                || key.toLowerCase(Locale.ROOT).contains(filter)
                || (title != null && title.toLowerCase(Locale.ROOT).contains(filter));
    }

    private void openCategories(Player player, List<Category> categories, int page) {
        Gui gui = newCatalogGui();
        int start = page * PAGE_SIZE;
        for (int i = start; i < Math.min(categories.size(), start + PAGE_SIZE); i++) {
            Category category = categories.get(i);
            String iconId = normalizeIcon(category.icon());
            if (iconId == null) iconId = category.entries().getFirst().id();
            ItemStack icon = named(iconId, Material.CHEST, category.label(),
                    Component.text(category.entries().size() + " 种有配方的物品 · 点击浏览", NamedTextColor.GRAY));
            gui.setItem(i - start, item(icon, p -> openItems(p, categories, page, category, 0)));
        }
        addPagination(gui, page, categories.size(), newPage -> openCategories(player, categories, newPage));
        gui.setItem(BACK, item(button(NexoInventory.EXIT_ICON, Material.BARRIER, "关闭目录"), Player::closeInventory));
        gui.open(player);
    }

    private void openItems(Player player, List<Category> categories, int rootPage, Category category, int page) {
        Gui gui = newCatalogGui();
        List<Entry> entries = category.entries();
        int start = page * PAGE_SIZE;
        for (int i = start; i < Math.min(entries.size(), start + PAGE_SIZE); i++) {
            Entry entry = entries.get(i);
            var builder = NexoItems.itemFromId(entry.id());
            if (builder == null) continue;
            var icon = new ItemBuilder(builder);
            var lore = new ArrayList<>(icon.lore());
            lore.add(Component.text("点击查看配方（" + entry.recipes().size() + "）", NamedTextColor.GRAY));
            gui.setItem(i - start, item(icon.lore(lore).build(),
                    p -> openRecipe(p, entry.recipes(), 0, who -> openItems(who, categories, rootPage, category, page))));
        }
        addPagination(gui, page, entries.size(), newPage -> openItems(player, categories, rootPage, category, newPage));
        gui.setItem(BACK, item(button(NexoInventory.EXIT_ICON, Material.BARRIER, "返回分类"),
                p -> openCategories(p, categories, rootPage)));
        gui.open(player);
    }

    Gui recipeGui(List<CustomRecipe> recipes, int page, Consumer<Player> back) {
        var preview = new NexoCatalogRecipeView(recipes.get(page));
        Gui gui = newGui(preview.title());
        preview.populate(gui);
        gui.setItem(NexoCatalogRecipeView.BACK, item(named(null, Material.ARROW,
                Component.text("返回物品目录", NamedTextColor.YELLOW),
                Component.text("返回打开这张配方前的物品列表", NamedTextColor.GRAY)), back));
        if (preview.hasLabeledBackButton()) {
            gui.addSlotAction(NexoCatalogRecipeView.BACK + 1, event -> runAfterClick(event, back));
            gui.addSlotAction(NexoCatalogRecipeView.BACK + 2, event -> runAfterClick(event, back));
        }
        if (page > 0) {
            gui.setItem(NexoCatalogRecipeView.PREVIOUS, item(button(NexoInventory.PREVIOUS_ICON, Material.ARROW, "上一种配方"),
                    p -> openRecipe(p, recipes, page - 1, back)));
        }
        if (page + 1 < recipes.size()) {
            gui.setItem(NexoCatalogRecipeView.NEXT, item(button(NexoInventory.NEXT_ICON, Material.ARROW, "下一种配方"),
                    p -> openRecipe(p, recipes, page + 1, back)));
        }
        return gui;
    }

    private void openRecipe(Player player, List<CustomRecipe> recipes, int page, Consumer<Player> back) {
        List<CustomRecipe> allowed = recipes.stream().filter(recipe -> recipe.hasPermission(player)).toList();
        if (allowed.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "没有可查看的配方。");
            back.accept(player);
            return;
        }
        recipeGui(allowed, Math.min(page, allowed.size() - 1), back).open(player);
    }

    private Gui newCatalogGui() {
        return newGui(NexoInventory.TITLE.toComponent());
    }

    private Gui newGui(Component title) {
        Gui gui = Gui.gui().rows(6).title(title)
                .inventory(this::paperInventory).create();
        lock(gui);
        return gui;
    }

    private Inventory paperInventory(Component title, InventoryHolder holder, int size) {
        try {
            return (Inventory) createPaperInventory.invoke(null, holder, size, title);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not open Nexo's styled catalogue inventory", e);
        }
    }

    private static void lock(Gui gui) {
        gui.disableAllInteractions();
        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.setDragAction(event -> event.setCancelled(true));
    }

    private void addPagination(Gui gui, int page, int size, java.util.function.IntConsumer open) {
        if (page > 0) {
            gui.setItem(PREVIOUS, item(button(NexoInventory.PREVIOUS_ICON, Material.ARROW, "上一页"), p -> open.accept(page - 1)));
        }
        if ((page + 1) * PAGE_SIZE < size) {
            gui.setItem(NEXT, item(button(NexoInventory.NEXT_ICON, Material.ARROW, "下一页"), p -> open.accept(page + 1)));
        }
    }

    private GuiItem item(ItemStack stack, Consumer<Player> action) {
        return new GuiItem(stack, event -> runAfterClick(event, action));
    }

    private void runAfterClick(InventoryClickEvent event, Consumer<Player> action) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || !event.getClick().isMouseClick()
                || event.isShiftClick()) return;
        var current = event.getView().getTopInventory();
        core.getServer().getScheduler().runTask(core, () -> {
            if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(current)) action.accept(player);
        });
    }

    private static ItemStack button(NexoInventory key, Material fallback, String name) {
        return named(normalizeIcon(key.toString()), fallback, Component.text(name, NamedTextColor.YELLOW));
    }

    private static String normalizeIcon(String id) {
        return id != null && id.startsWith("nexo:") ? id.substring("nexo:".length()) : id;
    }

    private static ItemStack named(String id, Material fallback, Component name, Component... lore) {
        var builder = id == null ? null : NexoItems.itemFromId(id);
        var icon = builder == null ? new ItemBuilder(fallback) : new ItemBuilder(builder);
        return icon.displayName(name).lore(lore).build();
    }

    record Entry(String id, List<CustomRecipe> recipes) {}

    record Category(String path, Component label, String icon, int slot, List<Entry> entries) {}
}
