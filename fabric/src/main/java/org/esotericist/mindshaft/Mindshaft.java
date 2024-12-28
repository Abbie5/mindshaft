package org.esotericist.mindshaft;

import net.neoforged.fml.config.ModConfig;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class Mindshaft implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NeoForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.CLIENT, mindshaftConfig.CLIENT_SPEC);

        CommonClass.setup();
        CommonClass.initKeybindings(KeyBindingHelper::registerKeyBinding);

        NeoForgeModConfigEvents.loading(Constants.MOD_ID).register(modConfig -> CommonClass.onModConfigEvent(modConfig, modConfig.getSpec()));
        ClientTickEvents.END_CLIENT_TICK.register(CommonClass::onEndTick);
        ClientTickEvents.END_CLIENT_TICK.register(inputHandler::onKeyInput);
        HudRenderCallback.EVENT.register(CommonClass::renderGameOverlay);
    }
}
