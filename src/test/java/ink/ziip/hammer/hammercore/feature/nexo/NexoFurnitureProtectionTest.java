package ink.ziip.hammer.hammercore.feature.nexo;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NexoFurnitureProtectionTest {
    @Test
    void keepsLegacyProtectionOnlyForFurnitureWithAnOwner() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        assertTrue(NexoFurnitureProtection.mayChangeFurniture(null, other, false));
        assertTrue(NexoFurnitureProtection.mayChangeFurniture(owner.toString(), owner, false));
        assertTrue(NexoFurnitureProtection.mayChangeFurniture(owner.toString(), other, true));
        assertFalse(NexoFurnitureProtection.mayChangeFurniture(owner.toString(), other, false));
        assertFalse(NexoFurnitureProtection.mayChangeFurniture(owner.toString(), null, false));
    }
}
