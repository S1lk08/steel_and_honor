package steelandhonor.modid.kingdom;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * A claimed city, with a circular territory zone around it.
 */
public class City {

    private final UUID id;
    private final UUID nationId;
    private final String name;

    private final Identifier dimensionId;
    private final BlockPos center;
    private final int radius;

    private boolean isCapital;

    public City(UUID id, UUID nationId, String name,
                Identifier dimensionId, BlockPos center, int radius) {

        this.id = id;
        this.nationId = nationId;
        this.name = name;
        this.dimensionId = dimensionId;
        this.center = center.toImmutable();
        this.radius = radius;
        this.isCapital = false;
    }

    public UUID getId() { return id; }
    public UUID getNationId() { return nationId; }
    public String getName() { return name; }
    public Identifier getDimensionId() { return dimensionId; }
    public BlockPos getCenter() { return center; }
    public int getRadius() { return radius; }

    public boolean isCapital() { return isCapital; }
    public void setCapital(boolean capital) { isCapital = capital; }

    public boolean contains(BlockPos pos, Identifier dim) {
        if (!dimensionId.equals(dim)) return false;

        int dx = pos.getX() - center.getX();
        int dz = pos.getZ() - center.getZ();
        return dx * dx + dz * dz <= radius * radius;
    }
}
