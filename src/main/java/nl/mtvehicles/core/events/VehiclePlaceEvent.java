package nl.mtvehicles.core.events;

import nl.mtvehicles.core.events.interfaces.HasVehicle;
import nl.mtvehicles.core.events.interfaces.IsCancellable;
import nl.mtvehicles.core.infrastructure.models.MTVEvent;
import nl.mtvehicles.core.infrastructure.models.Vehicle;
import nl.mtvehicles.core.infrastructure.models.VehicleUtils;
import org.bukkit.Location;

/**
 * On vehicle place
 */
public class VehiclePlaceEvent extends MTVEvent implements IsCancellable, HasVehicle {
    private Location location;
    private String licensePlate;

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public String getLicensePlate(){
        return licensePlate;
    }

    @Override
    public Vehicle getVehicle(){
        return VehicleUtils.getByLicensePlate(licensePlate);
    }

    @Override
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    /**
     * Get the location where vehicle is being placed
     * @return Placement location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Set a new location where the vehicle will be placed
     * @param location New placement location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

}
