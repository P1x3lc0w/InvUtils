package p1x3lc0w.invutil

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

class KeybindEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        val category = KeyMapping.Category(
            Identifier.tryBuild("p1x3lc0w_invutil","key")!!
        )

        val autoToolKeybind = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.p1x3lc0w.invutil.autoTool", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, category
            )
        )

        val swapSilkTouchKeybind = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.p1x3lc0w.invutil.swapSilkTouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category
            )
        )

        val swapElytraKeybind = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.p1x3lc0w.invutil.swapElytra", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, category
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(fun(client) {
            while (autoToolKeybind.consumeClick()) {
                InventoryUtil.autoTool(client)
            }

            while (swapSilkTouchKeybind.consumeClick()) {
                InventoryUtil.swapSilkTouch(client)
            }

            while (swapElytraKeybind.consumeClick()) {
                InventoryUtil.swapElytra(client)
            }
        })
    }
}