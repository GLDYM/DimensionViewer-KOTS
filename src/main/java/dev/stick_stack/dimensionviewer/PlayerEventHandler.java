package dev.stick_stack.dimensionviewer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.DEDICATED_SERVER)
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

    private static Style getDimensionStyle(ResourceLocation dimension) {
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

        return style;
    }

    private static MutableComponent createDimensionLabel(ServerPlayer player, Component translatedDimension) {
        ResourceLocation dimension = player.level().dimension().location();
        String dimSource = CommonUtils.toTitleCase(CommonUtils.splitResourceLocation(dimension, 0));
        final PlayerListHandler handler = new PlayerListHandler();
        MutableComponent dimComponent = handler.makeDimensionComponent(
                Config.LIST_FORMAT.get(),
                dimension,
                translatedDimension
        )
                .withStyle(getDimensionStyle(dimension));

        if (Config.CHAT_DIM_HOVER.get()) {
            dimComponent.withStyle(
                dimComponent.getStyle().withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(dimSource))
                )
            );
        }

        return dimComponent;
    }

    private static Component createDimensionComponent(ServerPlayer player, MutableComponent originalName) {
        MutableComponent dimComponent = createDimensionLabel(player, player.level().getDescription());
        MutableComponent spacer = MutableComponent.create(new PlainTextContents.LiteralContents(" "));
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
    public static void onServerChat(ServerChatEvent event) {
        if (!Config.DIM_IN_CHAT_NAME.get()) return;

        ResourceLocation dimension = event.getPlayer().level().dimension().location();
        Component fallbackDimensionName = Component.literal(CommonUtils.dimensionToString(dimension));
        MutableComponent dimensionComponent = createDimensionLabel(event.getPlayer(), fallbackDimensionName);
        event.setMessage(Component.empty()
                .append(dimensionComponent)
                .append(Component.literal(" "))
                .append(Component.literal(event.getRawText())));
    }

    @SubscribeEvent
    public static void changeTabListName(PlayerEvent.TabListNameFormat event) {
        if (!Config.DIM_IN_CHAT_NAME.get()) {
            event.setDisplayName(event.getEntity().getDisplayName());
            return;
        }

        MutableComponent originalName = event.getEntity().getDisplayName().copy();
        event.setDisplayName(createDimensionComponent((ServerPlayer) event.getEntity(), originalName));
    }
}
