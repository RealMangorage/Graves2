package me.mangorage.graves2;

import me.mangorage.graves2.Extra.MobStacking;
import me.mangorage.graves2.Menu.Upgrade;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Graves2 extends JavaPlugin implements Listener {
    HashMap<String, PlayerGrave> AdminGraves = new HashMap<>();
    ArrayList<String> cooldown = new ArrayList<>();
    ArrayList<PlayerGrave> Graves = new ArrayList<PlayerGrave>();
    Inventory AdminGraveMenu = Bukkit.createInventory(null, 54, "Player Graves");
    Upgrade UpgradeMenu;
    boolean Shatters = false;


    /**
     * Save:
     *
     * X,Y,Z
     * World
     * GraveInventory
     * PlayerName and ID
     * goespublicat
     *
     */

    /**
     * Permissions:
     * Graves.comamnd.open
     * Graves.command.tp
     * Graves.command.menu
     * Graves.command.help
     * Graves.use
     */

    public void RefreshInventory() {
        AdminGraveMenu.remove(Material.PLAYER_HEAD);
        AdminGraveMenu.remove(Material.GRAY_STAINED_GLASS_PANE);

        // Adding Graves

        for (PlayerGrave cbGrave : Graves) {
            ItemStack GraveI = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta playerHeadMeta = (SkullMeta) GraveI.getItemMeta();
            playerHeadMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(cbGrave.PlayerID)));
            GraveI.setItemMeta(playerHeadMeta);

            GraveI.setAmount(1);
            ItemMeta Meta = GraveI.getItemMeta();
            Meta.setDisplayName(cbGrave.PlayerName + "'s Grave");
            List<String> lore = new ArrayList<>();
            lore.add(cbGrave.SearchUUID);

            int x = cbGrave.Deathloca.getBlockX();
            int y = cbGrave.Deathloca.getBlockY();
            int z = cbGrave.Deathloca.getBlockZ();
            String World = cbGrave.Deathloca.getWorld().getName();

            lore.add("X: " + x + " Y: " + y + " Z: " + z);
            lore.add("World: " + World);
            lore.add("Owner: " + cbGrave.PlayerName);

            if (cbGrave.Public) {
                lore.add("Public Grave");
            } else {
                lore.add("Private Grave");
            }

            if (!cbGrave.Public) {
                Map<String, Integer> Mapy = BasicUtils.time(cbGrave.goespublicat - System.currentTimeMillis());
                lore.add(Mapy.get("Minutes") + "m " + Mapy.get("seconds") + "s" );
            }
            Meta.setLore(lore);
            GraveI.setItemMeta(Meta);
            AdminGraveMenu.addItem(GraveI);
        }

        ItemStack Filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta FillerMeta = Filler.getItemMeta();
        FillerMeta.setDisplayName(" ");
        Filler.setItemMeta(FillerMeta);


       // for (int i = Graves.size(); i < 55; i++) {
            //AdminGraveMenu.setItem(i-1, Filler);
       // }


    }

    private void loadCache() {
        for (File file : (new File(getDataFolder().getAbsolutePath() + File.separator + "Cache")).listFiles()) {

            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            String Inv = yamlConfiguration.getString("Inventory");
            String PlayerID = yamlConfiguration.getString("Data.PlayerID");
            String PlayerName = yamlConfiguration.getString("Data.PlayerName");
            int LocationX = yamlConfiguration.getInt("Data.Location.X");
            int LocationY = yamlConfiguration.getInt("Data.Location.Y");
            int LocationZ = yamlConfiguration.getInt("Data.Location.Z");
            String World = yamlConfiguration.getString("Data.Location.World");
            long TicksLeft = yamlConfiguration.getLong("Data.TicksLeft");
            long Created = yamlConfiguration.getLong("Data.CreatedAt");
            long GoesPublicAt = yamlConfiguration.getLong("Data.GoesPublicAt");
            String SearchID = yamlConfiguration.getString("Data.SearchID");
            boolean isPub = yamlConfiguration.getBoolean("Data.Public");
            Inventory InvData = null;

            Location loca = new Location(Bukkit.getWorld(World), LocationX, LocationY, LocationZ);
            file.delete();

            try {
                InvData = Serializer.fromBase64(Inv);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (InvData != null) {
                PlayerGrave newGrave = new PlayerGrave(this, InvData, PlayerID, PlayerName, loca, TicksLeft, Created, GoesPublicAt, isPub);
                Graves.add(newGrave);
                newGrave.SearchUUID = SearchID;
                AdminGraves.put(newGrave.SearchUUID, newGrave);
             }
        }

}


    @Override
    public void onEnable() {
        // Configure File Structure!

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File Cache = new File(getDataFolder(), "Cache");
        if (!Cache.exists()) {
            Cache.mkdirs();
        } else {
            loadCache();
        }


        Bukkit.getPluginManager().registerEvents(new ExtraFeatures(this), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new MobStacking(), this);
        this.UpgradeMenu = new Upgrade(this);
        getCommand("Graves").setExecutor(this::onCommand);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    for (PlayerGrave cGrave : Graves) {
                        if (cGrave.Deleted || cGrave.Empty) {
                            AdminGraves.remove(cGrave.SearchUUID);
                            Graves.remove(cGrave);
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    // Normal!
                }
                RefreshInventory();
            }
        },0,20L);

    }

    @EventHandler
    public void OnScrollUse(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (event.getItem().getItemMeta().getDisplayName().contains("Death Scroll")) {
                ItemStack ItemS = event.getItem();
                ItemMeta ItemMetaB = ItemS.getItemMeta();
                List<String> loreb = ItemMetaB.getLore();
                String UUID = loreb.get(0);

                if (AdminGraves.containsKey(UUID) ) {
                    if (AdminGraves.get(UUID).PlayerID.equals(event.getPlayer().getUniqueId().toString())) {
                        event.getPlayer().openInventory(AdminGraves.get(UUID).Grave);
                        if (Shatters) {
                            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                                @Override
                                public void run() {
                                    event.getPlayer().getInventory().removeItem(ItemS);
                                }
                            }, 10L);
                        }
                    } else {
                        event.getPlayer().sendMessage("[Graves] Not Your Grave!");
                    }
                } else {
                    event.getPlayer().sendMessage("[Graves] Grave doesn't Exist!");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        boolean check = false;
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            check = true;
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            check = true;
        }

        ItemStack Hand = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta HandMeta = Hand.getItemMeta();
        Player plr = event.getPlayer();

        if (check) {
            try {
                if (Hand != null && HandMeta.getDisplayName().contains("Death Note")) {
                    if (Hand.getAmount() == 1) {
                        UpgradeMenu.openUpgrade(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
                    } else {
                        if (!cooldown.contains(plr.getUniqueId().toString())) {
                            cooldown.add(event.getPlayer().getUniqueId().toString());
                            event.getPlayer().sendMessage("[Graves] Unable to upgrade. Please only have one Death Note in your Hand!");
                            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                                @Override
                                public void run() {
                                    cooldown.remove(plr.getUniqueId().toString());
                                }
                            }, 100L);
                        }
                    }
                }
            } catch (NullPointerException e) {
                // Idk how to fix , so fuck it!
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (PlayerGrave Shut : Graves) {
            try {
                Shut.Shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ItemStack createDeathNote(Player plr, PlayerGrave Grave) {
        int X = Grave.Deathloca.getBlockX();
        int Y = Grave.Deathloca.getBlockY();
        int Z = Grave.Deathloca.getBlockZ();
        String World = Grave.Deathloca.getWorld().getName();

        List<String> lore = new ArrayList<>();
        lore.add(Grave.SearchUUID);
        lore.add("X: " + X + " Y: " + Y + " Z: " + Z);
        lore.add("World: " + World);
        lore.add("Owner: " + Grave.PlayerName);
        lore.add("Right click to Upgrade");

        ItemStack DeathNote = new ItemStack(Material.PAPER);
        ItemMeta Meta = DeathNote.getItemMeta();
        Meta.setLore(lore);
        Meta.setDisplayName(Grave.PlayerName + "'s Death Note");
        DeathNote.setItemMeta(Meta);

        return DeathNote;
    }


    @EventHandler
    public void OnDeath(PlayerDeathEvent event) {

        if (!BasicUtils.isInvEmpty(event.getDrops())) {

            PlayerGrave newGrave = new PlayerGrave(this, event.getEntity(), event.getEntity().getLocation(), new ArrayList<ItemStack>(event.getDrops()));
            Graves.add(newGrave);
            newGrave.SearchUUID = UUID.randomUUID().toString();
            AdminGraves.put(newGrave.SearchUUID, newGrave);

            // Final bit!
            event.getDrops().clear();
            event.getEntity().getInventory().clear();

            // Death Note
            ItemStack DeathNote = createDeathNote(event.getEntity(), newGrave);
            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                @Override
                public void run() {
                    event.getEntity().getInventory().addItem(DeathNote);
                }
            }, 10L);
        }

    }

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase("Player Graves")) {
            // Right Click - Open Grave
            // Left Click - teleport to grave

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                String UUID = event.getCurrentItem().getItemMeta().getLore().get(0);
                Player plr = (Player) event.getWhoClicked();

                if (event.getClick() == ClickType.RIGHT) {
                    Bukkit.dispatchCommand(plr, "graves open " + UUID);
                } else if (event.getClick() == ClickType.LEFT) {
                    Bukkit.dispatchCommand(plr, "graves tp " + UUID);
                } else if (event.getClick() == ClickType.MIDDLE) {
                    Bukkit.dispatchCommand(plr, "graves getnote " + UUID);
                } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                   // Bukkit.dispatchCommand(plr, "graves preview " + UUID);
                }

            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnInvEvent(InventoryInteractEvent event) {
        if (event.getView().getTitle().equals("Player Graves")) {
            event.setCancelled(true);
        }
    }

    public void NoPerm(Player plr) {
        plr.sendMessage("You dont got Permission to use this command!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player plr = (Player) sender;

            if (args.length > 0) {
                if (args.length == 1) {
                    // /graves menu/info
                    if (args[0].contains("menu") && plr.hasPermission("Graves.command.menu")) {
                        // Menu
                        plr.openInventory(AdminGraveMenu);
                    } else if (args[0].contains("info") || args[0].contains("help") && plr.hasPermission("Graves.command.help")) {
                        // Help
                    } else {
                        NoPerm(plr);
                    }
                } else if (args.length > 1 && args.length <= 2) {
                    System.out.println(args[0] + "  :" + args[1]);
                    // /graves open/tp UUID
                    String UUID = args[1];

                    if (!AdminGraves.containsKey(UUID)) {
                        plr.sendMessage("Invalid GraveID");
                        return true;
                    }

                    PlayerGrave graveC = AdminGraves.get(UUID);

                    if (args[0].contains("open") && plr.hasPermission("Graves.command.open")) {
                        // Open
                        plr.openInventory(graveC.Grave);
                    } else if (args[0].contains("tp") && plr.hasPermission("Graves.command.tp")) {
                        // TP
                        plr.teleport(graveC.Deathloca);
                    } else if (args[0].contains("getnote") && plr.hasPermission("Graves.command.getnote")) {
                        if (graveC.PlayerID.equals(plr.getUniqueId().toString())) {
                            ItemStack DeathNote = createDeathNote(plr, graveC);
                            if (!plr.getInventory().containsAtLeast(DeathNote, 1)) {
                                Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                                    @Override
                                    public void run() {
                                        plr.getInventory().addItem(DeathNote);
                                    }
                                }, 10L);
                            }
                        }
                    } else {
                        NoPerm(plr);
                    }
                }
            }
        }
        return true;
    }
}
