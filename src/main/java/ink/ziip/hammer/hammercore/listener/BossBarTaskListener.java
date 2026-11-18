package ink.ziip.hammer.hammercore.listener;

import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import ink.ziip.hammer.hammercore.tasks.bar.BossBarTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BossBarTaskListener extends BaseListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigManager.INFO_BAR_ENABLED) return;
        Player player = event.getPlayer();
        BossBarTask.addToAll(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (ConfigManager.INFO_BAR_REMOVE_ON_QUIT) {
            BossBarTask.removeFromAll(player);
        }
    }
}
