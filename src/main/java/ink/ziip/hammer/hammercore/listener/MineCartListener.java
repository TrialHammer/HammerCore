package ink.ziip.hammer.hammercore.listener;

import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import ink.ziip.hammer.hammercore.api.object.user.HammerUser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

public class MineCartListener extends BaseListener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicleLeave(EntityDismountEvent event) {
        Entity dismounted = event.getDismounted();
        Entity entity = event.getEntity();
        if (entity instanceof Player && dismounted instanceof Minecart minecart && minecart.getPassengers().size() == 1) {
            HammerUser hammerUser = HammerUser.getUser(entity);
            if (hammerUser.isRemoveMineCartOnLeaving() && ink.ziip.hammer.hammercore.manager.ConfigManager.MINECART_REMOVE_ON_LEAVE) {
                minecart.remove();
                entity.teleport(entity.getLocation().add(0, 1, 0));
                entity.getWorld().dropItem(entity.getLocation(), new ItemStack(Material.MINECART));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicleLeave(VehicleEntityCollisionEvent event) {
        if (event.getEntity() instanceof Minecart minecart) {
            for (Entity entity : minecart.getPassengers()) {
                if (entity instanceof Player) {
                    event.setCollisionCancelled(true);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!ink.ziip.hammer.hammercore.manager.ConfigManager.MINECART_ACCELERATOR_ENABLED) return;
        if (event.getVehicle() instanceof Minecart cart && !event.getVehicle().isEmpty()) {

            for (Entity entity : event.getVehicle().getPassengers()) {
                if (entity instanceof LivingEntity) {
                    int required = ink.ziip.hammer.hammercore.manager.ConfigManager.MINECART_ACCELERATOR_RAILS_REQUIRED;
                    boolean boosted = true;
                    for (int i = 1; i <= required; i++) {
                        if (event.getFrom().add(cart.getFacing().getDirection().multiply(i)).getBlock().getType() != Material.POWERED_RAIL) {
                            boosted = false;
                            break;
                        }
                    }
                    if (boosted && cart.getLocation().getBlock().getType() != Material.RAIL
                            && event.getFrom().getBlock().getType() != Material.RAIL
                            && event.getTo().getBlock().getType() != Material.RAIL) {
                        cart.setMaxSpeed(ink.ziip.hammer.hammercore.manager.ConfigManager.MINECART_BOOSTED_MAX_SPEED);
                    } else {
                        double cap = ink.ziip.hammer.hammercore.manager.ConfigManager.MINECART_SPEED_CAP;
                        if (cart.getVelocity().length() > cap) {
                            cart.setVelocity(cart.getVelocity().multiply(cap / cart.getVelocity().length()));
                        }
                        cart.setMaxSpeed(ink.ziip.hammer.hammercore.manager.ConfigManager.MINECART_NORMAL_MAX_SPEED);
                    }
                    return;
                }
            }
        }
    }
}
