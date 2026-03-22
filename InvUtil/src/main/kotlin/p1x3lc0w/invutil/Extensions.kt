package p1x3lc0w.invutil

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import kotlin.jvm.optionals.getOrNull

fun PlayerInventory.indexOfFirstInRange(range: IntRange, predicate: (itemStack: ItemStack) -> Boolean, defaultValue: Int = -1): Int {
    return this.toList().indexOfFirstInRange(range, predicate, defaultValue)
}

fun <T> List<T>.indexOfFirstInRange(range: IntRange, predicate: (item: T) -> Boolean, defaultValue: Int = -1): Int {
    for (i in range) {
        if (predicate(this[i])) return i
    }

    return defaultValue
}

fun PlayerInventory.indexOfHighestInRange(range: IntRange, predicate: (itemStack: ItemStack) -> Float): Int {
    return this.toList().indexOfHighestInRange(range, predicate)
}

fun <T> List<T>.indexOfHighestInRange(range: IntRange, predicate: (item: T) -> Float): Int {
    var indexOfHighest = -1
    var highest = Float.NEGATIVE_INFINITY

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
//NOTE/FIXME: using an armor sloat as destination will break, see implementation.
fun MinecraftClient.swapPlayerInventorySlots(source: Int, destination: Int) {
    //player?.sendMessage(Text.literal("SWAP: ${source}; $destination"), false)
    if(destination < 9 || destination == 40) {
        interactionManager?.clickSlot(
            player!!.playerScreenHandler!!.syncId, source, destination, SlotActionType.SWAP, player
        )
    } else {
        // Not swapping from/to hotbar, so do the swap indirectly via hotbar since
        // interactionManager.clickSlot with SlotActionType.SWAP only allows that.
        // FIXME: This is a wired workaround but i haven't found a good way to do
        //        this directly without causing server/client desync

        // 1. Swap destination item to hotbar
        // FIXME: This swap is kind of janky since source and destination use different ids
        //        but the main inventory range lines up adn hotbar/offhand don't trigger this case
        this.swapPlayerInventorySlots(destination, 8)

        // 2. Swap destination item from hotbar to source
        this.swapPlayerInventorySlots(source, 8)

        // 3. Swap original hotbar item back
        // FIXME: see first swap
        this.swapPlayerInventorySlots(destination, 8)
    }

}

fun ItemStack.hasSilkTouch(): Boolean {
    return this.enchantments.enchantments.any {
        it.key.getOrNull()?.value?.toString() == "minecraft:silk_touch"
    }
}