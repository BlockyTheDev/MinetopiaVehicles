package nl.mtvehicles.core.infrastructure.models;

import de.tr7zw.changeme.nbtapi.NBTItem;
import nl.mtvehicles.core.infrastructure.enums.Message;
import nl.mtvehicles.core.infrastructure.helpers.TextUtils;
import nl.mtvehicles.core.infrastructure.modules.ConfigModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Abstract class for the plugin's /mtv subcommands
 */
public abstract class MTVehicleSubCommand {
    /**
     * The command sender
     */
    protected CommandSender sender;
    /**
     * Player who sent the command (may be null if the sender is not a player)
     */
    protected @Nullable Player player;
    /**
     * Whether the sender is a player
     */
    protected boolean isPlayer;
    /**
     * Arguments for the command
     */
    protected String[] arguments;
    /**
     * Whether this command may only be used by players (and not in console)
     */
    private boolean isPlayerCommand;

    public boolean onExecute(CommandSender sender, Command cmd, String s, String[] args) {
        this.sender = sender;
        this.isPlayer = sender instanceof Player;
        this.player = isPlayer ? (Player) sender : null;
        this.arguments = args;

        if (isPlayerCommand && !isPlayer) {
            sendMessage(ConfigModule.messagesConfig.getMessage(Message.NOT_FOR_CONSOLE));
            return true;
        }

        return this.execute();
    }

    /**
     * Code executed by a subcommand
     * @return True if successful
     */
    public abstract boolean execute();

    /**
     * Send a message to the command sender
     * @param message Message
     */
    public void sendMessage(String message) {
        this.sender.sendMessage(TextUtils.colorize(message));
    }

    /**
     * Check whether the command sender has a permission to use the subcommand
     * @param permission Permission necessary to use the subcommand
     * @return True if sender has the permission
     */
    public boolean checkPermission(String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }

        ConfigModule.messagesConfig.sendMessage(sender, Message.NO_PERMISSION);

        return false;
    }

    /**
     * Check whether the subcommand may only be used by players (and not in console)
     * @return True if the subcommand can't be used in console
     */
    public boolean isPlayerCommand() {
        return isPlayerCommand;
    }

    /**
     * Set whether the subcommand may only be used by players (and not in console)
     * @param playerCommand True if the subcommand can't be used in console
     */
    public void setPlayerCommand(boolean playerCommand) {
        isPlayerCommand = playerCommand;
    }

    /**
     * Check whether the player is holding a vehicle
     * @return True if the player is holding a vehicle
     */
    protected boolean isHoldingVehicle(){
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta() || !(new NBTItem(item)).hasKey("mtvehicles.kenteken")) {
            sendMessage(TextUtils.colorize(ConfigModule.messagesConfig.getMessage(Message.NO_VEHICLE_IN_HAND)));
            return false;
        }
        return true;
    }
}
