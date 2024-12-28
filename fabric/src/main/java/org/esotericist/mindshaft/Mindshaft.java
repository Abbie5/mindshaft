package org.esotericist.mindshaft;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraftforge.fml.config.ModConfig;

public class Mindshaft implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.CLIENT, mindshaftConfig.CLIENT_SPEC);

        CommonClass.setup();
        CommonClass.initKeybindings(KeyBindingHelper::registerKeyBinding);

        ModConfigEvents.loading(Constants.MOD_ID).register(CommonClass::onModConfigEvent);
        ClientTickEvents.END_CLIENT_TICK.register(CommonClass::onEndTick);
        ClientTickEvents.END_CLIENT_TICK.register(inputHandler::onKeyInput);
        HudRenderCallback.EVENT.register(CommonClass::renderGameOverlay);
    }
}
