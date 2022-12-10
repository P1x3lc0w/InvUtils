package p1x3lc0w.invutil.optional.clothconfig

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer
import net.minecraft.client.gui.screen.Screen
import p1x3lc0w.invutil.config.IInvUtilConfig

class ClothConfigUtil {
    companion object {
        private var _registered: Boolean = false

        fun getConfig(): IInvUtilConfig {
            if (!_registered)
                registerConfig()

            return AutoConfig.getConfigHolder(InvUtilClothConfigData::class.java).get()
        }

        fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
            if (!_registered)
                registerConfig()

            return ConfigScreenFactory { parent: Screen? ->
                AutoConfig.getConfigScreen(InvUtilClothConfigData::class.java, parent).get()
            }
        }

        private fun registerConfig() {
            AutoConfig.register(InvUtilClothConfigData::class.java) { definition: Config?, configClass: Class<InvUtilClothConfigData?>? ->
                Toml4jConfigSerializer(definition, configClass)
            }

            _registered = true
        }
    }
}