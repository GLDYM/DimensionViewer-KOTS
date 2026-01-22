package dev.stick_stack.dimensionviewer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PlayerListHandler {

    public static final List<Player> playerList = new ArrayList<>();

    public MutableComponent makeDimensionComponent(Player player, String format) {
        ResourceLocation dimension = player.level().dimension().location();

        return extractTokensFromFormat(format, dimension);
    }

    private MutableComponent extractTokensFromFormat(String format, ResourceLocation dimension) {
        // Check the list format for tokens, then remove any tokens from the string
        Style style = checkTokens(null, format);
        format = replaceTokens(format);

        // Replace tokens in the aliased dimension name
        String aliasedDim = checkForAliases(dimension.toString());
        style = checkTokens(style, aliasedDim);
        aliasedDim = replaceTokens(aliasedDim);

        // The alias can use the `%d` token, allowing the original name to be used
        aliasedDim = aliasedDim.replace("%d", CommonUtils.toTitleCase(
                CommonUtils.splitResourceLocation(dimension, 1)
        ));

        // Try to translate the dimension name
        Component aliasedDimComp = Component.translatable(aliasedDim);

        // Finally, replace the dimension placeholder with the actual dimension name.
        MutableComponent result = Component.literal("");
        List<String> parts = List.of(format.split("%d", -1));
        for (int i = 0; i < parts.size(); i++) {
            result.append(Component.literal(parts.get(i)));
            if (i < parts.size() - 1) {
                result.append(aliasedDimComp.copy());
            }
        }

        return result.withStyle(style);
    }


    /**
     * Checks a String for any valid styling tokens.
     * @param inStyle null for a new Style or an existing Style to append to.
     * @param inString The String to check for tokens in
     * @return A Style with the appropriate settings applied
     */
    private Style checkTokens(@Nullable Style inStyle, String inString) {
        inStyle = inStyle == null ? Style.EMPTY : inStyle;

        boolean useItalic = inString.contains("%i");
        boolean useBold = inString.contains("%b");
        boolean useUnderline = inString.contains("%u");
        boolean useStrikethrough = inString.contains("%s");
        boolean useObfuscate = inString.contains("%o");

        inStyle = inStyle
                .withItalic(inStyle.isItalic() || useItalic)
                .withBold(inStyle.isBold() || useBold)
                .withUnderlined(inStyle.isUnderlined() || useUnderline)
                .withStrikethrough(inStyle.isStrikethrough() || useStrikethrough)
                .withObfuscated(inStyle.isObfuscated() || useObfuscate);

        return inStyle;
    }

    /**
     * Replace all valid styling tokens in a string
     * @param inString A String with tokens to be removed
     * @return inString with all tokens removed
     */
    private String replaceTokens(String inString) {
        // Remove all tokens (anything proceeded with a %, but ignore the `%d` token)
        for (String token : new String[] {"%i", "%b", "%u", "%s", "%o"}) {
            inString = inString.replace(token, "");
        }

        // Remove any remaining invalid tokens
        inString = inString.replaceAll("%[^%d].*?", "");

        // Replace the escaped token
        inString = inString.replace("%%", "%");

        return inString;
    }

    public String checkForAliases(String dimensionResourceLocation) {
        if (Config.ENABLE_ALIASES.get()) {
            for (String alias : Config.DIM_ALIASES.get()) {
                Pattern AliasAsPattern = Pattern.compile(alias.split(" ")[0]);

                if (AliasAsPattern.matcher(dimensionResourceLocation).find()) {
                    return alias.split(" ", 2)[1];
                }
            }
        }

        Pattern userIdPattern = Pattern.compile("[-_]?\\w{8}([-_]\\w{4}){3}[-_]\\w{12}/?");
        return CommonUtils.dimensionToString(userIdPattern.matcher(dimensionResourceLocation).replaceAll(""));
    }

}