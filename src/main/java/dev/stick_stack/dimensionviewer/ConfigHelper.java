package dev.stick_stack.dimensionviewer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

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

    @SuppressWarnings("unchecked")
    public static List<String> GetAllCustomColors() {
        return (List<String>) Config.CUSTOM_COLORS.get();
    }

    public static void AddCustomColor(String name, String color) {
        @SuppressWarnings("unchecked")
        List<String> colors = (List<String>) Config.CUSTOM_COLORS.get();

        colors.add("%s %s".formatted(name, color));
        Config.CUSTOM_COLORS.set(colors);
    }

    public static boolean RemoveCustomColor(String name) {
        @SuppressWarnings("unchecked")
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
        @SuppressWarnings("unchecked")
        String entry = findMatchingEntry((List<String>) Config.DIM_ALIASES.get(), dimId);
        return entry == null ? null : entry.split(" ", 2)[1];
    }

    public static @Nullable String GetCustomColor(String dimId) {
        return switch (dimId) {
            case "minecraft:overworld" -> OverworldColor();
            case "minecraft:the_nether" -> NetherColor();
            case "minecraft:the_end" -> EndColor();
            default -> {
                @SuppressWarnings("unchecked")
                String entry = findMatchingEntry((List<String>) Config.MODDED_DIMS.get(), dimId);
                yield entry == null ? null : entry.split(" ", 2)[1];
            }
        };
    }

    public static void SetAlias(String dimId, String alias) {
        @SuppressWarnings("unchecked")
        List<String> aliases = (List<String>) Config.DIM_ALIASES.get();
        String updatedValue = "%s %s".formatted(dimId, alias);

        upsertPatternEntry(aliases, dimId, updatedValue);
        Config.DIM_ALIASES.set(aliases);
    }

    public static void SetColor(String dimId, String color) {
        @SuppressWarnings("unchecked")
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
                upsertPatternEntry(dims, dimId, "%s %s".formatted(dimId, color));
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
        @SuppressWarnings("unchecked")
        List<String> aliases = (List<String>) Config.DIM_ALIASES.get();

        removePatternEntry(aliases, dimId);
        Config.DIM_ALIASES.set(aliases);
    }

    public static void ResetColor(String dimId) {
        switch (dimId) {
            case "minecraft:overworld" -> Config.OVERWORLD_COLOR.set(Config.DEFAULT_OVERWORLD_COLOR);
            case "minecraft:the_nether" -> Config.NETHER_COLOR.set(Config.DEFAULT_NETHER_COLOR);
            case "minecraft:the_end" -> Config.END_COLOR.set(Config.DEFAULT_END_COLOR);
            default -> {
                @SuppressWarnings("unchecked")
                List<String> dims = (List<String>) Config.MODDED_DIMS.get();
                removePatternEntry(dims, dimId);

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

    @SuppressWarnings("unchecked")
    public static boolean HasAlias(String dimId) {
        return findMatchingEntry((List<String>) Config.DIM_ALIASES.get(), dimId) != null;
    }

    private static @Nullable String findMatchingEntry(List<String> entries, String dimId) {
        for (String entry : entries) {
            String[] values = entry.split(" ", 2);
            Pattern pattern = Pattern.compile(values[0]);

            if (pattern.matcher(dimId).find()) {
                return entry;
            }
        }

        return null;
    }

    private static void upsertPatternEntry(List<String> entries, String pattern, String updatedValue) {
        int index = findExactPatternIndex(entries, pattern);
        if (index >= 0) {
            entries.set(index, updatedValue);
        } else {
            entries.add(updatedValue);
        }
    }

    private static void removePatternEntry(List<String> entries, String pattern) {
        int index = findExactPatternIndex(entries, pattern);
        if (index >= 0) {
            entries.remove(index);
        }
    }

    private static int findExactPatternIndex(List<String> entries, String pattern) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).split(" ", 2)[0].equals(pattern)) {
                return i;
            }
        }

        return -1;
    }
}
