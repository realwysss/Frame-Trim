package dev.frtr.mixin;

import dev.frtr.Cull;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class PrtMxn {
	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
	private void cp(ParticleOptions o, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<Particle> ci) {
		if (Cull.prt(x, y, z)) ci.setReturnValue(null);
	}
}
