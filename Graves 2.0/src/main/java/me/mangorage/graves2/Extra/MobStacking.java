package me.mangorage.graves2.Extra;

import me.mangorage.graves2.Graves2;
import org.bukkit.Bukkit;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.Inventory;

public class MobStacking implements Listener {
    Inventory SheepMenu = Bukkit.createInventory(null, (9*4), "Sheep Menu");


    public MobStacking() {
        System.out.println("Loaded Sheeps");
    }

    @EventHandler
    private void onSheepClick(PlayerShearEntityEvent event) {
        double num = Math.random();
        if (Math.round(num*10) == 5) {
            event.getPlayer().sendMessage("[Sheep] OWWWW!");
            event.getPlayer().sendMessage("[Sheep] Ima Shear you!");
            event.getPlayer().damage(0.5);
            Sheep Shep = (Sheep) event.getEntity();
            Shep.setSheared(false);
        }
    }



}
