package org.esotericist.mindshaft;


import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class Mindshaft {

    public Mindshaft(ModContainer mod, IEventBus modEventBus) {
        IEventBus forgeEventBus = NeoForge.EVENT_BUS;

        mod.registerConfig(ModConfig.Type.CLIENT, mindshaftConfig.CLIENT_SPEC);

        forgeEventBus.addListener((ClientTickEvent.Post e) -> { CommonClass.onEndTick(Minecraft.getInstance()); });
        forgeEventBus.addListener((InputEvent.Key e) -> inputHandler.onKeyInput(Minecraft.getInstance()));
        forgeEventBus.addListener((RenderGuiEvent.Post e) -> CommonClass.renderGameOverlay(e.getGuiGraphics(), e.getPartialTick()));

        modEventBus.addListener((FMLClientSetupEvent e) -> CommonClass.setup());
        modEventBus.addListener((ModConfigEvent e) -> CommonClass.onModConfigEvent(e.getConfig(), e.getConfig().getSpec()));
        modEventBus.addListener((RegisterKeyMappingsEvent e) -> CommonClass.initKeybindings(e::register));
    }
}
