package dev.stick_stack.dimensionviewer;

import dev.stick_stack.dimensionviewer.CommonUtils;
import dev.stick_stack.dimensionviewer.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfigHelper{

    public static String BaseDefaultColor() {
        return Config.DEFAULT_DEFAULT_COLOR;
    }

    public static String BaseOverworldColor() {
        return Config.DEFAULT_OVERWORLD_COLOR;
    }

    public static String BaseNetherColor() {
        return Config.DEFAULT_NETHER_COLOR;
    }

    public static String BaseEndColor() {
        return Config.DEFAULT_END_COLOR;
    }

    public static String BaseListFormat() {
        return Config.DEFAULT_LIST_FORMAT;
    }


    public static String DefaultColor() {
        return Config.DEFAULT_COLOR.get();
    }

    public static String OverworldColor() {
        return Config.OVERWORLD_COLOR.get();
    }

    public static String NetherColor() {
        return Config.NETHER_COLOR.get();
    }

    public static String EndColor() {
        return Config.END_COLOR.get();
    }

    public static String ListFormat() {
        return Config.LIST_FORMAT.get();
    }

    public static List<String> GetAllCustomColors() {
        return (List<String>) Config.CUSTOM_COLORS.get();
    }

    public static void AddCustomColor(String name, String color) {
        List<String> colors = (List<String>) Config.CUSTOM_COLORS.get();

        colors.add("%s %s".formatted(name, color));
        Config.CUSTOM_COLORS.set(colors);
    }

    public static boolean RemoveCustomColor(String name) {
        List<String> colors = (List<String>) Config.CUSTOM_COLORS.get();

        int i = 0;
        for (String c : colors) {
            if (c.split(" ")[0].equals(name)) {
                colors.remove(i);
                Config.CUSTOM_COLORS.set(colors);
                return true;
            }
            i++;
        }
        return false;
    }

    public static @Nullable String GetAlias(String dimId) {
        for (var dim : Config.DIM_ALIASES.get()) {
            var values = dim.split(" ", 2);

            if (dimId.equals(values[0])) {
                return values[1];
            }
        }

        return null;
    }

    public static @Nullable String GetCustomColor(String dimId) {
        return switch (dimId) {
            case "minecraft:overworld" -> OverworldColor();
            case "minecraft:the_nether" -> NetherColor();
            case "minecraft:the_end" -> EndColor();
            default -> {
                for (var dim : Config.MODDED_DIMS.get()) {
                    var values = dim.split(" ", 2);

                    if (dimId.equals(values[0])) {
                        yield values[1];
                    }
                }

                yield null;
            }
        };
    }

    public static void SetAlias(String dimId, String alias) {
        List<String> aliases = (List<String>) Config.DIM_ALIASES.get();

        int i = 0;
        for (String dim : aliases) {
            var values = dim.split(" ", 2);

            if (dimId.equals(values[0])) {
                aliases.set(i, "%s %s".formatted(dimId, alias));
                Config.DIM_ALIASES.set(aliases);
                return;
            }

            i++;
        }

        aliases.add(i, "%s %s".formatted(dimId, alias));
        Config.DIM_ALIASES.set(aliases);
    }

    public static void SetColor(String dimId, String color) {
        List<String> dims = (List<String>) Config.MODDED_DIMS.get();

        switch (dimId) {
            case "minecraft:overworld" -> {
                Config.OVERWORLD_COLOR.set(color);
            }
            case "minecraft:the_nether" -> {
                Config.NETHER_COLOR.set(color);
            }
            case "minecraft:the_end" -> {
                Config.END_COLOR.set(color);
            }
            default -> {
                int i = 0;
                for (String dim : Config.MODDED_DIMS.get()) {
                    var values = dim.split(" ", 2);

                    if (dimId.equals(values[0])) {
                        dims.set(i, "%s %s".formatted(dimId, color));
                        Config.MODDED_DIMS.set(dims);
                        return;
                    }

                    i++;
                }

                dims.add("%s %s".formatted(dimId, color));
                Config.MODDED_DIMS.set(dims);
            }
        }
    }

    public static void SetBoolSetting(String setting, boolean value) {
        switch (setting) {
            case "perDimColor" -> Config.PER_DIM_COLOR.set(value);
            case "dimInChatName" -> Config.DIM_IN_CHAT_NAME.set(value);
            case "chatDimHover" -> Config.CHAT_DIM_HOVER.set(value);
            case "enableAliases" -> Config.ENABLE_ALIASES.set(value);
        }
    }

    public static void ResetAlias(String dimId) {
        List<String> aliases = (List<String>) Config.DIM_ALIASES.get();

        aliases.removeIf(a -> dimId.equals(a.split(" ", 2)[0]));
        Config.DIM_ALIASES.set(aliases);
    }

    public static void ResetColor(String dimId) {
        switch (dimId) {
            case "minecraft:overworld" -> Config.OVERWORLD_COLOR.set(Config.DEFAULT_OVERWORLD_COLOR);
            case "minecraft:the_nether" -> Config.NETHER_COLOR.set(Config.DEFAULT_NETHER_COLOR);
            case "minecraft:the_end" -> Config.END_COLOR.set(Config.DEFAULT_END_COLOR);
            default -> {
                List<String> dims = (List<String>) Config.MODDED_DIMS.get();
                dims.removeIf(a -> dimId.equals(a.split(" ", 2)[0]));

                Config.MODDED_DIMS.set(dims);
            }
        }
    }

    public static void SetFormat(String format) {
        Config.LIST_FORMAT.set(format);
    }

    public static void SetPlacement(CommonUtils.DimensionPosition position) {
        Config.DIM_POSITION.set(position);
    }

    public static void RefreshPlayerData(PlayerList players) {
        players.getPlayers().forEach(ServerPlayer::refreshDisplayName);
        players.getPlayers().forEach(ServerPlayer::refreshTabListName);
    }

    public static boolean HasAlias(String dimId) {
        for (var dim : Config.DIM_ALIASES.get()) {
            if (dim.split(" ", 2)[0].equals(dimId)) {
                return true;
            }
        }

        return false;
    }
}