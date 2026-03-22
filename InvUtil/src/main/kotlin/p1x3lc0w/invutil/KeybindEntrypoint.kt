package p1x3lc0w.invutil

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

class KeybindEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        val category = KeyBinding.Category(
            Identifier.of("p1x3lc0w_invutil","key")
        )

        val autoToolKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.p1x3lc0w.invutil.autoTool", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, category
            )
        )

        val swapSilkTouchKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.p1x3lc0w.invutil.swapSilkTouch", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, category
            )
        )

        val swapElytraKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.p1x3lc0w.invutil.swapElytra", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, category
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(fun(client) {
            while (autoToolKeybind.wasPressed()) {
                InventoryUtil.autoTool(client)
            }

            while (swapSilkTouchKeybind.wasPressed()) {
                InventoryUtil.swapSilkTouch(client)
            }

            while (swapElytraKeybind.wasPressed()) {
                InventoryUtil.swapElytra(client)
            }
        })
    }
}