package com.borderrank.battle.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a team in the Border Rank Battle game.
 */
public class Team {
    private String name;
    private UUID leaderId;
    private final Set<UUID> members = new HashSet<>();
    private int teamRP;

    /**
     * Constructs a Team instance.
     *
     * @param name the team name
     * @param leaderId the UUID of the team leader
     */
    public Team(String name, UUID leaderId) {
        this.name = name;
        this.leaderId = leaderId;
        this.members.add(leaderId);
    }

    /**
     * Gets the team name.
     *
     * @return the team name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the team leader's UUID.
     *
     * @return the leader UUID
     */
    public UUID getLeaderId() {
        return leaderId;
    }

    /**
     * Gets all members of the team.
     *
     * @return an unmodifiable set of member UUIDs
     */
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Gets the team's RP value.
     *
     * @return the team RP
     */
    public int getTeamRP() {
        return teamRP;
    }

    /**
     * Sets the team's RP value.
     *
     * @param teamRP the new team RP
     */
    public void setTeamRP(int teamRP) {
        this.teamRP = teamRP;
    }

    /**
     * Adds a member to the team.
     *
     * @param uuid the UUID of the member to add
     * @return true if the member was added, false if already present
     */
    public boolean addMember(UUID uuid) {
        return members.add(uuid);
    }

    /**
     * Removes a member from the team.
     * Note: The team leader cannot be removed.
     *
     * @param uuid the UUID of the member to remove
     * @return true if the member was removed, false if not found or if trying to remove the leader
     */
    public boolean removeMember(UUID uuid) {
        if (uuid.equals(leaderId)) {
            return false;
        }
        return members.remove(uuid);
    }

    /**
     * Checks if a player is the team leader.
     *
     * @param uuid the player's UUID
     * @return true if the player is the leader, false otherwise
     */
    public boolean isLeader(UUID uuid) {
        return leaderId.equals(uuid);
    }

    /**
     * Checks if a player is a member of this team.
     *
     * @param uuid the player's UUID
     * @return true if the player is a member, false otherwise
     */
    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    /**
     * Gets the number of members in the team.
     *
     * @return the team size
     */
    public int size() {
        return members.size();
    }

    @Override
    public String toString() {
        return "Team{" +
                "name='" + name + '\'' +
                ", leaderId=" + leaderId +
                ", size=" + members.size() +
                ", teamRP=" + teamRP +
                '}';
    }
}
