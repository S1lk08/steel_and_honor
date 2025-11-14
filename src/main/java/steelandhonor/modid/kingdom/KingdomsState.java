package steelandhonor.modid.kingdom;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.*;

/**
 * Central server-side storage for nations, cities, wars and player membership.
 * Currently in-memory only (no disk persistence).
 */
public class KingdomsState {

    // 20 minutes * 60 seconds * 20 ticks = 24,000 ticks
    public static final int CAPTURE_TICKS = 20 * 60 * 20;

    // ---- singleton per server ----
    private static KingdomsState INSTANCE = null;

    public static KingdomsState get(MinecraftServer server) {
        if (INSTANCE == null) {
            INSTANCE = new KingdomsState();
        }
        return INSTANCE;
    }

    // ---- data ----
    private final Map<UUID, Nation> nations = new HashMap<>();
    private final Map<UUID, City> cities = new HashMap<>();
    private final Map<UUID, War> wars = new HashMap<>();

    // playerId -> nationId
    private final Map<UUID, UUID> playerNation = new HashMap<>();

    // nationId -> home location
    private final Map<UUID, NationHome> nationHomes = new HashMap<>();

    private static class NationHome {
        final Identifier dimension;
        final BlockPos pos;

        NationHome(Identifier dim, BlockPos pos) {
            this.dimension = dim;
            this.pos = pos.toImmutable();
        }
    }

    private KingdomsState() {}

    // -------- Nations --------

    public Nation createNation(ServerPlayerEntity creator, String name, int colorRgb) {
        UUID id = UUID.randomUUID();
        Nation nation = new Nation(id, name, colorRgb, creator.getUuid());
        nations.put(id, nation);
        playerNation.put(creator.getUuid(), id);
        return nation;
    }

    public Optional<Nation> getNation(UUID id) {
        return Optional.ofNullable(nations.get(id));
    }

    public Optional<Nation> getNationOfPlayer(UUID playerId) {
        UUID nid = playerNation.get(playerId);
        return nid == null ? Optional.empty() : Optional.ofNullable(nations.get(nid));
    }

    public void joinNation(ServerPlayerEntity player, Nation nation) {
        playerNation.put(player.getUuid(), nation.getId());
        nation.addMember(player.getUuid(), Enums.Role.CITIZEN);
    }

    public Nation findNationByName(String name) {
        for (Nation n : nations.values()) {
            if (n.getName().equalsIgnoreCase(name)) {
                return n;
            }
        }
        return null;
    }

    public void setPlayerRole(UUID playerId, Enums.Role role) {
        getNationOfPlayer(playerId).ifPresent(n -> n.setRole(playerId, role));
    }

    public Collection<Nation> getAllNations() {
        return nations.values();
    }

    // -------- Nation home --------

    public void setNationHome(Nation nation, Identifier dimId, BlockPos pos) {
        nationHomes.put(nation.getId(), new NationHome(dimId, pos));
    }

    public boolean teleportToNationHome(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        Optional<Nation> nationOpt = getNationOfPlayer(player.getUuid());
        if (nationOpt.isEmpty()) return false;

        Nation nation = nationOpt.get();
        NationHome home = nationHomes.get(nation.getId());
        if (home == null) return false;

        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, home.dimension);
        ServerWorld world = server.getWorld(worldKey);
        if (world == null) return false;

        BlockPos pos = home.pos;

        Vec3d position = new Vec3d(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5
        );

        TeleportTarget target = new TeleportTarget(
                world,
                position,
                Vec3d.ZERO,
                player.getYaw(),
                player.getPitch(),
                TeleportTarget.NO_OP
        );

        player.teleportTo(target);
        return true;
    }

    // -------- Cities --------

    public City createCity(Nation nation,
                           String name,
                           Identifier dimensionId,
                           BlockPos center,
                           int radius) {

        UUID id = UUID.randomUUID();
        City city = new City(id, nation.getId(), name, dimensionId, center, radius);
        cities.put(id, city);
        nation.addCity(id);
        return city;
    }

    public Optional<City> getCity(UUID id) {
        return Optional.ofNullable(cities.get(id));
    }

    public Optional<City> getCityAt(Identifier dimId, BlockPos pos) {
        for (City city : cities.values()) {
            if (city.contains(pos, dimId)) {
                return Optional.of(city);
            }
        }
        return Optional.empty();
    }

    public City findCityInNationByName(Nation nation, String name) {
        for (UUID cid : nation.getCityIds()) {
            City c = cities.get(cid);
            if (c != null && c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    public boolean setCapitalCityByName(Nation nation, String name) {
        City city = findCityInNationByName(nation, name);
        if (city == null) return false;

        // unset old capital
        UUID oldId = nation.getCapitalCityId();
        if (oldId != null && !oldId.equals(city.getId())) {
            City old = cities.get(oldId);
            if (old != null) old.setCapital(false);
        }

        city.setCapital(true);
        nation.setCapitalCityId(city.getId());
        return true;
    }

    public Collection<City> getAllCities() {
        return cities.values();
    }

    // -------- Wars --------

    public War startWar(Nation attacker, Nation defender, City city) {
        UUID id = UUID.randomUUID();
        War war = new War(id, attacker.getId(), defender.getId(), city.getId());
        wars.put(id, war);
        return war;
    }

    public Collection<War> getActiveWars() {
        List<War> result = new ArrayList<>();
        for (War war : wars.values()) {
            if (war.getStatus() == War.Status.ACTIVE) {
                result.add(war);
            }
        }
        return result;
    }

    /**
     * Called every server tick from WarTickHandler.
     * Handles siege progress / capture.
     */
    public void tickWars(MinecraftServer server) {
        for (War war : getActiveWars()) {
            City city = cities.get(war.getCityId());
            if (city == null) continue;

            Identifier cityDim = city.getDimensionId();
            BlockPos cityCenter = city.getCenter();
            int radius = city.getRadius();

            int attackersInCity = 0;
            int defendersInCity = 0;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                Identifier playerDim = player.getWorld().getRegistryKey().getValue();
                if (!playerDim.equals(cityDim)) continue;

                BlockPos pos = player.getBlockPos();
                int dx = pos.getX() - cityCenter.getX();
                int dz = pos.getZ() - cityCenter.getZ();
                if ((dx * dx + dz * dz) > radius * radius) continue;

                UUID pid = player.getUuid();
                Optional<Nation> nOpt = getNationOfPlayer(pid);
                if (nOpt.isEmpty()) continue;

                Nation n = nOpt.get();
                if (n.getId().equals(war.getAttackerNationId())) {
                    attackersInCity++;
                } else if (n.getId().equals(war.getDefenderNationId())) {
                    Enums.Role role = n.getRole(pid);
                    if (role == Enums.Role.MILITARY || role == Enums.Role.LEADER) {
                        defendersInCity++;
                    }
                }
            }

            if (attackersInCity > 0 && defendersInCity == 0) {
                war.addSiegeTicks(1);
                if (war.getSiegeTicks() >= CAPTURE_TICKS) {
                    Nation attacker = nations.get(war.getAttackerNationId());
                    Nation defender = nations.get(war.getDefenderNationId());
                    if (attacker != null && defender != null) {
                        defender.getCityIds().remove(city.getId());
                        attacker.addCity(city.getId());
                    }
                    war.setStatus(War.Status.CAPTURED);
                }
            }
            // defenders present -> siege paused, timer not reset
        }
    }
}
