package ink.ziip.hammer.hammercore.api.task;

import ink.ziip.hammer.hammercore.HammerCore;
import org.bukkit.scheduler.BukkitTask;

/**
 * Reusable scheduled task. A cancelled BukkitRunnable cannot be scheduled again
 * on modern Paper, so this holds a {@link BukkitTask} handle instead and lets
 * callers start/stop the task any number of times (for /reload).
 */
public abstract class BaseTask {

    protected boolean started;
    protected int period;
    private BukkitTask task;

    public BaseTask(int period) {
        this.started = false;
        this.period = Math.max(1, period);
    }

    /** Updates the scheduler period; takes effect on the next {@link #start()}. */
    public void setPeriod(int period) {
        this.period = Math.max(1, period);
    }

    public void start() {
        stop();
        this.task = HammerCore.getInstance().getServer().getScheduler()
                .runTaskTimer(HammerCore.getInstance(), this::run, 1L, period);
        started = true;
    }

    public abstract void run();

    public abstract void stop();

    /** Cancels the underlying scheduler handle, if any. */
    protected void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
