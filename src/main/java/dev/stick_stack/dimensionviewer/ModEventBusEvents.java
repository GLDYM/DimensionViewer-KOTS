package dev.stick_stack.dimensionviewer;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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