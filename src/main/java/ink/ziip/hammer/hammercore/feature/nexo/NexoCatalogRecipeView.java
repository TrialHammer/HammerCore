package ink.ziip.hammer.hammercore.feature.nexo;

import com.nexomc.libs.gui.guis.Gui;
import com.nexomc.libs.gui.guis.GuiItem;
import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.recipes.CustomCookingRecipe;
import com.nexomc.nexo.recipes.CustomRecipe;
import com.nexomc.nexo.recipes.RecipeIngredient;
import com.nexomc.nexo.recipes.RecipeType;
import com.nexomc.nexo.utils.AdventureUtils;
import com.nexomc.nexo.utils.inventories.NexoInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

/** Draw the live Nexo recipe without using its generic crafting-grid showcase for cooking. */
final class NexoCatalogRecipeView {
    static final int BACK = 39;
    static final int PREVIOUS = 36;
    static final int NEXT = 44;
    private static final String RECIPE_GLYPH = "innova_catalog_recipe";
    private static final String COOKING_GLYPH = "innova_catalog_cooking";
    private final CustomRecipe recipe;
    private final boolean customLayout;
    private final String glyphId;

    NexoCatalogRecipeView(CustomRecipe recipe) {
        this.recipe = recipe;
        glyphId = recipe instanceof CustomCookingRecipe ? COOKING_GLYPH : RECIPE_GLYPH;
        customLayout = NexoPlugin.instance().fontManager().glyphFromID(glyphId) != null;
    }

    Component title() {
        return customLayout ? AdventureUtils.INSTANCE.getMINI_MESSAGE().deserialize("<shift:-8><glyph:" + glyphId + ">")
                : NexoInventory.RECIPE_SHOWCASE_TITLE.toComponent();
    }

    boolean hasLabeledBackButton() {
        return customLayout;
    }

    void populate(Gui gui) {
        var station = stationFor(recipe.getRecipeType());
        if (recipe instanceof CustomCookingRecipe cooking) {
            int input = customLayout ? 19 : 22;
            int device = customLayout ? 22 : 24;
            int output = customLayout ? 25 : 26;
            if (!recipe.getIngredients().isEmpty()) putIngredient(gui, input, recipe.getIngredients().getFirst());
            var deviceIcon = new ItemBuilder(station.material())
                    .displayName(Component.text(station.label(), NamedTextColor.YELLOW))
                    .lore(Component.text(station.material() == Material.CAMPFIRE ? "将原料放在点燃的营火上" : "将原料放入设备，并添加燃料", NamedTextColor.GRAY),
                            Component.text("耗时 " + seconds(cooking.getCookingTime()) + " 秒", NamedTextColor.GRAY))
                    .build();
            gui.setItem(device, new GuiItem(deviceIcon));
            gui.setItem(output, new GuiItem(recipe.getResult().clone()));
            return;
        }

        for (int index = 0; index < Math.min(9, recipe.getIngredients().size()); index++) {
            putIngredient(gui, 11 + (index / 3) * 9 + index % 3, recipe.getIngredients().get(index));
        }
        gui.setItem(24, new GuiItem(new ItemBuilder(station.material())
                .displayName(Component.text(station.label(), NamedTextColor.YELLOW)).build()));
        gui.setItem(26, new GuiItem(recipe.getResult().clone()));
    }

    @SuppressWarnings("deprecation") // RecipeChoice exposes the same representative stack used by Nexo's showcase.
    private static void putIngredient(Gui gui, int slot, RecipeIngredient ingredient) {
        if (ingredient == null || ingredient.getChoice().test(new ItemStack(Material.AIR))) return;
        var input = ingredient.getChoice().getItemStack().clone();
        if (input.getType().isAir()) return;
        input.setAmount(ingredient.getAmount());
        gui.setItem(slot, new GuiItem(input));
    }

    static String seconds(int ticks) {
        return BigDecimal.valueOf(ticks).divide(BigDecimal.valueOf(20)).stripTrailingZeros().toPlainString();
    }

    private static Station stationFor(RecipeType type) {
        return switch (type) {
            case SHAPED -> new Station(Material.CRAFTING_TABLE, "工作台合成（按位置摆放）");
            case SHAPELESS, TRANSMUTE -> new Station(Material.CRAFTING_TABLE, "工作台合成（摆放不限）");
            case CRAFTER -> new Station(Material.CRAFTER, "合成器制作");
            case FURNACE -> new Station(Material.FURNACE, "熔炉烧制");
            case BLASTING -> new Station(Material.BLAST_FURNACE, "高炉冶炼");
            case SMOKER -> new Station(Material.SMOKER, "烟熏炉烹饪");
            case CAMPFIRE -> new Station(Material.CAMPFIRE, "营火烹饪");
            case STONECUTTING -> new Station(Material.STONECUTTER, "切石机切割");
            case BREWING -> new Station(Material.BREWING_STAND, "酿造台酿造");
            case SMITHING -> new Station(Material.SMITHING_TABLE, "锻造台加工");
            case ANVIL -> new Station(Material.ANVIL, "铁砧加工");
        };
    }

    private record Station(Material material, String label) {}
}
