package dev.frtr;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class FrTr implements ClientModInitializer {
	public static KeyMapping key;

	public void onInitializeClient() {
		Cfg.ld();
		KeyMapping k = key = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.frametrim.open", InputConstants.KEY_K, KeyMapping.Category.register(Identifier.fromNamespaceAndPath("frametrim", "main"))));
		ClientTickEvents.END_CLIENT_TICK.register(m -> {
			Cull.tick(m);
			while (k.consumeClick()) if (m.gui.screen() == null) m.gui.setScreen(new dev.frtr.ui.Panel(null));
		});
	}
}
