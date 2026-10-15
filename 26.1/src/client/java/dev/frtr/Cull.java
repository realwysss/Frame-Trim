package dev.frtr;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Cull {
	public static float q = 1f;
	public static int ents, blks, prts;
	public static int sEnts, sBlks, sPrts;
	static int pn;
	static int tk;
	static int slow, fast;

	static float lim(Entity e, Cfg c) {
		if (e instanceof ItemEntity) return c.di;
		if (e instanceof ExperienceOrb) return c.dx;
		if (e instanceof Projectile) return c.dp;
		if (e instanceof ArmorStand || e instanceof HangingEntity) return c.dd;
		if (e instanceof Player) return c.dpl;
		if (e instanceof LivingEntity) return c.dm;
		return c.dot;
	}

	public static boolean ent(Entity e, double x, double y, double z) {
		Cfg c = Cfg.c;
		if (!c.on || !c.ent) return false;
		double dx = e.getX() - x, dy = e.getY() - y, dz = e.getZ() - z;
		double d2 = dx * dx + dy * dy + dz * dz;
		if (d2 < 100.0) return false;
		Minecraft m = Minecraft.getInstance();
		if (e == m.getCameraEntity() || e == m.player || e.hasIndirectPassenger(m.player)) return false;
		float l = lim(e, c) * q;
		if (d2 <= (double) l * l) return false;
		ents++;
		return true;
	}

	public static boolean blk(BlockEntity b) {
		Cfg c = Cfg.c;
		if (!c.on || !c.blk) return false;
		Minecraft m = Minecraft.getInstance();
		Entity ce = m.getCameraEntity();
		if (ce == null) return false;
		double l = c.bd * q;
		if (b.getBlockPos().distToCenterSqr(ce.getX(), ce.getY(), ce.getZ()) <= l * l) return false;
		blks++;
		return true;
	}

	public static boolean prt(double x, double y, double z) {
		Cfg c = Cfg.c;
		if (!c.on || !c.prt) return false;
		if (++pn > c.pmax) {
			prts++;
			return true;
		}
		Entity ce = Minecraft.getInstance().getCameraEntity();
		if (ce == null) return false;
		double dx = x - ce.getX(), dy = y - ce.getY(), dz = z - ce.getZ();
		double l = c.pd * q;
		if (dx * dx + dy * dy + dz * dz <= l * l) return false;
		prts++;
		return true;
	}

	public static void tick(Minecraft m) {
		pn = 0;
		if (++tk < 20) return;
		tk = 0;
		sEnts = ents;
		sBlks = blks;
		sPrts = prts;
		ents = 0;
		blks = 0;
		prts = 0;
		Cfg c = Cfg.c;
		if (!c.on || !c.ad || m.level == null) {
			q = 1f;
			slow = 0;
			fast = 0;
			return;
		}
		int fps = m.getFps();
		if (fps < c.tgt) {
			fast = 0;
			if (++slow >= 2) {
				q = Math.max(c.low / 100f, q - 0.1f);
				slow = 0;
			}
		} else if (fps > c.tgt * 1.25f) {
			slow = 0;
			if (++fast >= 3) {
				q = Math.min(1f, q + 0.05f);
				fast = 0;
			}
		}
	}
}
