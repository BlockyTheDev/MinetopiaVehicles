package nl.mtvehicles.core.commands.vehiclesubs;

import nl.mtvehicles.core.infrastructure.enums.Message;
import nl.mtvehicles.core.infrastructure.models.MTVehicleSubCommand;
import nl.mtvehicles.core.infrastructure.models.VehicleUtils;
import nl.mtvehicles.core.infrastructure.modules.ConfigModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * <b>/vehicle givecar %player% %uuid%</b> - add a vehicle to a player.
 */
public class VehicleGiveCar extends MTVehicleSubCommand {
    public VehicleGiveCar() {
        this.setPlayerCommand(false);
    }

    @Override
    public boolean execute() {
        if (!checkPermission("mtvehicles.givecar")) return true;

        if (arguments.length != 3) {
            sendMessage(ConfigModule.messagesConfig.getMessage(Message.USE_GIVE_CAR));
            return true;
        }

        Player of = Bukkit.getPlayer(arguments[1]);

        String carUuid = arguments[2];

        if (of == null || !of.hasPlayedBefore()) {
            sendMessage(ConfigModule.messagesConfig.getMessage(Message.PLAYER_NOT_FOUND));
            return true;
        }

        ItemStack car = VehicleUtils.getItemByUUID(of, carUuid);

        if (car == null){
            sender.sendMessage(ConfigModule.messagesConfig.getMessage(Message.GIVE_CAR_NOT_FOUND));
            return true;
        }
        of.getInventory().addItem(car);
        sender.sendMessage(ConfigModule.messagesConfig.getMessage(Message.GIVE_CAR_SUCCESS).replace("%p%", of.getName()));

        return true;
    }
}
