package org.esotericist.mindshaft;


import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraftforge.client.event.RenderGuiOverlayEvent;

import net.minecraftforge.fml.event.config.ModConfigEvent;


@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Mindshaft {

    public Mindshaft() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerBindings);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, mindshaftConfig.CLIENT_SPEC);
    }

    public void setup(FMLClientSetupEvent event) {
        CommonClass.setup();

        MinecraftForge.EVENT_BUS.register(this);

    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent configEvent) {
        CommonClass.onModConfigEvent(configEvent.getConfig());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) CommonClass.onEndTick();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void RenderGameOverlayEvent(RenderGuiOverlayEvent.Post event) {
        CommonClass.renderGameOverlay(event.getPoseStack());
    }

    @SubscribeEvent
    public void registerBindings(RegisterKeyMappingsEvent event) {
        CommonClass.initKeybindings(event::register);
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.Key event) {
        inputHandler.onKeyInput();
    }
}
