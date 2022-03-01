package nl.mtvehicles.core.events;

import nl.mtvehicles.core.commands.vehiclesubs.VehicleFuel;
import nl.mtvehicles.core.infrastructure.helpers.NBTUtils;
import nl.mtvehicles.core.infrastructure.helpers.TextUtils;
import nl.mtvehicles.core.infrastructure.modules.ConfigModule;
import nl.mtvehicles.core.infrastructure.modules.DependencyModule;
import nl.mtvehicles.core.infrastructure.modules.VersionModule;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class JerryCanClickEvent implements Listener {
    @EventHandler
    public void onJerryCanClick(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final Action action = e.getAction();
        final ItemStack item = e.getItem();

        if (e.getItem() == null
                || !e.getItem().hasItemMeta()
                || !(NBTUtils.contains(item, "mtvehicles.benzinesize"))
                || e.getClickedBlock() == null
        ) return;

        e.setCancelled(true);

        if (e.getHand() != EquipmentSlot.HAND) {
            e.getPlayer().sendMessage(TextUtils.colorize(ConfigModule.messagesConfig.getMessage("wrongHand")));
            return;
        }

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        Block clickedBlock = e.getClickedBlock();

        if (!ConfigModule.defaultConfig.canFillJerryCans(p, clickedBlock.getLocation())) return;
        final boolean isSneaking = p.isSneaking();

        if (clickedBlock.getType().toString().contains("LEVER") && ConfigModule.defaultConfig.isFillJerryCansLeverEnabled()) {
            if (isSneaking) fillWholeJerryCan(p, item);
            else fillJerryCan(p, item);
        } else if (clickedBlock.getType().toString().contains("TRIPWIRE_HOOK") && ConfigModule.defaultConfig.isFillJerryCansTripwireHookEnabled()) {
            if (isSneaking) fillWholeJerryCan(p, item);
            else fillJerryCan(p, item);
        }
    }

    private void fillJerryCan(Player p, ItemStack item){
        int benval = Integer.parseInt(NBTUtils.getString(item, "mtvehicles.benzineval"));
        int bensize = Integer.parseInt(NBTUtils.getString(item, "mtvehicles.benzinesize"));

        if (benval == bensize) ConfigModule.messagesConfig.sendMessage(p, "jerrycanFull");

        if ((benval + 1) <= bensize){
            double price = getFuelPrice();
            if (makePlayerPay(p, price)){
                p.setItemInHand(VehicleFuel.benzineItem(bensize, benval + 1));
                p.sendMessage(String.format(ConfigModule.messagesConfig.getMessage("transactionSuccessful"), DependencyModule.vault.getMoneyFormat(price)));
                playJerryCanSound(p);
            }
        }
    }

    private void fillWholeJerryCan(Player p, ItemStack item){
        int benval = Integer.parseInt(NBTUtils.getString(item, "mtvehicles.benzineval"));
        int bensize = Integer.parseInt(NBTUtils.getString(item, "mtvehicles.benzinesize"));
        if (benval == bensize) ConfigModule.messagesConfig.sendMessage(p, "jerrycanFull");

        int difference = bensize - benval;
        double price = getFuelPrice(difference);
        if (makePlayerPay(p, price)){
            p.setItemInHand(VehicleFuel.benzineItem(bensize, bensize));
            p.sendMessage(String.format(ConfigModule.messagesConfig.getMessage("transactionSuccessful"), DependencyModule.vault.getMoneyFormat(price)));
            playJerryCanSound(p);
        }
    }

    private boolean makePlayerPay(Player p, double price){ //returns true if payed/doesn't have to, false if didn't pay/error
        if (!ConfigModule.defaultConfig.isFillJerryCanPriceEnabled()) return true; //it isn't enabled, so just fill the jerrycan...

        return DependencyModule.vault.withdrawMoneyPlayer(p, price);
    }

    private double getFuelPrice(){
        return ConfigModule.defaultConfig.getFillJerryCanPrice();
    }

    private double getFuelPrice(int litres){
        return litres * ConfigModule.defaultConfig.getFillJerryCanPrice();
    }

    private void playJerryCanSound(Player p){
        if (!ConfigModule.defaultConfig.jerryCanPlaySound()) return;

        if (VersionModule.serverVersion.equals("v1_12_R1")) { //1.12 has different names
            try {
                p.getWorld().playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_PLING"), 3.0F, 0.5F);
            } catch (IllegalArgumentException e) {
                e.printStackTrace(); //The sound could not be played, hmmm.
            }
        } else {
            try {
                p.getWorld().playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_BLOCK_PLING"), 3.0F, 0.5F);
            } catch (IllegalArgumentException e) {
                e.printStackTrace(); //The sound could not be played, hmmm.
            }
        }
    }
}
