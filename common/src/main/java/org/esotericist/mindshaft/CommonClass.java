package org.esotericist.mindshaft;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Consumer;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

    public static Player player;

    public static mindshaftRenderer renderer = new mindshaftRenderer();

    public static inputHandler input;

    public static zoomState zoom = new zoomState();

    public static mindshaftScanner scanner = new mindshaftScanner();


    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void setup() {
        Constants.LOG.info("setup");
        bakeandzoom();
        Minecraft.getInstance().tell(new assetinit());
    }

    static void bakeandzoom() {
        mindshaftConfig.bakeConfig();
        zoom.initzooms();
    }

    public static void onEndTick(Minecraft mc) {
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
            BlockPos pPos = new BlockPos(player.getBlockX(), pY, player.getBlockZ());
            double rawV = player.getDeltaMovement().y;
            int vY = (int) (rawV > 0 ? Math.ceil(rawV) : Math.floor(rawV));

            scanner.setWorld(player.getCommandSenderWorld());
            scanner.setNow(world.getGameTime());
            scanner.setDim(world.dimension().hashCode());
            scanner.processChunks(pY, vY);
            scanner.rasterizeLayers(pPos, vY, renderer, zoom);
        }
    }

    public static void initKeybindings(Consumer<KeyMapping> consumer) {
        for (KeyMapping keyMapping : inputHandler.keyBindings) consumer.accept(keyMapping);
    }

    public static class assetinit implements Runnable {
        public void run() {
            renderer.initAssets();
        }
    }

    public static void onModConfigEvent(Object config, IConfigSpec spec) {
        if (config != null && spec == mindshaftConfig.CLIENT_SPEC) {
            mindshaftConfig.dirtyconfig = true;
        }
    }

    public static void renderGameOverlay(GuiGraphics gui, DeltaTracker deltaTracker) {
        renderer.doRender(gui, player, zoom);
    }

}