package steelandhonor.modid.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import steelandhonor.modid.kingdom.*;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public final class ModCommands {

    private ModCommands() {}

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(ModCommands::registerAll);
    }

    private static void registerAll(CommandDispatcher<ServerCommandSource> dispatcher,
                                    CommandRegistryAccess registryAccess,
                                    CommandManager.RegistrationEnvironment env) {

        // /nation ...
        dispatcher.register(
                literal("nation")
                        .then(literal("create")
                                .then(argument("name", StringArgumentType.word())
                                        .then(argument("colorHex", StringArgumentType.word())
                                                .executes(ModCommands::nationCreate))))
                        .then(literal("join")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(ModCommands::nationJoin)))
                        .then(literal("sethome")
                                .executes(ModCommands::nationSetHome))
                        .then(literal("home")
                                .executes(ModCommands::nationHome))
                        .then(literal("setrole")
                                .then(argument("player", StringArgumentType.word())
                                        .then(argument("role", StringArgumentType.word())
                                                .executes(ModCommands::nationSetRole))))
        );

        // /city ...
        dispatcher.register(
                literal("city")
                        .then(literal("claim")
                                .then(argument("name", StringArgumentType.word())
                                        .then(argument("radius", IntegerArgumentType.integer(8, 256))
                                                .executes(ModCommands::cityClaim))))
                        .then(literal("setcapital")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(ModCommands::citySetCapital)))
        );

        // /war ...
        dispatcher.register(
                literal("war")
                        .then(literal("declare")
                                .requires(src -> src.hasPermissionLevel(4)) // admin
                                .then(argument("attacker", StringArgumentType.word())
                                        .then(argument("defender", StringArgumentType.word())
                                                .then(argument("city", StringArgumentType.word())
                                                        .executes(ModCommands::warDeclare)))))
        );
    }

    // ---------- /nation handlers ----------

    private static int nationCreate(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        String name = StringArgumentType.getString(ctx, "name");
        String colorHex = StringArgumentType.getString(ctx, "colorHex");

        int color;
        try {
            String hex = colorHex.startsWith("#") ? colorHex.substring(1) : colorHex;
            color = (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            source.sendError(Text.literal("Invalid color. Use hex like #FF0000."));
            return 0;
        }

        MinecraftServer server = source.getServer();
        KingdomsState state = KingdomsState.get(server);

        Nation nation = state.createNation(player, name, color);
        source.sendFeedback(() -> Text.literal("Created nation " + nation.getName() + " with color " + colorHex), true);
        return 1;
    }

    private static int nationJoin(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        String name = StringArgumentType.getString(ctx, "name");
        KingdomsState state = KingdomsState.get(source.getServer());

        Nation target = state.findNationByName(name);
        if (target == null) {
            source.sendError(Text.literal("No nation named " + name + " exists."));
            return 0;
        }

        state.joinNation(player, target);
        source.sendFeedback(() -> Text.literal("Joined nation " + target.getName()), true);
        return 1;
    }

    private static int nationSetHome(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        KingdomsState state = KingdomsState.get(source.getServer());
        var nationOpt = state.getNationOfPlayer(player.getUuid());
        if (nationOpt.isEmpty()) {
            source.sendError(Text.literal("You are not in a nation."));
            return 0;
        }

        Nation nation = nationOpt.get();
        Identifier dim = player.getWorld().getRegistryKey().getValue();
        BlockPos pos = player.getBlockPos();
        state.setNationHome(nation, dim, pos);

        source.sendFeedback(() -> Text.literal("Nation home set."), false);
        return 1;
    }

    private static int nationHome(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        KingdomsState state = KingdomsState.get(source.getServer());
        if (!state.teleportToNationHome(player)) {
            source.sendError(Text.literal("Your nation has no home set."));
            return 0;
        }
        return 1;
    }

    private static int nationSetRole(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity executor = source.getPlayer();
        if (executor == null) return 0;

        String playerName = StringArgumentType.getString(ctx, "player");
        String roleName = StringArgumentType.getString(ctx, "role");

        KingdomsState state = KingdomsState.get(source.getServer());
        var nationOpt = state.getNationOfPlayer(executor.getUuid());
        if (nationOpt.isEmpty()) {
            source.sendError(Text.literal("You are not in a nation."));
            return 0;
        }

        Nation nation = nationOpt.get();
        if (!nation.getLeaderId().equals(executor.getUuid())) {
            source.sendError(Text.literal("Only the nation leader can change roles."));
            return 0;
        }

        ServerPlayerEntity target = source.getServer().getPlayerManager().getPlayer(playerName);
        if (target == null) {
            source.sendError(Text.literal("Player not found: " + playerName));
            return 0;
        }

        Enums.Role role;
        try {
            role = Enums.Role.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendError(Text.literal("Unknown role. Use LEADER, MILITARY, CITIZEN, or SENATOR."));
            return 0;
        }

        state.setPlayerRole(target.getUuid(), role);
        source.sendFeedback(() -> Text.literal("Set role of " +
                target.getName().getString() + " to " + role), true);
        return 1;
    }

    // ---------- /city handlers ----------

    private static int cityClaim(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        String name = StringArgumentType.getString(ctx, "name");
        int radius = IntegerArgumentType.getInteger(ctx, "radius");

        KingdomsState state = KingdomsState.get(source.getServer());
        var nationOpt = state.getNationOfPlayer(player.getUuid());
        if (nationOpt.isEmpty()) {
            source.sendError(Text.literal("You are not in a nation."));
            return 0;
        }

        Nation nation = nationOpt.get();
        if (!nation.getLeaderId().equals(player.getUuid())) {
            source.sendError(Text.literal("Only the nation leader can claim cities."));
            return 0;
        }

        Identifier dim = player.getWorld().getRegistryKey().getValue();
        BlockPos pos = player.getBlockPos();
        City city = state.createCity(nation, name, dim, pos, radius);
        source.sendFeedback(() -> Text.literal("Claimed city " + city.getName() +
                " with radius " + radius), true);
        return 1;
    }

    private static int citySetCapital(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        String name = StringArgumentType.getString(ctx, "name");
        KingdomsState state = KingdomsState.get(source.getServer());

        var nationOpt = state.getNationOfPlayer(player.getUuid());
        if (nationOpt.isEmpty()) {
            source.sendError(Text.literal("You are not in a nation."));
            return 0;
        }

        Nation nation = nationOpt.get();
        if (!nation.getLeaderId().equals(player.getUuid())) {
            source.sendError(Text.literal("Only the nation leader can set the capital."));
            return 0;
        }

        if (!state.setCapitalCityByName(nation, name)) {
            source.sendError(Text.literal("No city named " + name + " in your nation."));
            return 0;
        }

        source.sendFeedback(() -> Text.literal("Capital city set to " + name), true);
        return 1;
    }

    // ---------- /war handlers ----------

    private static int warDeclare(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        String attackerName = StringArgumentType.getString(ctx, "attacker");
        String defenderName = StringArgumentType.getString(ctx, "defender");
        String cityName = StringArgumentType.getString(ctx, "city");

        KingdomsState state = KingdomsState.get(source.getServer());

        Nation attacker = state.findNationByName(attackerName);
        Nation defender = state.findNationByName(defenderName);
        if (attacker == null || defender == null) {
            source.sendError(Text.literal("Both attacker and defender nations must exist."));
            return 0;
        }

        City targetCity = state.findCityInNationByName(defender, cityName);
        if (targetCity == null) {
            source.sendError(Text.literal("Defender has no city named " + cityName));
            return 0;
        }

        War war = state.startWar(attacker, defender, targetCity);
        source.sendFeedback(() -> Text.literal("War declared: " +
                attacker.getName() + " vs " + defender.getName() +
                " over " + targetCity.getName()), true);
        return 1;
    }
}
