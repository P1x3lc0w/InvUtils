package p1x3lc0w.invutil

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ElytraItem
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import org.lwjgl.glfw.GLFW

class KeybindEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        val autoToolKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.p1x3lc0w.invutil.autoTool", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "p1x3lc0w.invutil.key"
            )
        )

        val swapSilkTouchKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.p1x3lc0w.invutil.swapSilkTouch", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "p1x3lc0w.invutil.key"
            )
        )

        val swapElytraKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.p1x3lc0w.invutil.swapElytra", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "p1x3lc0w.invutil.key"
            )
        )

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
        })
    }

    companion object {/*
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

        const val INVENTORY_CHEST_INDEX = 38
        const val SCREEN_CHEST_INDEX = 6
        //const val SCREEN_HOTBAR_START = 36

        val SCREEN_INVENTORY_RANGE = 9..35
        //val SCREEN_INVENTORY_AND_HOTBAR_RANGE = 9..45

        val HOTBAR_RANGE = 0..8

        //val INVENTORY_RANGE = 9..35
        val INVENTORY_AND_HOTBAR_RANGE = 0..35

        fun swapElytra(client: MinecraftClient) {
            val currentChestItem = client.player!!.inventory.getStack(INVENTORY_CHEST_INDEX).item
            val inventoryItemIndex = if (currentChestItem is ElytraItem) {
                client.player!!.inventory.indexOfFirstInRange(INVENTORY_AND_HOTBAR_RANGE, fun(stack): Boolean {
                    val item = stack.item
                    return item is ArmorItem && item.slotType == EquipmentSlot.CHEST
                })
            } else {
                client.player!!.inventory.indexOfFirstInRange(INVENTORY_AND_HOTBAR_RANGE, fun(stack): Boolean {
                    val item = stack.item
                    return item is ElytraItem
                })
            }

            if (inventoryItemIndex > 0) {
                client.swapPlayerInventorySlots(SCREEN_CHEST_INDEX, inventoryItemIndex)
            }
        }

        fun swapSilkTouch(client: MinecraftClient) {
            val currentStack = client.player!!.inventory.getStack(client.player!!.inventory!!.selectedSlot)
            val currentTool = currentStack.item
            if (currentTool is MiningToolItem) {
                findAndSwapTo(client, fun(stack): Boolean {
                    if (stack == currentStack) return false
                    val item = stack.item
                    return item is MiningToolItem && currentTool.javaClass == stack.item.javaClass && stack.hasSilkTouch() != currentStack.hasSilkTouch()
                })
            }
        }

        fun autoTool(client: MinecraftClient) {
            val entity = client.getCameraEntity()
            val blockHit = entity?.raycast(20.0, 0.0f, false)
            if (blockHit != null && blockHit.type == HitResult.Type.BLOCK && blockHit is BlockHitResult) {
                val blockState: BlockState? = client.world!!.getBlockState(blockHit.blockPos)

                findAndSwapTo(client, fun(stack): Boolean {
                    val item = stack.item
                    return item is MiningToolItem && item.isSuitableFor(blockState)
                })
            }
        }

        fun findAndSwapTo(client: MinecraftClient, predicate: (itemStack: ItemStack) -> Boolean) {
            val selectedIndex = client.player!!.inventory!!.selectedSlot
            val selectedStack = client.player!!.inventory!!.getStack(selectedIndex)

            val hotbarIndex = client.player!!.inventory.indexOfFirstInRange(HOTBAR_RANGE, fun(stack): Boolean {
                return stack != selectedStack && predicate(stack)
            })

            if (hotbarIndex >= 0) {
                client.player!!.inventory!!.selectedSlot = hotbarIndex
                return
            }

            val screenItemIndex =
                client.player!!.playerScreenHandler!!.slots.indexOfFirstInRange(SCREEN_INVENTORY_RANGE,
                    fun(slot): Boolean { return predicate(slot.stack) })

            if (screenItemIndex >= 0) {
                client.swapPlayerInventorySlots(screenItemIndex, selectedIndex)
            }
        }
    }
}