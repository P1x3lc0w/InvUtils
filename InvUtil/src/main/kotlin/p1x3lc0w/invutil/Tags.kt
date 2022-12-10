package p1x3lc0w.invutil

import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier


class Tags {
    companion object {
        val GLASS_BLOCKS = TagKey.of(Registries.BLOCK.key, Identifier("c", "glass_blocks"))
        val GLASS_PANES = TagKey.of(Registries.BLOCK.key, Identifier("c", "glass_panes"))
    }
}