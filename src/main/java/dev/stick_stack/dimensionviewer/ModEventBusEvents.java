package dev.stick_stack.dimensionviewer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void onConfigReloaded(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().contains(Constants.MOD_ID)) {
            Constants.LOG.info("Config file reloaded!");

            if (!PlayerListHandler.playerList.isEmpty()) {
                MinecraftServer server = PlayerListHandler.playerList.get(0).getServer();

                // Refresh display name first as tab list name uses it if `DIM_IN_CHAT_NAME` is true
                server.getPlayerList().getPlayers().forEach(ServerPlayer::refreshDisplayName);
                server.getPlayerList().getPlayers().forEach(ServerPlayer::refreshTabListName);
            } else {
                Constants.LOG.info("Skipping player refresh as there are no players...");
            }
        }
    }
}
