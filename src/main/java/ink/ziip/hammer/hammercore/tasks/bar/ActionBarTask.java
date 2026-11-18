package ink.ziip.hammer.hammercore.tasks.bar;

import ink.ziip.hammer.hammercore.api.object.user.HammerUser;
import ink.ziip.hammer.hammercore.api.task.BaseTask;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionBarTask extends BaseTask {

    private static ActionBarTask instance;

    private ActionBarTask() {
        super(ConfigManager.ACTION_BAR_REFRESH_TICKS);
    }

    public static ActionBarTask getInstance() {
        if (instance == null) {
            instance = new ActionBarTask();
        }
        return instance;
    }

    @Override
    public void run() {
        if (started && ConfigManager.ACTION_BAR_ENABLED) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.isInWater()) {
                    HammerUser hammerUser = HammerUser.getUser(player);
                    if (!hammerUser.isBedrockPlayer()) {
                        if (ConfigManager.ACTION_BAR_CONTENT != null && !ConfigManager.ACTION_BAR_CONTENT.isBlank()
                                && System.currentTimeMillis() - hammerUser.getActionBarSendingTime() > ConfigManager.ACTION_BAR_IDLE_RESET_MILLIS) {
                            hammerUser.sendActionBar(ConfigManager.ACTION_BAR_CONTENT, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        started = false;
        cancelTask();
    }
}