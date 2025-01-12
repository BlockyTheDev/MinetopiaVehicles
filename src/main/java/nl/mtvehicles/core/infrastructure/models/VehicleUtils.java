package nl.mtvehicles.core.infrastructure.models;

import de.tr7zw.changeme.nbtapi.NBTItem;
import nl.mtvehicles.core.infrastructure.annotations.ToDo;
import nl.mtvehicles.core.infrastructure.dataconfig.DefaultConfig;
import nl.mtvehicles.core.infrastructure.dataconfig.VehicleDataConfig;
import nl.mtvehicles.core.infrastructure.enums.InventoryTitle;
import nl.mtvehicles.core.infrastructure.enums.Message;
import nl.mtvehicles.core.infrastructure.helpers.ItemFactory;
import nl.mtvehicles.core.infrastructure.helpers.ItemUtils;
import nl.mtvehicles.core.infrastructure.helpers.TextUtils;
import nl.mtvehicles.core.infrastructure.modules.ConfigModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Useful methods for vehicles
 * @see Vehicle
 */
public final class VehicleUtils {

    /**
     * A private constructor - makes this a "static class"
     */
    private VehicleUtils(){}

    /**
     * HashMap containing information about which trunk a player has opened (determined by vehicle's license plate)
     */
    public static HashMap<Player, String> openedTrunk = new HashMap<>();

    /**
     * Spawn a vehicle
     * @param licensePlate Vehicle's license plate
     * @param location Location where the vehicle should be spawned
     *
     * @throws IllegalArgumentException If vehicle with given license plate does not exist
     */
    public static void spawnVehicle(String licensePlate, Location location) throws IllegalArgumentException {
        if (!existsByLicensePlate(licensePlate)) throw new IllegalArgumentException("Vehicle does not exists.");

        ArmorStand standSkin = location.getWorld().spawn(location, ArmorStand.class);
        standSkin.setVisible(false);
        standSkin.setCustomName("MTVEHICLES_SKIN_" + licensePlate);
        standSkin.getEquipment().setHelmet(
                ItemUtils.getVehicleItem(
                        ItemUtils.getMaterial(ConfigModule.vehicleDataConfig.get(licensePlate, VehicleDataConfig.Option.SKIN_ITEM).toString()),
                        (int) ConfigModule.vehicleDataConfig.get(licensePlate, VehicleDataConfig.Option.SKIN_DAMAGE),
                        false,
                        ConfigModule.vehicleDataConfig.get(licensePlate, VehicleDataConfig.Option.NAME).toString(),
                        licensePlate));

        ArmorStand standMain = location.getWorld().spawn(location, ArmorStand.class);
        standMain.setVisible(false);
        standMain.setCustomName("MTVEHICLES_MAIN_" + licensePlate);

        Vehicle vehicle = getByLicensePlate(licensePlate);

        List<Map<String, Double>> seats = (List<Map<String, Double>>) vehicle.getVehicleData().get("seats");
        Map<String, Double> mainSeat = seats.get(0);
        Location locationMainSeat = new Location(location.getWorld(), location.getX() + mainSeat.get("x"), location.getY() + mainSeat.get("y"), location.getZ() + mainSeat.get("z"));
        ArmorStand standMainSeat = locationMainSeat.getWorld().spawn(locationMainSeat, ArmorStand.class);
        standMainSeat.setCustomName("MTVEHICLES_MAINSEAT_" + licensePlate);
        standMainSeat.setGravity(false);
        standMainSeat.setVisible(false);

        if (ConfigModule.vehicleDataConfig.getType(licensePlate).isHelicopter()) {
            List<Map<String, Double>> helicopterBlades = (List<Map<String, Double>>) vehicle.getVehicleData().get("wiekens");
            Map<?, ?> blade = helicopterBlades.get(0);
            Location locationBlade = new Location(location.getWorld(), location.getX() + (double) blade.get("z"), location.getY() + (double) blade.get("y"), location.getZ() + (double) blade.get("x"));
            ArmorStand standRotors = locationBlade.getWorld().spawn(locationBlade, ArmorStand.class);
            standRotors.setCustomName("MTVEHICLES_WIEKENS_" + licensePlate);
            standRotors.setGravity(false);
            standRotors.setVisible(false);

            if ((boolean) ConfigModule.defaultConfig.get(DefaultConfig.Option.HELICOPTER_BLADES_ALWAYS_ON)) {
                ItemStack rotor = (new ItemFactory(Material.getMaterial("DIAMOND_HOE"))).setDurability((short) 1058).setName(TextUtils.colorize("&6Wieken")).setNBT("mtvehicles.kenteken", licensePlate).toItemStack();
                ItemMeta itemMeta = rotor.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(TextUtils.colorize("&a"));
                lore.add(TextUtils.colorize("&a" + licensePlate));
                lore.add(TextUtils.colorize("&a"));
                itemMeta.setLore(lore);
                itemMeta.setUnbreakable(true);
                rotor.setItemMeta(itemMeta);
                standRotors.setHelmet((ItemStack) blade.get("item"));
            }
        }
    }

