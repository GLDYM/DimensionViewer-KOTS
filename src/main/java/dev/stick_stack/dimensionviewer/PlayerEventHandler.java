package dev.stick_stack.dimensionviewer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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

import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PlayerEventHandler {

    private static void refreshPlayerDetails(PlayerEvent event) {
        List<ServerPlayer> players = event.getEntity().getServer().getPlayerList().getPlayers();

        players.forEach(ServerPlayer::refreshDisplayName);
        players.forEach(ServerPlayer::refreshTabListName);
    }

    private static Style tryGetColor(String color) {
        try {
            ChatFormatting format = ChatFormatting.valueOf(color);
            return Style.EMPTY.withColor(format);
        } catch (IllegalArgumentException exception) {
            for (String entry : Config.CUSTOM_COLORS.get()) {
                String[] splits = entry.split(" ");

                if (color.equals(splits[0])) {
                    if (splits[1].startsWith("#")) {
                        return Style.EMPTY.withColor(CommonUtils.hexToInt(splits[1]));
                    } else {
                        int r = Integer.parseInt(splits[1].substring(1));
                        int g = Integer.parseInt(splits[2].substring(1));
                        int b = Integer.parseInt(splits[3].substring(1));

                        return Style.EMPTY.withColor(CommonUtils.rgbToInt(r, g, b));
                    }
                }
            }
        }

        return Style.EMPTY;
    }

    private static Component createDimensionComponent(PlayerEvent event, MutableComponent originalName) {
        ResourceLocation dimension = event.getEntity().level().dimension().location();
        String dimSource = CommonUtils.toTitleCase(CommonUtils.splitResourceLocation(dimension, 0));
        final PlayerListHandler handler = new PlayerListHandler();

        Style style = Style.EMPTY;
        boolean foundModdedDim = false;
        if (Config.PER_DIM_COLOR.get()) {
            for (String modDim : Config.MODDED_DIMS.get()) {
                if (modDim.split(" ")[0].equals(dimension.toString())) {
                    style = tryGetColor(modDim.split(" ")[1]);
                    foundModdedDim = true;
                    break;
                }
            }

            if (!foundModdedDim) {
                style = switch (dimension.toString()) {
                    case "minecraft:overworld" -> tryGetColor(Config.OVERWORLD_COLOR.get());
                    case "minecraft:the_nether" -> tryGetColor(Config.NETHER_COLOR.get());
                    case "minecraft:the_end" -> tryGetColor(Config.END_COLOR.get());
                    default -> tryGetColor(Config.DEFAULT_COLOR.get());
                };
            }
        } else {
            style = tryGetColor(Config.DEFAULT_COLOR.get());
        }

        MutableComponent dimComponent = handler.makeDimensionComponent(event.getEntity(), Config.LIST_FORMAT.get())
                .withStyle(style);

        if (Config.CHAT_DIM_HOVER.get()) {
            dimComponent.withStyle(
                dimComponent.getStyle().withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(dimSource))
                )
            );
        }

        MutableComponent spacer = Component.literal(" ");
        if (Config.DIM_POSITION.get() == CommonUtils.DimensionPosition.PREPEND) {
            spacer.setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)).append(originalName);
            return dimComponent.append(spacer);
        } else {
            spacer.append(dimComponent);
            return originalName.append(spacer);
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CustomCommands.RegisterCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerListHandler.playerList.add(event.getEntity());
        refreshPlayerDetails(event);
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerListHandler.playerList.remove(event.getEntity());
        refreshPlayerDetails(event);
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        refreshPlayerDetails(event);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        refreshPlayerDetails(event);
    }

    @SubscribeEvent
    public static void changeDisplayName(PlayerEvent.NameFormat event) {
        if (!Config.DIM_IN_CHAT_NAME.get()) return;

        event.setDisplayname(createDimensionComponent(event, event.getDisplayname().copy()));

    }

    @SubscribeEvent
    public static void changeTabListName(PlayerEvent.TabListNameFormat event) {
        if (Config.DIM_IN_CHAT_NAME.get()) {
            event.setDisplayName(event.getEntity().getDisplayName());
        } else {
            MutableComponent originalName = event.getEntity().getDisplayName().copy();
            event.setDisplayName(createDimensionComponent(event, originalName));
        }
    }
}