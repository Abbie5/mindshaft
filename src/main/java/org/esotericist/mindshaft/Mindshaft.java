package org.esotericist.mindshaft;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.api.fml.event.config.ModConfigEvents;

public class Mindshaft implements ClientModInitializer, ModConfigEvents.Loading, ClientTickEvents.EndTick, HudRenderCallback {
    public static final String MODID = "mindshaft";
    public static final String NAME = "Mindshaft";

    public static final Logger logger = LogManager.getLogger();

    private static Player player;

    private static mindshaftRenderer renderer = new mindshaftRenderer();

    private static inputHandler input;

    public static zoomState zoom = new zoomState();

    private static mindshaftScanner scanner = new mindshaftScanner();

    @Override
    public void onInitializeClient() {
        ModLoadingContext.registerConfig(MODID, ModConfig.Type.CLIENT, mindshaftConfig.CLIENT_SPEC);
        logger.info("setup");
        bakeandzoom();
        input = new inputHandler();
        Minecraft.getInstance().tell(new assetinit());

        ModConfigEvents.loading(MODID).register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        HudRenderCallback.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(input);
    }

    public static class assetinit implements Runnable {
        public void run() {
            renderer.initAssets();
        }
    }

    static void bakeandzoom() {
        mindshaftConfig.bakeConfig();
        zoom.initzooms();
    }

    @Override
    public void onModConfigLoading(ModConfig config) {
        if (config != null && config.getSpec() == mindshaftConfig.CLIENT_SPEC) {
            mindshaftConfig.dirtyconfig = true;
        }
    }

    @Override
    public void onEndTick(Minecraft mc) {

        if (mindshaftConfig.dirtyconfig) {
            bakeandzoom();
            mindshaftConfig.dirtyconfig = false;
        }

            player = mc.player;
            Level world = mc.level;
            if (player == null) {
                return;
            }

            if (mindshaftConfig.enabled || zoom.fullscreen) {

                // this adjustment allows the player to be considered at the 'same' Y value
                // whether on a normal block, on farmland (so slightly below normal), or
                // on a slab (half a block above normal)
                int pY = (int) (Math.ceil(player.getY() - (17 / 32D)));
                BlockPos pPos = new BlockPos(player.getX(), pY, player.getZ());
                double rawV = player.getDeltaMovement().y;
                int vY = (int) (rawV > 0 ? Math.ceil(rawV) : Math.floor(rawV));

                scanner.setWorld(player.getCommandSenderWorld());
                scanner.setNow(world.getGameTime());
                scanner.setDim(world.dimension().hashCode());
                scanner.processChunks(pY, vY);
                scanner.rasterizeLayers(pPos, vY, renderer, zoom);
        }
    }

    @Override
    public void onHudRender(PoseStack poseStack, float partialTick) {
        renderer.doRender(poseStack, player, zoom);
    }
}