    /**
     * Get license plate from a vehicle item
     * @param item Vehicle as Item
     * @return Vehicle's license plate
     */
    public static String getLicensePlate(ItemStack item){
        NBTItem nbt = new NBTItem(item);
        return nbt.getString("mtvehicles.kenteken");
    }

    /**
     * Create a vehicle and get its item by UUID (UUID may be found in vehicles.yml)
     * @param p Vehicle's owner
     * @param uuid Vehicle's UUID (UUID may be found in vehicles.yml)
     * @return Vehicle item
     */
    public static ItemStack getItemByUUID(Player p, String uuid) {
        List<Map<?, ?>> vehicles = ConfigModule.vehiclesConfig.getVehicles();
        List<Map<?, ?>> matchedVehicles = new ArrayList<>();
        for (Map<?, ?> configVehicle : vehicles) {
            List<Map<?, ?>> skins = (List<Map<?, ?>>) configVehicle.get("cars");
            for (Map<?, ?> skin : skins) {
                if (skin.get("uuid") != null) {
                    if (skin.get("uuid").equals(uuid)) {
                        String nbtVal;
                        if (skin.get("nbtValue") == null) {
                            nbtVal = "null";
                        } else {
                            nbtVal = skin.get("nbtValue").toString();
                        }
                        ItemStack is = ItemUtils.getVehicleItem(ItemUtils.getMaterial(skin.get("SkinItem").toString()), (int) skin.get("itemDamage"), ((String) skin.get("name")), "mtcustom", nbtVal);
                        NBTItem nbt = new NBTItem(is);
                        String licensePlate = nbt.getString("mtvehicles.kenteken");
                        matchedVehicles.add(configVehicle);

                        Vehicle vehicle = new Vehicle();
                        List<String> members = ConfigModule.vehicleDataConfig.getMembers(licensePlate);
                        List<String> riders = ConfigModule.vehicleDataConfig.getRiders(licensePlate);
                        List<String> trunkData = ConfigModule.vehicleDataConfig.getTrunkData(licensePlate);

                        vehicle.setLicensePlate(licensePlate);
                        vehicle.setName((String) skin.get("name"));
                        vehicle.setVehicleType((String) configVehicle.get("vehicleType"));
                        vehicle.setSkinDamage((Integer) skin.get("itemDamage"));
                        vehicle.setSkinItem((String) skin.get("SkinItem"));
                        vehicle.setGlow(false);
                        vehicle.setBenzineEnabled((Boolean) configVehicle.get("benzineEnabled"));
                        vehicle.setFuel(100);
                        vehicle.setHornEnabled((Boolean) configVehicle.get("hornEnabled"));
                        vehicle.setHealth((double) configVehicle.get("maxHealth"));
                        vehicle.setTrunk((Boolean) configVehicle.get("kofferbakEnabled"));
                        vehicle.setTrunkRows(1);
                        vehicle.setFuelUsage(0.01);
                        vehicle.setTrunkData(trunkData);
                        vehicle.setAccelerationSpeed((Double) configVehicle.get("acceleratieSpeed"));
                        vehicle.setMaxSpeed((Double) configVehicle.get("maxSpeed"));
                        vehicle.setBrakingSpeed((Double) configVehicle.get("brakingSpeed"));
                        vehicle.setFrictionSpeed((Double) configVehicle.get("aftrekkenSpeed"));
                        vehicle.setRotateSpeed((Integer) configVehicle.get("rotateSpeed"));
                        vehicle.setMaxSpeedBackwards((Double) configVehicle.get("maxSpeedBackwards"));
                        vehicle.setOwner(p.getUniqueId().toString());
                        vehicle.setRiders(riders);
                        vehicle.setMembers(members);
                        vehicle.setNbtValue(((String) skin.get("nbtValue")));
                        vehicle.save();
                        return is;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check whether horn is enabled on this vehicle.
     * @param damage The vehicle item's durability
     * @return True if horn is enabled
     */
    public static boolean getHornByDamage(int damage){
        List<Map<?, ?>> vehicles = ConfigModule.vehiclesConfig.getVehicles();
        for (Map<?, ?> configVehicle : vehicles) {
            List<Map<?, ?>> skins = (List<Map<?, ?>>) configVehicle.get("cars");
            for (Map<?, ?> skin : skins) {
                if (skin.get("itemDamage") != null) {
                    if (skin.get("itemDamage").equals(damage)) {
                        return (boolean) configVehicle.get("hornEnabled");
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check what is the max health of this vehicle.
     * @param damage The vehicle item's durability
     * @return Max health of the vehicle
     */
    public static double getMaxHealthByDamage(int damage){
        List<Map<?, ?>> vehicles = ConfigModule.vehiclesConfig.getVehicles();
        for (Map<?, ?> configVehicle : vehicles) {
            List<Map<?, ?>> skins = (List<Map<?, ?>>) configVehicle.get("cars");
            for (Map<?, ?> skin : skins) {
                if (skin.get("itemDamage") != null) {
                    if (skin.get("itemDamage").equals(damage)) {
                        return (double) configVehicle.get("maxHealth");
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Get a vehicle item by UUID. <b>Does not create a new vehicle - just for aesthetic purposes.</b> (Otherwise, use {@link #getItemByUUID(Player, String)})
     * @param carUUID Vehicle's UUID (UUID may be found in vehicles.yml)
     * @return The vehicle item - just aesthetic
     *
     */
    public static ItemStack getCarItem(String carUUID) {
        List<Map<?, ?>> vehicles = ConfigModule.vehiclesConfig.getVehicles();
        List<Map<?, ?>> matchedVehicles = new ArrayList<>();
        for (Map<?, ?> configVehicle : vehicles) {
            List<Map<?, ?>> skins = (List<Map<?, ?>>) configVehicle.get("cars");
            for (Map<?, ?> skin : skins) {
                if (skin.get("uuid") != null) {
                    if (skin.get("uuid").equals(carUUID)) {
                        if (skin.get("uuid") != null) {
                            ItemStack is = ItemUtils.getVehicleItem(ItemUtils.getMaterial(skin.get("SkinItem").toString()), (int) skin.get("itemDamage"), ((String) skin.get("name")));
                            matchedVehicles.add(configVehicle);
                            return is;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check whether an entity is a vehicle
     * @param entity Checked entity
     * @return True if the entity is a vehicle
     */
    public static boolean isVehicle(Entity entity){
        return entity.getCustomName() != null && entity instanceof ArmorStand && entity.getCustomName().contains("MTVEHICLES");
    }

    /**
     * Get license plate of an entity (which should be a vehicle - see {@link #isVehicle(Entity)}.
     * @param entity Vehicle's main armor stand
     * @return Vehicle's license plate
     */
    public static String getLicensePlate(Entity entity){
        final String name = entity.getCustomName();
        if (name.split("_").length > 1) {
            return name.split("_")[2];
        }
        return null;
    }

    /**
     * Get the UUID of a car by its license plate
     * @param licensePlate Vehicle's license plate
     * @return Vehicle's UUID
     */
    public static String getCarUUID(String licensePlate) {
        if (!existsByLicensePlate(licensePlate)) return null;

        Map<?, ?> vehicleData = ConfigModule.vehicleDataConfig.getConfig()
                .getConfigurationSection(String.format("vehicle.%s", licensePlate)).getValues(true);
        List<Map<?, ?>> vehicles = ConfigModule.vehiclesConfig.getVehicles();
        List<Map<?, ?>> matchedVehicles = new ArrayList<>();
        for (Map<?, ?> configVehicle : vehicles) {
            List<Map<?, ?>> skins = (List<Map<?, ?>>) configVehicle.get("cars");
            for (Map<?, ?> skin : skins) {
                if (skin.get("itemDamage").equals(vehicleData.get("skinDamage"))) {
                    if (skin.get("SkinItem").equals(vehicleData.get("skinItem"))) {
                        if (skin.get("nbtValue") != null) {
                            if (skin.get("nbtValue").equals(vehicleData.get("nbtValue"))) {
                                matchedVehicles.add(configVehicle);
                                return skin.get("uuid").toString();
                            }
                        } else {
                            matchedVehicles.add(configVehicle);
                            return skin.get("uuid").toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the Vehicle instance by a vehicle's license place
     * @param licensePlate Vehicle's license plate
     * @return Vehicle instance
     *
     * @see Vehicle
     */
    @ToDo("Beautify the code inside this method.")
    public static Vehicle getByLicensePlate(String licensePlate) {
        if (!existsByLicensePlate(licensePlate)) return null;

        ConfigModule.vehicleDataConfig.reload();

        Map<?, ?> vehicleData = ConfigModule.vehicleDataConfig.getConfig()
                .getConfigurationSection(String.format("vehicle.%s", licensePlate)).getValues(true);
        List<Map<?, ?>> vehicles = ConfigModule.vehiclesConfig.getVehicles();
        List<Map<?, ?>> matchedVehicles = new ArrayList<>();
        for (Map<?, ?> configVehicle : vehicles) {
            List<Map<?, ?>> skins = (List<Map<?, ?>>) configVehicle.get("cars");
            for (Map<?, ?> skin : skins) {
                if (skin.get("itemDamage").equals(vehicleData.get("skinDamage"))) {
                    if (skin.get("SkinItem").equals(vehicleData.get("skinItem"))) {
                        if (skin.get("nbtValue") != null) {
                            if (skin.get("nbtValue").equals(vehicleData.get("nbtValue"))) {
                                matchedVehicles.add(configVehicle);
                            }
                        } else {
                            matchedVehicles.add(configVehicle);
                        }
                    }
                }
            }
        }
        if (matchedVehicles.size() == 0) return null;
        if (matchedVehicles.size() > 1) return null;
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleData(matchedVehicles.get(0));
        vehicle.setLicensePlate(licensePlate);
        vehicle.setOpen(false);
        vehicle.setName((String) vehicleData.get("name"));
        vehicle.setVehicleType((String) vehicleData.get("vehicleType"));
        vehicle.setSkinDamage((Integer) vehicleData.get("skinDamage"));
        vehicle.setSkinItem((String) vehicleData.get("skinItem"));
        vehicle.setGlow((Boolean) vehicleData.get("isGlow"));
        vehicle.setHornEnabled(ConfigModule.vehicleDataConfig.isHornSet(licensePlate) ? (boolean) vehicleData.get("hornEnabled") : ConfigModule.vehicleDataConfig.isHornEnabled(licensePlate));
        vehicle.setHealth(ConfigModule.vehicleDataConfig.isHealthSet(licensePlate) ? (double) vehicleData.get("health") : ConfigModule.vehicleDataConfig.getHealth(licensePlate));
        vehicle.setBenzineEnabled((Boolean) vehicleData.get("benzineEnabled"));
        vehicle.setFuel((Double) vehicleData.get("benzine"));
        vehicle.setFuelUsage((Double) vehicleData.get("benzineVerbruik"));
        vehicle.setTrunk((Boolean) vehicleData.get("kofferbak"));
        vehicle.setTrunkRows((Integer) vehicleData.get("kofferbakRows"));
        vehicle.setTrunkData((List<String>) vehicleData.get("kofferbakData"));
        vehicle.setAccelerationSpeed((Double) vehicleData.get("acceleratieSpeed"));
        vehicle.setMaxSpeed((Double) vehicleData.get("maxSpeed"));
        vehicle.setBrakingSpeed((Double) vehicleData.get("brakingSpeed"));
        vehicle.setFrictionSpeed((Double) vehicleData.get("aftrekkenSpeed"));
        vehicle.setRotateSpeed((Integer) vehicleData.get("rotateSpeed"));
        vehicle.setMaxSpeedBackwards((Double) vehicleData.get("maxSpeedBackwards"));
        vehicle.setOwner((String) vehicleData.get("owner"));
        vehicle.setRiders((List<String>) vehicleData.get("riders"));
        vehicle.setMembers((List<String>) vehicleData.get("members"));
        return vehicle;
    }

    /**
     * Check whether this vehicle exists in the database (vehicleData.yml)
     * @param licensePlate Vehicle's license plate
     * @return True if vehicle is in the database (vehicleData.yml)
     */
    public static boolean existsByLicensePlate(String licensePlate) {
        return ConfigModule.vehicleDataConfig.getConfig().get(String.format("vehicle.%s", licensePlate)) != null;
    }

    /**
     * Check whether a player can ride/drive the vehicle.
     * @param player Player
     * @param licensePlate Vehicle's license plate
     * @return True if player is the vehicle's set rider.
     */
    public static boolean canRide(Player player, String licensePlate) {
        return ConfigModule.vehicleDataConfig.getRiders(licensePlate).contains(player.getUniqueId().toString());
    }

    /**
     * Check whether a player can sit in the vehicle.
     * @param player Player
     * @param licensePlate Vehicle's license plate
     * @return True if player is the vehicle's set passenger/member.
     */
    public static boolean canSit(Player player, String licensePlate) {
        return ConfigModule.vehicleDataConfig.getMembers(licensePlate).contains(player.getUniqueId().toString());
    }

    /**
     * Get the UUID of the vehicle's owner
     * @param licensePlate Vehicle's license plate
     * @return UUID of vehicle's owner
     */
    public static UUID getOwnerUUID(String licensePlate) {
        if (ConfigModule.vehicleDataConfig.getConfig().getString("vehicle." + licensePlate + ".owner") == null) {
            return null;
        }
        return UUID.fromString(ConfigModule.vehicleDataConfig.getConfig().getString("vehicle." + licensePlate + ".owner"));
    }

    /**
     * Open a vehicle's trunk to a player
     * @param p Player who is opening the trunk
     * @param license Vehicle's license plate
     */
    public static void openTrunk(Player p, String license) {
        if ((boolean) ConfigModule.defaultConfig.get(DefaultConfig.Option.TRUNK_ENABLED)) {
            if (VehicleUtils.getByLicensePlate(license) == null) {
                ConfigModule.messagesConfig.sendMessage(p, Message.VEHICLE_NOT_FOUND);
                return;
            }

            if (VehicleUtils.getByLicensePlate(license).isOwner(p) || p.hasPermission("mtvehicles.kofferbak")) {
                ConfigModule.configList.forEach(Config::reload);
                Inventory inv = Bukkit.createInventory(null, (int) ConfigModule.vehicleDataConfig.get(license, VehicleDataConfig.Option.TRUNK_ROWS) * 9, InventoryTitle.VEHICLE_TRUNK.getStringTitle());

                if (ConfigModule.vehicleDataConfig.get(license, VehicleDataConfig.Option.TRUNK_DATA) != null) {
                    List<ItemStack> chestContentsFromConfig = (List<ItemStack>) ConfigModule.vehicleDataConfig.get(license, VehicleDataConfig.Option.TRUNK_DATA);

                    for (ItemStack item : chestContentsFromConfig) {
                        if (item != null) inv.addItem(item);
                    }
                }

                openedTrunk.put(p, license);
                p.openInventory(inv);

            } else {
                p.sendMessage(TextUtils.colorize(ConfigModule.messagesConfig.getMessage(Message.VEHICLE_NO_RIDER_TRUNK).replace("%p%", VehicleUtils.getByLicensePlate(license).getOwnerName())));
            }
        }
    }

    /**
     * Check whether a player is inside a vehicle
     * @param p Player
     * @return True if player is inside any MTV vehicle
     */
    public static boolean isInsideVehicle(Player p){
        if (p == null) return false;
        if (!p.isInsideVehicle()) return false;
        return VehicleUtils.isVehicle(p.getVehicle());
    }

    /**
     * Get all the vehicle's set drivers/riders.
     * @param licensePlate Vehicle's license plate
     * @return String of all the drivers/riders separated by commas
     *
     * @deprecated Use {@link #canRide(Player, String)} instead.
     */
    @Deprecated
    public static String getRidersAsString(String licensePlate) {
        StringBuilder sb = new StringBuilder();
        for (String s : ConfigModule.vehicleDataConfig.getConfig().getStringList("vehicle." + licensePlate + ".riders")) {
            if (!UUID.fromString(s).equals(getOwnerUUID(licensePlate))) {
                sb.append(Bukkit.getOfflinePlayer(UUID.fromString(s)).getName()).append(", ");
            }
        }
        if (sb.toString().isEmpty()) {
            sb.append("Niemand");
        }
        return sb.toString();
    }

    /**
     * Pick up a vehicle and put it to player's inventory
     * @param license Vehicle's license plate
     * @param player Player
     */
    public static void pickupVehicle(String license, Player player) {
        if (getByLicensePlate(license) == null) {
            for (World world : Bukkit.getServer().getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getCustomName() != null && entity.getCustomName().contains(license)) {
                        ArmorStand test = (ArmorStand) entity;
                        if (test.getCustomName().contains("MTVEHICLES_SKIN_" + license)) {
                            if (!TextUtils.checkInvFull(player)) {
                                player.getInventory().addItem(test.getHelmet());
                            } else {
                                ConfigModule.messagesConfig.sendMessage(player, Message.INVENTORY_FULL);
                                return;
                            }
                        }
                        test.remove();
                    }
                }
            }
            ConfigModule.messagesConfig.sendMessage(player, Message.VEHICLE_NOT_FOUND);
            return;
        }
        if (getByLicensePlate(license).isOwner(player) && !((boolean) ConfigModule.defaultConfig.get(DefaultConfig.Option.CAR_PICKUP)) || player.hasPermission("mtvehicles.oppakken")) {
            for (World world : Bukkit.getServer().getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getCustomName() != null && entity.getCustomName().contains(license)) {
                        ArmorStand test = (ArmorStand) entity;
                        if (test.getCustomName().contains("MTVEHICLES_SKIN_" + license)) {
                            if (!TextUtils.checkInvFull(player)) {
                                player.getInventory().addItem(test.getHelmet());
                                player.sendMessage(TextUtils.colorize(ConfigModule.messagesConfig.getMessage(Message.VEHICLE_PICKUP).replace("%p%", getByLicensePlate(license).getOwnerName())));
                            } else {
                                ConfigModule.messagesConfig.sendMessage(player, Message.INVENTORY_FULL);
                                return;
                            }
                        }
                        test.remove();
                    }
                }
            }
        } else {
            if ((boolean) ConfigModule.defaultConfig.get(DefaultConfig.Option.CAR_PICKUP)) {
                player.sendMessage(TextUtils.colorize(ConfigModule.messagesConfig.getMessage(Message.CANNOT_DO_THAT_HERE)));
                return;
            }
            player.sendMessage(TextUtils.colorize(ConfigModule.messagesConfig.getMessage(Message.VEHICLE_NO_OWNER_PICKUP).replace("%p%", getByLicensePlate(license).getOwnerName())));
            return;
        }
    }
}
