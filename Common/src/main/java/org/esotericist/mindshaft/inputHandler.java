package org.esotericist.mindshaft;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

class inputHandler {

    public static KeyMapping[] keyBindings = {
            new KeyMapping("mindshaft.key.toggle.desc",     InputConstants.KEY_NUMPAD1, "mindshaft.key.category"),
            new KeyMapping("mindshaft.key.fullscreen.desc", InputConstants.KEY_NUMPAD0, "mindshaft.key.category"),
            new KeyMapping("mindshaft.key.zoomin.desc",     InputConstants.KEY_NUMPAD6, "mindshaft.key.category"),
            new KeyMapping("mindshaft.key.zoomout.desc",    InputConstants.KEY_NUMPAD3, "mindshaft.key.category")
    };

    public static void onKeyInput() {

        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive()) {
            return;
        }

        zoomState zoom = CommonClass.zoom;

        // binding 0: enable/disable toggle
        while (keyBindings[0].consumeClick()) mindshaftConfig.setEnabled(!mindshaftConfig.enabled);

        // binding 1: fullscreen toggle
        // this doesn't have a config entry because it isn't meant to be persistent
        // across sessions.
        while (keyBindings[1].consumeClick()) zoom.fullscreen = !zoom.fullscreen;

        // binding 2: zoom in
        while (keyBindings[2].consumeClick()) zoom.nextZoom();

        // binding 3: zoom out
        while (keyBindings[3].consumeClick()) zoom.prevZoom();

    }

}