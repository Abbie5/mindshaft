package org.esotericist.mindshaft;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.config.ModConfig;

public class Mindshaft implements ClientModInitializer, ModConfigEvents.Loading, ClientTickEvents.EndTick, HudRenderCallback {
    @Override
    public void onInitializeClient() {
        ForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.CLIENT, mindshaftConfig.CLIENT_SPEC);

        CommonClass.setup();
        CommonClass.initKeybindings(KeyBindingHelper::registerKeyBinding);

        ModConfigEvents.loading(Constants.MOD_ID).register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        HudRenderCallback.EVENT.register(this);
    }

    @Override
    public void onEndTick(Minecraft client) {
        inputHandler.onKeyInput();
        CommonClass.onEndTick();
    }

    @Override
    public void onHudRender(PoseStack matrixStack, float tickDelta) {
        CommonClass.renderGameOverlay(matrixStack);
    }

    @Override
    public void onModConfigLoading(ModConfig config) {
        CommonClass.onModConfigEvent(config);
    }
}
