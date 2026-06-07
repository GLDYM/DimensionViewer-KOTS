package dev.stick_stack.dimensionviewer;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(DimensionViewer.MODID)
public class DimensionViewer {

    public static final String MODID = "dimensionviewer";

    public DimensionViewer(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.CONFIG);
    }

}
