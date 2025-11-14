package steelandhonor.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import steelandhonor.modid.kingdom.WarTickHandler;
import steelandhonor.modid.registry.ModCommands;
import steelandhonor.modid.registry.ModItems;

public class Steel_and_honor implements ModInitializer {
    public static final String MOD_ID = "steel_and_honor";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Steel_and_honor] Initializing kingdom systems and items…");

        // Register custom armor & weapons
        ModItems.initialize();

        // Register /nation, /city, /war, etc.
        ModCommands.registerCommands();

        // Per-tick war / capture logic
        ServerTickEvents.END_SERVER_TICK.register(WarTickHandler::onEndTick);

        LOGGER.info("[Steel_and_honor] Initialization complete.");
    }
}
