package ink.ziip.hammer.hammercore.feature.cauldron;

import com.dre.brewery.BDistiller;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;

import java.util.Map;

/** Keeps automated starts in BreweryX's own registry, which also controls hopper extraction. */
final class BreweryDistillation {
    private final Map<Block, BDistiller> active;
    private final Material ingredient;

    @SuppressWarnings("unchecked")
    BreweryDistillation(Material ingredient) throws ReflectiveOperationException {
        var field = BDistiller.class.getDeclaredField("trackedDistillers");
        field.setAccessible(true);
        active = (Map<Block, BDistiller>) field.get(null);
        this.ingredient = ingredient;
    }

    void check(BrewingStand stand) {
        Block block = stand.getBlock();
        if (active.containsKey(block)) return;
        var inventory = stand.getInventory();
        var catalyst = inventory.getIngredient();
        if (catalyst == null || (catalyst.getType() != Material.GLOWSTONE_DUST
                && catalyst.getType() != ingredient)) return;
        if (BDistiller.hasBrew(inventory, BDistiller.getDistillContents(inventory)) != 2) return;

        var distiller = new BDistiller(block, stand.getFuelLevel());
        if (active.putIfAbsent(block, distiller) != null) return;
        try {
            distiller.start();
        } catch (RuntimeException | LinkageError error) {
            active.remove(block, distiller);
            throw error;
        }
    }
}
