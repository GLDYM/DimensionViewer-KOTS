package dev.stick_stack.dimensionviewer;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

import java.util.logging.Logger;

@Mod(DimensionViewer.MODID)
public class DimensionViewer {

    public static final String MODID = "dimensionviewer";
    public static final Logger LOGGER = Logger.getLogger(DimensionViewer.MODID);

    public DimensionViewer(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () ->
                new IExtensionPoint.DisplayTest(() ->
                        NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        context.registerConfig(ModConfig.Type.SERVER, Config.CONFIG);
    }

}
