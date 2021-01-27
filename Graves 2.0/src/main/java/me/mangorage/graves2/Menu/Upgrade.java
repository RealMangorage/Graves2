package me.mangorage.graves2.Menu;

import me.mangorage.graves2.Graves2;
import me.mangorage.graves2.PlayerGrave;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Upgrade implements Listener {
    Inventory UpgradeMenu = Bukkit.createInventory(null, 27, "Death Scroll Upgrade");
    ItemStack Filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
    ItemMeta FillerMeta = Filler.getItemMeta();

    Graves2 Plug;

    public Upgrade(Graves2 pl) {
        Plug = pl;

        FillerMeta.setDisplayName(" ");
        Filler.setItemMeta(FillerMeta);

        for (int i = 0; i < 27; i++) {
            UpgradeMenu.setItem(i, Filler);
        }

        // 29 31 33

        ItemStack Info = new ItemStack(Material.BOOK, 1);
        ItemStack Confirm = new ItemStack(Material.EMERALD, 1);

        ItemMeta InfoMeta = Info.getItemMeta();
        ItemMeta ConfirmMeta = Confirm.getItemMeta();

        List<String> InfoLore = new ArrayList<>();
        List<String> ConfirmLore = new ArrayList<>();

        InfoLore.add("Upgrading this allows");
        InfoLore.add("you to open your Grave!");
        ConfirmLore.add("This Upgrade Costs 4");
        ConfirmLore.add("4 Netherite Ingots");

        InfoMeta.setLore(InfoLore);
        ConfirmMeta.setLore(ConfirmLore);

        InfoMeta.setDisplayName("Info");
        ConfirmMeta.setDisplayName("Confirm");

        Info.setItemMeta(InfoMeta);
        Confirm.setItemMeta(ConfirmMeta);

        UpgradeMenu.setItem(13, Confirm);
        UpgradeMenu.setItem(15, Info);

        Bukkit.getPluginManager().registerEvents(this, pl);
        System.out.println("Loaded Upgrade Menu!");
    }

    private void confirmUpgrade(Player plr, ItemStack DeathNote, String Name) {
        ItemMeta DeathMeta = DeathNote.getItemMeta();
        DeathMeta.setDisplayName(Name + "Death Scroll");
        DeathMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        DeathMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        DeathMeta.addEnchant(Enchantment.MENDING, 1, false);

        if (plr.getInventory().contains(Material.NETHERITE_INGOT, 4)) {
            plr.getInventory().removeItem(new ItemStack(Material.NETHERITE_INGOT, 4));
            plr.getInventory().getItemInMainHand().setItemMeta(DeathMeta);
            plr.closeInventory();
        } else {
            plr.sendMessage("Dont have enough to Upgrade!");
        }

    }

    public void openUpgrade(Player plr, ItemStack DeathNote) {


        Inventory Temp = Bukkit.createInventory(null, 27, "Death Scroll Upgrade");
        Temp.setContents(UpgradeMenu.getContents());
        Temp.setItem(11, DeathNote);

        plr.openInventory(Temp);

    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("Death Scroll Upgrade")) {
            event.setCancelled(true);

            if (event.getClick() == ClickType.LEFT) {
                if (event.getSlot() == 13) {
                    String Name = event.getClickedInventory().getItem(11).getItemMeta().getDisplayName().replaceAll("Death Note", "");
                    confirmUpgrade((Player) event.getWhoClicked(), event.getClickedInventory().getItem(11), Name);
                }
            }
        }
    }

}
