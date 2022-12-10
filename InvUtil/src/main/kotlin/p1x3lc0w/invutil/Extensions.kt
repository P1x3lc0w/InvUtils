package p1x3lc0w.invutil

import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.slot.SlotActionType

fun PlayerInventory.indexOfFirstInRange(range: IntRange, predicate: (itemStack: ItemStack) -> Boolean): Int {
    val combined = main + armor + offHand
    return combined.indexOfFirstInRange(range, predicate)
}

fun <T> List<T>.indexOfFirstInRange(range: IntRange, predicate: (item: T) -> Boolean): Int {
    for (i in range) {
        if (predicate(this[i])) return i
    }

    return -1
}

fun PlayerInventory.indexOfHighestInRange(range: IntRange, predicate: (itemStack: ItemStack) -> Int): Int {
    val combined = main + armor + offHand
    return combined.indexOfHighestInRange(range, predicate)
}

fun <T> List<T>.indexOfHighestInRange(range: IntRange, predicate: (item: T) -> Int): Int {
    var indexOfHighest = -1
    var highest = Int.MIN_VALUE

    for (i in range) {
        val value = predicate(this[i])

        if (value >= 0 && value > highest) {
            indexOfHighest = i
            highest = value
        }
    }

    return indexOfHighest
}

/*
* Player Screen slot layout
* 0 crafting result
* 1-4 crafting input
* 5-8 Equipment
* 9-35 inventory
* 36-44 hotbar
* 45 offhand
*
* Player combined inventory layout
* 0-35 inventory
* 36-39 armor
* 40 offhand
*/
//NOTE: Source id is from screen slots (see above), while destination id is from combined inventory (see above)
fun MinecraftClient.swapPlayerInventorySlots(source: Int, destination: Int) {
    //player?.sendMessage(Text.literal("SWAP: ${source}; $destination"))
    interactionManager?.clickSlot(
        player!!.playerScreenHandler!!.syncId, source, destination, SlotActionType.SWAP, player
    )
}

fun ItemStack.hasSilkTouch(): Boolean {
    return enchantments.any {
        EnchantmentHelper.getIdFromNbt(it as NbtCompound?).toString() == "minecraft:silk_touch"
    }
}