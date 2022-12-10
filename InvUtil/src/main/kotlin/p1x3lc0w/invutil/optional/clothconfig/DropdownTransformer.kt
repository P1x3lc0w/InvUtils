package p1x3lc0w.invutil.optional.clothconfig

import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess
import me.shedaniel.autoconfig.gui.registry.api.GuiTransformer
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry
import java.lang.reflect.Field

class DropdownTransformer : GuiTransformer {
    override fun transform(
        entries: MutableList<AbstractConfigListEntry<Any>>?,
        name: String?,
        field: Field?,
        p3: Any?,
        p4: Any?,
        guiRegistryAccess: GuiRegistryAccess?
    ): MutableList<AbstractConfigListEntry<Any>>? {
        if (entries != null) {
            for (entry in entries) {
                if (entry is DropdownBoxEntry<*>) {
                    entry.isSuggestionMode = false
                }
            }
        }

        return entries
    }
}