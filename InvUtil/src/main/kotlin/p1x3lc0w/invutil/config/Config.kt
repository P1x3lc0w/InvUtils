package p1x3lc0w.invutil.config

import net.fabricmc.loader.api.FabricLoader

class Config {
    companion object {
        fun getConfig(): IInvUtilConfig {
            val fabricLoader: FabricLoader = FabricLoader.getInstance()
            if (
                fabricLoader.isModLoaded("cloth-config") &&
                fabricLoader.isModLoaded("modmenu")
            ) {
                return p1x3lc0w.invutil.optional.clothconfig.ClothConfigUtil.getConfig()
            }

            return DefaultConfigData()
        }
    }
}