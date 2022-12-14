package p1x3lc0w.invutil

import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ElytraItem
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import p1x3lc0w.invutil.config.AutoToolTargetSlot
import p1x3lc0w.invutil.config.Config

class InventoryUtil {
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

        const val INVENTORY_CHEST_INDEX = 38
        const val SCREEN_CHEST_INDEX = 6
        const val SCREEN_HOTBAR_START = 36

        val SCREEN_INVENTORY_RANGE = 9..35
        val SCREEN_INVENTORY_AND_HOTBAR_RANGE = 9..45

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

        fun swapSilkTouch(client: MinecraftClient, matchToolType: Boolean = true) {
            val config = Config.getConfig()

            val currentStack = client.player!!.inventory.getStack(client.player!!.inventory!!.selectedSlot)

            if (config.autoToolConfig.prioritizeHigherMiningLevelTools) {
                findAndSwapToHighest(client, fun(stack): Int {
                    val item = stack.item
                    // Item is not a tool -> not a match.
                    if (item !is MiningToolItem) return -1

                    return if (isSuitableSilkTouchItemStack(
                            stack,
                            currentStack,
                            matchToolType
                        )
                    ) item.material.miningLevel else -1
                })
            } else {
                findAndSwapTo(client, fun(stack): Boolean {
                    return isSuitableSilkTouchItemStack(stack, currentStack, matchToolType)
                })
            }
        }

        private fun isSuitableSilkTouchItemStack(
            stack: ItemStack,
            currentStack: ItemStack,
            matchToolType: Boolean
        ): Boolean {
            val currentTool = currentStack.item
            if (stack == currentStack) return false

            val item = stack.item
            // Item is not a tool -> not a match.
            if (item !is MiningToolItem) return false

            if (currentTool is MiningToolItem) {
                //We are currently holding a tool.

                //If matchToolType is true, check if the item is the same tool as the one we are holding,
                if (matchToolType && currentTool.javaClass != stack.item.javaClass)
                    return false

                //We want to swap from Silk Touch to non-Silk Touch and vice versa.
                if (matchToolType && stack.hasSilkTouch() == currentStack.hasSilkTouch())
                    return false

                if ((!matchToolType) && (!stack.hasSilkTouch()))
                    return false

                return true
            } else {
                //We are currently not holding a tool, so no match if matchToolType is true.
                if (matchToolType)
                    return false

                if (!stack.hasSilkTouch())
                    return false

                return true
            }
        }

        fun autoTool(client: MinecraftClient) {
            val config = Config.getConfig()

            val entity = client.getCameraEntity()
            val blockHit = entity?.raycast(20.0, 0.0f, false)

            if (blockHit != null && blockHit.type == HitResult.Type.BLOCK && blockHit is BlockHitResult) {
                val blockState: BlockState = client.world!!.getBlockState(blockHit.blockPos) ?: return

                if (config.autoToolConfig.glassSilkTouch && BlockUtil.isGlass(blockState.block)) {
                    swapSilkTouch(client, false)
                    return
                }

                if (config.autoToolConfig.prioritizeHigherMiningLevelTools) {
                    findAndSwapToHighest(client, fun(stack): Int {
                        val item = stack.item
                        if (item is MiningToolItem && item.isSuitableFor(blockState)) {
                            return item.material.miningLevel
                        }

                        return -1
                    })
                } else {
                    findAndSwapTo(client, fun(stack): Boolean {
                        val item = stack.item
                        return item is MiningToolItem && item.isSuitableFor(blockState)
                    })
                }
            }
        }

        fun findAndSwapTo(client: MinecraftClient, predicate: (itemStack: ItemStack) -> Boolean) {
            val selectedIndex = client.player!!.inventory!!.selectedSlot
            val selectedStack = client.player!!.inventory!!.getStack(selectedIndex)

            val screenItemIndex = client.player!!.playerScreenHandler!!.slots.indexOfFirstInRange(
                SCREEN_INVENTORY_AND_HOTBAR_RANGE, fun(slot): Boolean {
                    return slot.stack != selectedStack && predicate(slot.stack)
                })

            if (screenItemIndex >= 0) {
                swapTo(
                    client,
                    getHotbarSlotOffset(client, Config.getConfig().autoToolConfig.targetSlot),
                    screenItemIndex
                )
            }
        }

        fun findAndSwapToHighest(client: MinecraftClient, predicate: (itemStack: ItemStack) -> Int) {
            val selectedIndex = client.player!!.inventory!!.selectedSlot
            val selectedStack = client.player!!.inventory!!.getStack(selectedIndex)

            val screenItemIndex = client.player!!.playerScreenHandler!!.slots.indexOfHighestInRange(
                SCREEN_INVENTORY_AND_HOTBAR_RANGE,
                fun(slot): Int {
                    if (slot.stack == selectedStack)
                        return -1

                    return predicate(slot.stack)
                })

            if (screenItemIndex >= 0) {
                swapTo(
                    client,
                    getHotbarSlotOffset(client, Config.getConfig().autoToolConfig.targetSlot),
                    screenItemIndex
                )
            }
        }

        fun swapTo(client: MinecraftClient, targetSlot: Int, screenItemIndex: Int) {
            if (targetSlot != SCREEN_HOTBAR_START + screenItemIndex) {
                client.swapPlayerInventorySlots(screenItemIndex, targetSlot)
            }

            client.player!!.inventory!!.selectedSlot = targetSlot
        }

        fun getHotbarSlotOffset(client: MinecraftClient, target: AutoToolTargetSlot): Int {
            return when (target) {
                AutoToolTargetSlot.Selected -> client.player!!.inventory!!.selectedSlot
                AutoToolTargetSlot.FirstTool -> client.player!!.inventory!!.indexOfFirstInRange(
                    HOTBAR_RANGE,
                    fun(stack): Boolean {
                        return stack.item is MiningToolItem
                    },
                    client.player!!.inventory!!.selectedSlot
                )
                AutoToolTargetSlot.Slot0 -> 0
                AutoToolTargetSlot.Slot1 -> 1
                AutoToolTargetSlot.Slot2 -> 2
                AutoToolTargetSlot.Slot3 -> 3
                AutoToolTargetSlot.Slot4 -> 4
                AutoToolTargetSlot.Slot5 -> 5
                AutoToolTargetSlot.Slot6 -> 6
                AutoToolTargetSlot.Slot7 -> 7
                AutoToolTargetSlot.Slot8 -> 8
            }
        }
    }
}