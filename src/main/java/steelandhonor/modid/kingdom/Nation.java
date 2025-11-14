package steelandhonor.modid.kingdom;

import java.util.*;

public class Nation {

    private final UUID id;
    private String name;
    private int color;
    private UUID leaderId;

    // playerId -> Role
    private final Map<UUID, Enums.Role> memberRoles = new HashMap<>();

    // all cities belonging to this nation
    private final Set<UUID> cityIds = new HashSet<>();

    // capital city (optional)
    private UUID capitalCityId;

    public Nation(UUID id, String name, int color, UUID leaderId) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.leaderId = leaderId;
        memberRoles.put(leaderId, Enums.Role.LEADER);
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getColor() { return color; }

    public void setColor(int color) { this.color = color; }

    public UUID getLeaderId() { return leaderId; }

    public void setLeaderId(UUID leaderId) {
        this.leaderId = leaderId;
        memberRoles.put(leaderId, Enums.Role.LEADER);
    }

    public void addMember(UUID playerId, Enums.Role role) {
        memberRoles.put(playerId, role);
    }

    public boolean isMember(UUID playerId) {
        return memberRoles.containsKey(playerId);
    }

    public Enums.Role getRole(UUID playerId) {
        return memberRoles.getOrDefault(playerId, Enums.Role.CITIZEN);
    }

    public void setRole(UUID playerId, Enums.Role role) {
        memberRoles.put(playerId, role);
    }

    public Set<UUID> getMemberIds() {
        return Collections.unmodifiableSet(memberRoles.keySet());
    }

    public void addCity(UUID cityId) {
        cityIds.add(cityId);
    }

    public Set<UUID> getCityIds() {
        return cityIds;
    }

    public UUID getCapitalCityId() {
        return capitalCityId;
    }

    public void setCapitalCityId(UUID capitalCityId) {
        this.capitalCityId = capitalCityId;
    }
}
