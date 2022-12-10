package p1x3lc0w.invutil

import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.collection.DefaultedList

fun PlayerInventory.indexOfFirstInRange(range: IntRange, predicate: (itemStack: ItemStack) -> Boolean): Int {
    val combined = main + armor + offHand
    for (i in range) {
        if (predicate(combined[i])) return i
    }

    return -1
}

fun <T> DefaultedList<T>.indexOfFirstInRange(range: IntRange, predicate: (item: T) -> Boolean): Int {
    for (i in range) {
        if (predicate(this[i])) return i
    }

    return -1
}

//NOTE: Source id is from screen slots (see above), while destination id is from combined inventory (see above)
fun MinecraftClient.swapPlayerInventorySlots(source: Int, destination: Int) {
    //player?.sendMessage(Text.literal("SWAP: ${source}; $destination"))
    interactionManager?.clickSlot(
        player!!.playerScreenHandler!!.syncId, source, destination, SlotActionType.SWAP, player
    )
}

fun ItemStack.hasSilkTouch(): Boolean {
    return enchantments.any {
        EnchantmentHelper.getIdFromNbt(it as NbtCompound?).toString().equals("minecraft:silk_touch")
    }
}