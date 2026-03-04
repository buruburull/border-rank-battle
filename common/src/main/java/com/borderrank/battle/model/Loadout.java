package com.borderrank.battle.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's loadout with up to 8 trigger slots.
 * Slots 0-3 are main triggers, slots 4-7 are sub triggers.
 */
public class Loadout {
    private static final int SLOT_COUNT = 8;
    private static final int MAIN_SLOTS = 4;

    private final UUID playerUuid;
    private final String name;
    private final List<String> slots;
    private int totalCost;

    /**
     * Constructs a Loadout instance.
     *
     * @param playerUuid the player's UUID
     * @param name the loadout name
     */
    public Loadout(UUID playerUuid, String name) {
        this.playerUuid = playerUuid;
        this.name = name;
        this.slots = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.slots.add("");
        }
        this.totalCost = 0;
    }

    /**
     * Constructs a Loadout instance with initial slot data.
     *
     * @param playerUuid the player's UUID
     * @param name the loadout name
     * @param slots the initial slots (must be size 8)
     */
    public Loadout(UUID playerUuid, String name, String[] slots) {
        if (slots.length != SLOT_COUNT) {
            throw new IllegalArgumentException("Slots array must have exactly " + SLOT_COUNT + " elements");
        }
        this.playerUuid = playerUuid;
        this.name = name;
        this.slots = new ArrayList<>(Arrays.asList(slots));
        this.totalCost = 0;
    }

    /**
     * Gets the player's UUID.
     *
     * @return the player UUID
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * Gets the loadout name.
     *
     * @return the loadout name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the trigger ID at the specified slot.
     *
     * @param slot the slot index (0-7)
     * @return the trigger ID, or null if empty
     * @throws IndexOutOfBoundsException if slot is out of range
     */
    public String getSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            throw new IndexOutOfBoundsException("Slot must be between 0 and " + (SLOT_COUNT - 1));
        }
        return slots.get(slot);
    }

    /**
     * Sets a trigger at the specified slot.
     *
     * @param slot the slot index (0-7)
     * @param triggerId the trigger ID, or empty string to clear the slot
     * @throws IndexOutOfBoundsException if slot is out of range
     */
    public void setSlot(int slot, String triggerId) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            throw new IndexOutOfBoundsException("Slot must be between 0 and " + (SLOT_COUNT - 1));
        }
        slots.set(slot, triggerId);
    }

    /**
     * Gets all slots.
     *
     * @return the slots list
     */
    public List<String> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    /**
     * Gets the total cost of the loadout.
     *
     * @return the total cost
     */
    public int getTotalCost() {
        return totalCost;
    }

    /**
     * Calculates the total cost of this loadout based on trigger data.
     *
     * @param triggerDataMap a map of trigger ID to TriggerData
     * @return the calculated total cost
     */
    public int calculateTotalCost(Map<String, TriggerData> triggerDataMap) {
        int cost = 0;
        for (String triggerId : slots) {
            if (triggerId != null && !triggerId.isEmpty()) {
                TriggerData triggerData = triggerDataMap.get(triggerId);
                if (triggerData != null) {
                    cost += triggerData.getCost();
                }
            }
        }
        this.totalCost = cost;
        return cost;
    }

    /**
     * Checks if this loadout is active (has at least one non-empty slot).
     *
     * @return true if any slot is non-empty, false otherwise
     */
    public boolean isActive() {
        for (String slot : slots) {
            if (slot != null && !slot.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this loadout is valid based on the maximum TP.
     *
     * @param maxTP the maximum TP available
     * @return true if the total cost is within the limit, false otherwise
     */
    public boolean isValid(int maxTP) {
        return totalCost <= maxTP;
    }

    /**
     * Gets all main triggers (slots 0-3).
     *
     * @return a list of main trigger IDs (excludes null entries)
     */
    public List<String> getMainTriggers() {
        List<String> mainTriggers = new ArrayList<>();
        for (int i = 0; i < MAIN_SLOTS; i++) {
            String slot = slots.get(i);
            if (slot != null && !slot.isEmpty()) {
                mainTriggers.add(slot);
            }
        }
        return mainTriggers;
    }

    /**
     * Gets all sub triggers (slots 4-7).
     *
     * @return a list of sub trigger IDs (excludes null entries)
     */
    public List<String> getSubTriggers() {
        List<String> subTriggers = new ArrayList<>();
        for (int i = MAIN_SLOTS; i < SLOT_COUNT; i++) {
            String slot = slots.get(i);
            if (slot != null && !slot.isEmpty()) {
                subTriggers.add(slot);
            }
        }
        return subTriggers;
    }

    /**
     * Gets the count of non-empty slots.
     *
     * @return the number of filled slots
     */
    public int getFilledSlotCount() {
        int count = 0;
        for (String slot : slots) {
            if (slot != null && !slot.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "Loadout{" +
                "name='" + name + '\'' +
                ", totalCost=" + totalCost +
                ", filledSlots=" + getFilledSlotCount() +
                '}';
    }
}
