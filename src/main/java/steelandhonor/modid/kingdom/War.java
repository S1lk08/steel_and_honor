package steelandhonor.modid.kingdom;

import java.util.UUID;

public class War {

    public enum Status {
        ACTIVE,
        CAPTURED,
        CANCELLED
    }

    private final UUID id;
    private final UUID attackerNationId;
    private final UUID defenderNationId;
    private final UUID cityId;

    private Status status = Status.ACTIVE;
    private int siegeTicks = 0;

    public War(UUID id, UUID attackerNationId, UUID defenderNationId, UUID cityId) {
        this.id = id;
        this.attackerNationId = attackerNationId;
        this.defenderNationId = defenderNationId;
        this.cityId = cityId;
    }

    public UUID getId() { return id; }
    public UUID getAttackerNationId() { return attackerNationId; }
    public UUID getDefenderNationId() { return defenderNationId; }
    public UUID getCityId() { return cityId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getSiegeTicks() { return siegeTicks; }
    public void addSiegeTicks(int ticks) { this.siegeTicks += ticks; }
}
