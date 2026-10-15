package dev.frtr.mixin;

import dev.frtr.Cull;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntMxn {
	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private <E extends Entity> void sr(E e, Frustum f, double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
		if (Cull.ent(e, x, y, z)) ci.setReturnValue(false);
	}
}
