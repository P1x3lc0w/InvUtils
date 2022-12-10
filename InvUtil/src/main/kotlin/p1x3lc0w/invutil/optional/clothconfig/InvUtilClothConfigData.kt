package p1x3lc0w.invutil.optional.clothconfig

import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry
import p1x3lc0w.invutil.config.AutoToolConfig
import p1x3lc0w.invutil.config.IInvUtilConfig

@Config(name = "p1x3lc0w.invutil")
class InvUtilClothConfigData : ConfigData, IInvUtilConfig {
    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("autotool")
    @ConfigEntry.Gui.PrefixText
    override var autoToolConfig: AutoToolConfig = AutoToolConfig()
}