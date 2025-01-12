package nl.mtvehicles.core.commands.vehiclesubs;

import de.tr7zw.changeme.nbtapi.NBTItem;
import nl.mtvehicles.core.infrastructure.enums.Message;
import nl.mtvehicles.core.infrastructure.models.MTVehicleSubCommand;
import nl.mtvehicles.core.infrastructure.models.Vehicle;
import nl.mtvehicles.core.infrastructure.models.VehicleUtils;
import nl.mtvehicles.core.infrastructure.modules.ConfigModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * <b>/vehicle addrider %player%</b> - add a player who may steer the held vehicle.
 */
public class VehicleAddRider extends MTVehicleSubCommand {
    public VehicleAddRider() {
        this.setPlayerCommand(true);
    }

    @Override
    public boolean execute() {
        if (!isHoldingVehicle()) return true;

        ItemStack item = player.getInventory().getItemInMainHand();
        NBTItem nbt = new NBTItem(item);

        if (arguments.length != 2) {
            sendMessage(ConfigModule.messagesConfig.getMessage(Message.USE_ADD_RIDER));
            return true;
        }

        Player offlinePlayer = Bukkit.getPlayer(arguments[1]);
        String licensePlate = nbt.getString("mtvehicles.kenteken");

        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            sendMessage(ConfigModule.messagesConfig.getMessage(Message.PLAYER_NOT_FOUND));
            return true;
        }

        Vehicle vehicle = VehicleUtils.getByLicensePlate(licensePlate);

        assert vehicle != null;
        List<String> riders = vehicle.getRiders();
        riders.add(offlinePlayer.getUniqueId().toString());
        vehicle.setRiders(riders);
        vehicle.save();

        sendMessage(ConfigModule.messagesConfig.getMessage(Message.MEMBER_CHANGE));

        return true;
    }
}
