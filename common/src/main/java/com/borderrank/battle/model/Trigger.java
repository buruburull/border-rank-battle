package com.borderrank.battle.model;

import org.bukkit.Material;

/**
 * Represents a runtime trigger instance used in loadouts and during matches.
 */
public class Trigger {
    private final String id;
    private final TriggerData data;
    private int slotIndex;

    /**
     * Constructs a Trigger instance.
     *
     * @param id the trigger ID
     * @param data the trigger data definition
     * @param slotIndex the slot index (0-7)
     */
    public Trigger(String id, TriggerData data, int slotIndex) {
        this.id = id;
        this.data = data;
        this.slotIndex = slotIndex;
    }

    /**
     * Gets the trigger ID.
     *
     * @return the trigger ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the trigger data definition.
     *
     * @return the trigger data
     */
    public TriggerData getData() {
        return data;
    }

    /**
     * Gets the slot index.
     *
     * @return the slot index (0-7)
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    /**
     * Sets the slot index.
     *
     * @param slotIndex the new slot index (0-7)
     */
    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    /**
     * Gets the trigger name from its data.
     *
     * @return the trigger name
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the cost from its data.
     *
     * @return the cost
     */
    public int getCost() {
        return data.getCost();
    }

    /**
     * Gets the trion usage from its data.
     *
     * @return the trion usage
     */
    public int getTrionUse() {
        return data.getTrionUse();
    }

    /**
     * Gets the trion sustain rate from its data.
     *
     * @return the trion sustain rate
     */
    public double getTrionSustain() {
        return data.getTrionSustain();
    }

    /**
     * Gets the Minecraft item representation from its data.
     *
     * @return the Minecraft item
     */
    public Material getMcItem() {
        return data.getMcItem();
    }

    /**
     * Gets the cooldown from its data.
     *
     * @return the cooldown in ticks
     */
    public int getCooldown() {
        return data.getCooldown();
    }

    /**
     * Gets the category from its data.
     *
     * @return the trigger category
     */
    public TriggerCategory getCategory() {
        return data.getCategory();
    }

    @Override
    public String toString() {
        return "Trigger{" +
                "id='" + id + '\'' +
                ", name='" + data.getName() + '\'' +
                ", slotIndex=" + slotIndex +
                '}';
    }
}
