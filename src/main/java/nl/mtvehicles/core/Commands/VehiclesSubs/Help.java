package nl.mtvehicles.core.Commands.VehiclesSubs;

import nl.mtvehicles.core.Infrastructure.Models.MTVehicleSubCommand;
import nl.mtvehicles.core.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Help extends MTVehicleSubCommand {
    @Override
    public boolean execute(CommandSender sender, Command cmd, String s, String[] args) {
        for (String message : Main.messagesConfig.getConfig().getStringList("help")) {
            sendMessage(message);
        }

        return false;
    }
}
