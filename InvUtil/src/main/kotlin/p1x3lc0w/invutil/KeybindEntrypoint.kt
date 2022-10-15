package p1x3lc0w.invutil

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ArmorItem
import net.minecraft.item.ElytraItem
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import org.lwjgl.glfw.GLFW


class KeybindEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        var autoToolKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.p1x3lc0w.invutil.autoTool", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "p1x3lc0w.invutil.key"
            )
        );

        var swapSilkTouchKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.p1x3lc0w.invutil.swapSilkTouch", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "p1x3lc0w.invutil.key"
            )
        );

        var swapElytraKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.p1x3lc0w.invutil.swapElytra", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "p1x3lc0w.invutil.key"
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(fun(client) {
            while (autoToolKeybind.wasPressed()) {
                autoTool(client)
            }

            while (swapSilkTouchKeybind.wasPressed()) {
                swapSilkTouch(client)
            }

            while (swapElytraKeybind.wasPressed()) {
                swapElytra(client)
            }
        });
    }

    companion object {
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

        const val INVENTORY_CHEST_INDEX = 38;
        const val SCREEN_HOTBAR_START = 36;
        val SCREEN_INVENTORY_RANGE = 9..35
        val SCREEN_INVENTORY_AND_HOTBAR_RANGE = 9..45
        fun swapElytra(client: MinecraftClient) {
            val currentChestItem = client.player!!.inventory.getStack(INVENTORY_CHEST_INDEX).item;
            val screenItemIndex = if (currentChestItem is ElytraItem) {
                client.player!!.currentScreenHandler.slots.indexOfFirstInRange(SCREEN_INVENTORY_AND_HOTBAR_RANGE,
                    fun(slot): Boolean {
                        val item = slot.stack.item
                        return item is ArmorItem && item.slotType == EquipmentSlot.CHEST
                    })
            } else {
                client.player!!.currentScreenHandler.slots.indexOfFirstInRange(SCREEN_INVENTORY_AND_HOTBAR_RANGE,
                    fun(slot): Boolean {
                        val item = slot.stack.item
                        return item is ElytraItem
                    })
            }

            if (screenItemIndex > 0) {
                client.swapPlayerInventorySlots(screenItemIndex, INVENTORY_CHEST_INDEX);
            }
        }

        fun swapSilkTouch(client: MinecraftClient) {
            val currentStack = client.player!!.inventory.getStack(client.player!!.inventory!!.selectedSlot)
            val currentTool = currentStack.item;
            if (currentTool is MiningToolItem) {
                val screenItemIndex = client.player!!.currentScreenHandler.slots.indexOfFirstInRange(
                    SCREEN_INVENTORY_AND_HOTBAR_RANGE,
                    fun(slot): Boolean {
                        if (slot.stack == currentStack) return false
                        val item = slot.stack.item
                        return item is MiningToolItem && currentTool.javaClass == slot.stack.item.javaClass && slot.stack.hasSilkTouch() != currentStack.hasSilkTouch()
                    })

                if (screenItemIndex > 0) {
                    client.swapPlayerInventorySlots(screenItemIndex, client.player!!.inventory!!.selectedSlot)
                }
            }
        }

        fun autoTool(client: MinecraftClient) {
            val entity = client.getCameraEntity()
            val blockHit = entity?.raycast(20.0, 0.0f, false)
            if (blockHit != null && blockHit.type == HitResult.Type.BLOCK && blockHit is BlockHitResult) {
                var blockState: BlockState? = client.world!!.getBlockState(blockHit.blockPos)

                var screenItemIndex = client.player!!.playerScreenHandler!!.slots.indexOfFirstInRange(
                    SCREEN_INVENTORY_RANGE,
                    fun(slot): Boolean {
                        val item = slot.stack.item;
                        return item is MiningToolItem && item.isSuitableFor(blockState);
                    })

                if (screenItemIndex >= 0) {
                    client.swapPlayerInventorySlots(screenItemIndex, client.player!!.inventory!!.selectedSlot)
                }
            }
        }
    }
}

fun PlayerInventory.indexOfFirstInRange(range: IntRange, predicate: (itemStack: ItemStack) -> Boolean): Int {
    for (i in range) {
        if (predicate(main[i])) return i
    }

    return -1
}

fun <T> DefaultedList<T>.indexOfFirstInRange(range: IntRange, predicate: (item: T) -> Boolean): Int {
    for (i in range) {
        if (predicate(this[i])) return i;
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