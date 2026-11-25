package ink.ziip.hammer.hammercore.feature.cauldron;

import com.dre.brewery.BDistiller;
import com.dre.brewery.Brew;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BreweryDistillationTest {
    @Test
    void leavesRecipeValidationToBreweryAndRegistersOnlyOneDistillerPerStand() throws Exception {
        var stand = mock(BrewingStand.class);
        var block = mock(Block.class);
        var inventory = mock(BrewerInventory.class);
        when(stand.getBlock()).thenReturn(block);
        when(stand.getInventory()).thenReturn(inventory);
        var contents = new Brew[3];

        try (var brewery = mockStatic(BDistiller.class); var created = mockConstruction(BDistiller.class)) {
            brewery.when(() -> BDistiller.isTrackingDistiller(block)).thenCallRealMethod();
            brewery.when(() -> BDistiller.getDistillContents(inventory)).thenReturn(contents);
            brewery.when(() -> BDistiller.hasBrew(inventory, contents)).thenReturn((byte) 1, (byte) 2);
            var distillation = new BreweryDistillation(Material.GLOWSTONE_DUST);
            distillation.check(stand);
            assertTrue(created.constructed().isEmpty());

            when(inventory.getIngredient()).thenReturn(new ItemStack(Material.GLOWSTONE_DUST));
            distillation.check(stand);
            assertTrue(created.constructed().isEmpty());
            distillation.check(stand);
            distillation.check(stand);

            assertEquals(1, created.constructed().size());
            assertTrue(BDistiller.isTrackingDistiller(block));
            verify(created.constructed().getFirst()).start();
        }
    }
}
