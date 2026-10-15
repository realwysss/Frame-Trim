package dev.frtr.mixin;

import dev.frtr.Cull;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlkMxn {
	@Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
	private <E extends BlockEntity, S extends BlockEntityRenderState> void tx(E b, float pt, ModelFeatureRenderer.CrumblingOverlay co, CallbackInfoReturnable<S> ci) {
		if (Cull.blk(b)) ci.setReturnValue(null);
	}
}
