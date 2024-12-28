package org.esotericist.mindshaft.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

import org.esotericist.mindshaft.platform.services.IPlatformHelper;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }
}