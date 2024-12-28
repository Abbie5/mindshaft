package org.esotericist.mindshaft;


import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.TickEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraftforge.client.event.RenderGuiOverlayEvent;

import net.minecraftforge.fml.event.config.ModConfigEvent;


@Mod(Constants.MOD_ID)
public class Mindshaft {

    public Mindshaft() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, mindshaftConfig.CLIENT_SPEC);

        forgeEventBus.addListener((TickEvent.ClientTickEvent e) -> { if (e.phase == TickEvent.Phase.END) CommonClass.onEndTick(Minecraft.getInstance()); });
        forgeEventBus.addListener((InputEvent.Key e) -> inputHandler.onKeyInput(Minecraft.getInstance()));
        forgeEventBus.addListener((RenderGuiOverlayEvent.Post e) -> CommonClass.renderGameOverlay(e.getGuiGraphics(), e.getPartialTick()));

        modEventBus.addListener((FMLClientSetupEvent e) -> CommonClass.setup());
        modEventBus.addListener((ModConfigEvent e) -> CommonClass.onModConfigEvent(e.getConfig()));
        modEventBus.addListener((RegisterKeyMappingsEvent e) -> CommonClass.initKeybindings(e::register));
    }
}
