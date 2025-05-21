package dev.glowie.townySpacePorts.utils;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import dev.glowie.townySpacePorts.TownySpacePorts;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class SpaceUtils {

    private static HashMap<UUID, Long> cooldown = null;
    public SpaceUtils() {
        this.cooldown = new HashMap<>();
    }

    public static void teleportPlayer(ConfigurationSection portSettings, Player player, World portWorld) {

                    int cdSec = ConfigUtils.getCooldownSeconds(portSettings);
                    int cdTick = cdSec*20;
                    //if (!cooldown.containsKey(player.getUniqueId()) || System.currentTimeMillis() - cooldown.get(player.getUniqueId()) > cdSec*1000) {
                    //    cooldown.put(player.getUniqueId(), System.currentTimeMillis());
                    //} else {
                    //    long calc = (System.currentTimeMillis() - cooldown.get(player.getUniqueId()))/1000;
                    //    MessagingUtils.errorPlayer(player , "You need to wait another" + Math.round(cdSec - calc) + " seconds to travel again.");
                    //    return;
                    //}
                    int warmupSec = ConfigUtils.getWarmupSeconds(portSettings);
                    int warmupTick = warmupSec*20;
                    MessagingUtils.messagePlayer(player, "You will depart in §b" + warmupSec + " seconds§a.");

                    TownBlock locOld = TownyAPI.getInstance().getTownBlock(player);

                    Bukkit.getScheduler().runTaskLater(TownySpacePorts.instance, new Runnable() {
                        @Override
                        public void run() {
                            if (!player.isOnline()) {
                                return;
                            }

                            if (locOld != TownyAPI.getInstance().getTownBlock(player.getLocation())) {
                                MessagingUtils.alertPlayer(player, "You have moved away from the space port while waiting, teleportation denied.");
                                return;
                            }
                            player.teleportAsync(ConfigUtils.getPortLocationXYZ(portSettings, portWorld));
                            MessagingUtils.messagePlayer(player,"Arrived at the space port.");
                        }
                    }, warmupTick);


    }

    public static ConfigurationSection findPortFromPlayer(Player p) {
        for (String portName : ConfigUtils.getConfig().getKeys(false)) {
            for (String worldName : ConfigUtils.getConfig().getConfigurationSection(portName).getKeys(false)) {
                Location portL = ConfigUtils.getPortLocationXYZ(ConfigUtils.getPortSettings(portName, worldName), Bukkit.getWorld(worldName));
                if (isPlayerNearLocation(p, portL)) {
                    return ConfigUtils.getPortSettings(portName, worldName);
                }
            }
        }
        return null;
    }

    public static String findPortNameFromPlayer(Player p) {
        for (String portName : ConfigUtils.getConfig().getKeys(false)) {
            for (String worldName : ConfigUtils.getConfig().getConfigurationSection(portName).getKeys(false)) {
                Location portL = ConfigUtils.getPortLocationXYZ(ConfigUtils.getPortSettings(portName, worldName), Bukkit.getWorld(worldName));
                if (isPlayerNearLocation(p, portL)) {
                    return portName;
                }
            }
        }
        return "";
    }

    public static boolean isPlayerNearLocation(Player player, Location location) {
        return TownyAPI.getInstance().getTownBlock(player) == TownyAPI.getInstance().getTownBlock(location);
    }

    public static ConfigurationSection findPortFromPlot(TownBlock townBlock) {
        for (String portName : ConfigUtils.getConfig().getKeys(false)) {
            for (String worldName : ConfigUtils.getConfig().getConfigurationSection(portName).getKeys(false)) {
                Location portL = ConfigUtils.getPortLocationXYZ(ConfigUtils.getPortSettings(portName, worldName), Bukkit.getWorld(worldName));
                if (isPlotNearLocation(townBlock, portL)) {
                    return ConfigUtils.getPortSettings(portName, worldName);
                }
            }
        }
        return null;
    }

    public static String findPortNameFromPlot(TownBlock townBlock) {
        for (String portName : ConfigUtils.getConfig().getKeys(false)) {
            for (String worldName : ConfigUtils.getConfig().getConfigurationSection(portName).getKeys(false)) {
                Location portL = ConfigUtils.getPortLocationXYZ(ConfigUtils.getPortSettings(portName, worldName), Bukkit.getWorld(worldName));
                if (isPlotNearLocation(townBlock, portL)) {
                    return portName;
                }
            }
        }
        return null;
    }

    public static boolean isPlotNearLocation(TownBlock tb, Location location) {
        return tb == TownyAPI.getInstance().getTownBlock(location);
    }

    public static boolean isPort(TownBlock townBlock) {
        for (String portName : ConfigUtils.getConfig().getKeys(false)) {
            for (String worldName : ConfigUtils.getConfig().getConfigurationSection(portName).getKeys(false)) {
                Location portL = ConfigUtils.getPortLocationXYZ(ConfigUtils.getPortSettings(portName, worldName), Bukkit.getWorld(worldName));
                if (isPlotNearLocation(townBlock, portL)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static void transferOtherPorts(TownBlock conqueredTownBlock, Town attackingTown) {

        if (!SpaceUtils.isPort(conqueredTownBlock)) {
            return;
        }

        ConfigurationSection port = SpaceUtils.findPortFromPlot(conqueredTownBlock);
        String portName = SpaceUtils.findPortNameFromPlot(conqueredTownBlock);



        if (attackingTown == null) return;

        if (ConfigUtils.conquersRestSameName(port)) {
            for (String world : ConfigUtils.getConfig().getConfigurationSection(portName).getKeys(false)) {
                Location loc = ConfigUtils.getPortLocationXYZ(ConfigUtils.getPortSettings(portName, world), Bukkit.getWorld(world));
                if (!TownyAPI.getInstance().isWilderness(loc)) {
                    TownyAPI.getInstance().getTownBlock(loc).setTown(attackingTown);
                    TownyAPI.getInstance().getTownBlock(loc).save();
                }
            }
        }
    }

}
