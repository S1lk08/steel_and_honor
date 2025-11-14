package steelandhonor.modid.kingdom;

import net.minecraft.server.MinecraftServer;

public class WarTickHandler {

    // Called by ServerTickEvents.END_SERVER_TICK in your main mod class
    public static void onEndTick(MinecraftServer server) {
        KingdomsState.get(server).tickWars(server);
    }
}
