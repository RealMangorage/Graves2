package me.mangorage.graves2;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BasicUtils {
    public static boolean isInvEmpty(List<ItemStack> Drops) {
        for(ItemStack item : Drops)
        {
            if(item != null)
                return false;
        }
        return true;
    }

    public static int OutOfBounds(int y) {
        if (y >= 255) {
            return 1; // Over Y 255
        } else if (y <= 0) {
            return -1; // Under Y 0
        } else {
            return 0; // No out of Bounds
        }
    }

    public static boolean isInvEmpty(Inventory Inv) {
        for(ItemStack item : Inv)
        {
            if(item != null)
                return false;
        }
        return true;
    }

    public static String turnPosintoString(Location loc) {
        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
    }

    public static void sendColoredMessage(Player Plr, String Message) {
        String Char = "&";
        Plr.sendMessage(ChatColor.translateAlternateColorCodes(Char.charAt(0), Message));
    }

    public static String colorify(String Message) {
        String Char = "&";
        return ChatColor.translateAlternateColorCodes(Char.charAt(0), Message);
    }

    public static void SaveGrave(PlayerGrave grave) throws IOException {
        String UUIDTag = UUID.randomUUID().toString();
        File GraveData = new File(grave.plug.getDataFolder() + "/Cache/" + UUIDTag + ".yml" );
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(GraveData);

        yamlConfiguration.set("Inventory", Serializer.toBase64(grave.Grave));
        yamlConfiguration.set("Data.PlayerID", grave.PlayerID);
        yamlConfiguration.set("Data.PlayerName", grave.PlayerName);
        yamlConfiguration.set("Data.Location.X", grave.Deathloca.getBlockX());
        yamlConfiguration.set("Data.Location.Y", grave.Deathloca.getBlockY());
        yamlConfiguration.set("Data.Location.Z", grave.Deathloca.getBlockZ());
        yamlConfiguration.set("Data.Location.World", grave.Deathloca.getWorld().getName());
        yamlConfiguration.set("Data.TicksLeft", ((grave.goespublicat - System.currentTimeMillis())/1000)*20);
        yamlConfiguration.set("Data.CreatedAt", grave.createdAt);
        yamlConfiguration.set("Data.GoesPublicAt", grave.goespublicat);
        yamlConfiguration.set("Data.SearchID", grave.SearchUUID);
        yamlConfiguration.set("Data.IsPublic", grave.Public);
        yamlConfiguration.save(GraveData);


    }

    public static Map<String, Integer> time(long milliseconds) {
        Map<String, Integer> Map = new HashMap<>();
        int seconds =(int)(milliseconds /1000)%60;
        int minutes =(int)((milliseconds /(1000*60))%60);
        int hours =(int)((milliseconds /(1000*60*60))%24);
        Map.put("seconds", seconds);
        Map.put("Minutes", minutes);
        Map.put("Hours", hours);
        return Map;
    }
}
