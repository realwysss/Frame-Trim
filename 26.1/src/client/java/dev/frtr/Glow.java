package dev.frtr;

import net.minecraft.resources.Identifier;

public final class Glow {
	public static final String ID = "frametrim";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
