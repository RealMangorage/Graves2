package me.mangorage.graves2;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.List;


public class PlayerGrave implements Listener {
    Graves2 plug;
    public String PlayerID;
    public String PlayerName;
    public Location Deathloca;
    public Inventory Grave;
    public boolean Public = false;
    public boolean Empty = false;
    public boolean Deleted = false;
    public String SearchUUID = "";
    public long createdAt = 0;
    public long goespublicat = 0;
    private int TaskID = -1;
    BlockData Data = Material.ENDER_CHEST.createBlockData();




    PlayerGrave(Graves2 pl, Player Plr, Location Death, List<ItemStack> Drops) {
        // Used to create a new Grave
        // Check to see if we can place Grave!
        this.plug = pl;
        int Valid = 1;
        int Bounds = BasicUtils.OutOfBounds(Death.getBlockY());
        Location nDeath = Death;
        int nY = 0;

        if (Bounds == 0) {
            if (Death.getBlock().getType().isAir()) {
                Valid = 0;
            }
            nDeath = new Location(Death.getWorld(), Death.getBlockX(), Death.getBlockY() + Valid, Death.getBlockZ());
        } else if (Bounds == 1 || Bounds == 0) {
            nDeath = new Location(Death.getWorld(), Death.getBlockX(), 253, Death.getBlockZ());
        }



        createdAt = System.currentTimeMillis();
        goespublicat = createdAt + ((18000/20) * 1000);
        PlayerID = Plr.getUniqueId().toString();
        PlayerName = Plr.getName();
        Deathloca = nDeath;
        Deathloca.getBlock().setType(Material.CHEST);

        this.Grave = Bukkit.createInventory(null, 54, PlayerName + "'s Grave");
        for (ItemStack ItemA : Drops) {
            Grave.addItem(ItemA);
        }

        Bukkit.getPluginManager().registerEvents(this, pl);

        TaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
            @Override
            public void run() {
                for (Player plra : Bukkit.getOnlinePlayers()) {
                    plra.sendBlockChange(Deathloca, Data.getMaterial(), (byte) 0);
                }
            }
        }, 0, 0);

        Bukkit.getScheduler().runTaskLater(pl, new Runnable() {
            @Override
            public void run() {
                Public = true;
            }
        }, 18000);

    }

    PlayerGrave(Graves2 pl, Inventory GraveData, String bPlayerID, String bPlayerName, Location Death, long TicksLeft, long CreatedAt, long GoesPublicAt, boolean Pub) { // Used to load a Grave
        this.plug = pl;
        System.out.println("Ticks Left: " + TicksLeft);
        // Used to create a new Grave
        // Check to see if we can place Grave!
        int Valid = 0;

        Public = Pub;
        createdAt = CreatedAt;
        goespublicat = GoesPublicAt;

        Location nDeath = new Location(Death.getWorld(), Death.getBlockX(), Death.getBlockY() + Valid, Death.getBlockZ());
        PlayerID = bPlayerID;
        PlayerName = bPlayerName;
        Deathloca = nDeath;
        Deathloca.getBlock().setType(Material.CHEST);

        this.Grave = Bukkit.createInventory(null, 54, PlayerName + "'s Grave");
        for (ItemStack ItemA : GraveData.getContents()) {
            if (ItemA != null) {
                Grave.addItem(ItemA);
            }
        }

        Bukkit.getPluginManager().registerEvents(this, pl);

        TaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
            @Override
            public void run() {
                for (Player plra : Bukkit.getOnlinePlayers()) {
                    plra.sendBlockChange(Deathloca, Data.getMaterial(), (byte) 0);
                }
            }
        }, 0, 0);

        if (!Pub) {
            Bukkit.getScheduler().runTaskLater(pl, new Runnable() {
                @Override
                public void run() {
                    Public = true;
                }
            }, TicksLeft);
        }
    }

    public void DeleteGrave() {
        Bukkit.getScheduler().cancelTask(TaskID);
        Deathloca.getBlock().setType(Material.AIR);
        Deleted = true;
    }

    public void Shutdown() throws IOException {
        System.out.println("Saving Grave!");
        Bukkit.getScheduler().cancelTask(TaskID);
        Deathloca.getBlock().setType(Material.AIR);
        if (!(Deleted || Empty)) {
            BasicUtils.SaveGrave(this);
        }
    }

    @EventHandler
    public void OnPlayerOpen(InventoryOpenEvent event) {
        if (event.getInventory().getLocation() == null) {
            return;
        }

        if (event.getInventory().getLocation().equals(Deathloca)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getLocation().equals(Deathloca) && !Deleted) {
            event.setCancelled(true);
            Deathloca.getBlock().setType(Material.CHEST);
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if (event.getInventory().getLocation() == null) {
            if (event.getView().getTitle().contains(PlayerName + "'s Grave")) {
                if (BasicUtils.isInvEmpty(Grave)) {
                    Deleted = true;
                    DeleteGrave();
                }
            }
        }
    }

    @EventHandler
    public void OnBlockInteract(PlayerInteractEvent event) {
        Player plr = event.getPlayer();
        if (event.getClickedBlock() != null) {
            if (event.getClickedBlock().getLocation().equals(Deathloca) && !Deleted) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    // Info
                    event.getPlayer().sendMessage("[Graves] This is a Grave! Right click to Open!");
                } else {
                    if (event.getPlayer().getUniqueId().toString().equals(PlayerID) || Public) {
                        Bukkit.getScheduler().runTaskLater(this.plug, new Runnable() {
                            @Override
                            public void run() {
                                event.getPlayer().openInventory(Grave);
                            }
                        }, 5);
                    } else {
                        // Info (Not your Grave)
                        event.getPlayer().sendMessage("[Graves] This isnt your Grave!");
                    }
                }
            }
        }
    }



}
