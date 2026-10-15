package dev.frtr;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class FrTr implements ClientModInitializer {
	public static KeyMapping key;

	public void onInitializeClient() {
		Cfg.ld();
		KeyMapping k = key = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.frametrim.open", InputConstants.KEY_K, KeyMapping.Category.register(Identifier.fromNamespaceAndPath("frametrim", "main"))));
		ClientTickEvents.END_CLIENT_TICK.register(m -> {
			Cull.tick(m);
			while (k.consumeClick()) if (m.screen == null) m.setScreen(new dev.frtr.ui.Panel(null));
		});
	}
}
