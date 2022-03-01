package nl.mtvehicles.core.infrastructure.modules;

import lombok.Getter;
import lombok.Setter;
import nl.mtvehicles.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.logging.Logger;

public class VersionModule {
    private static @Getter
    @Setter
    VersionModule instance;

    public static String pluginVersion;
    public static boolean isPreRelease;
    public static String serverVersion;
    public static String serverSoftware;
    Logger logger = Main.instance.getLogger();

    public VersionModule() {
        PluginDescriptionFile pdf = Main.instance.getDescription();
        pluginVersion = pdf.getVersion();

        //Pre-releases should thus be named "vX.Y.Z-preU" etc... (Instead of pre, dev for developing and rc for release candidates are acceptable too.)
        isPreRelease = pluginVersion.toLowerCase().contains("pre") || pluginVersion.toLowerCase().contains("rc") || pluginVersion.toLowerCase().contains("dev");

        serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        serverSoftware = Bukkit.getName();
    }

    public boolean isSupportedVersion(){
        if (!serverVersion.equals("v1_12_R1") && !serverVersion.equals("v1_13_R2") && !serverVersion.equals("v1_15_R1") && !serverVersion.equals("v1_16_R3") && !serverVersion.contains("v1_17_R1") && !serverVersion.contains("v1_18_R1")) {
            logger.severe("--------------------------");
            logger.severe("Your Server version is not supported. The plugin will NOT load.");
            logger.severe("Check the supported versions here: https://mtvehicles.nl");
            logger.severe("--------------------------");
            Main.disablePlugin();
            return false;
        }

        else if (!Bukkit.getVersion().contains("1.12.2") && !Bukkit.getVersion().contains("1.13.2") && !Bukkit.getVersion().contains("1.15.2") && !Bukkit.getVersion().contains("1.16.5") && !Bukkit.getVersion().contains("1.17.1") && !Bukkit.getVersion().contains("1.18.1")) {
            logger.warning("--------------------------");
            logger.warning("Your Server does not run the latest patch version (e.g. you may be running 1.16.3 instead of 1.16.5 etc...).");
            logger.warning("The plugin WILL load but you are NOT eligible for any support unless you update the server.");
            logger.warning("--------------------------");
        }

        else if (serverSoftware.equals("Purpur")){
            logger.warning("--------------------------");
            logger.warning("Your Server is running Purpur (fork of Paper).");
            logger.warning("The plugin WILL load but it MAY NOT work properly. Bear in mind that support for Purpur is NOT guaranteed.");
            logger.warning("--------------------------");
        }

        else if (!serverSoftware.equals("Spigot") && !serverSoftware.equals("Paper") && !serverSoftware.equals("CraftBukkit")){
            logger.warning("--------------------------");
            logger.warning("Your Server is not running Spigot, nor Paper (" + serverSoftware + " detected).");
            logger.warning("The plugin WILL load but you are NOT eligible for any support unless you switch to Spigot/Paper.");
            logger.warning("--------------------------");
        }

        return true;
    }
}
