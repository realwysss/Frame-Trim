package dev.frtr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class Cfg {
	public boolean on = true;
	public boolean ent = true;
	public int di = 32;
	public int dx = 24;
	public int dp = 64;
	public int dd = 64;
	public int dm = 80;
	public int dpl = 256;
	public int dot = 128;
	public boolean prt = true;
	public int pd = 48;
	public int pmax = 600;
	public boolean blk = true;
	public int bd = 48;
	public boolean ad = false;
	public int tgt = 60;
	public int low = 40;

	static final Gson G = new GsonBuilder().setPrettyPrinting().create();
	static final Path P = FabricLoader.getInstance().getConfigDir().resolve("frametrim.json");
	public static Cfg c = new Cfg();

	public Cfg copy() {
		return G.fromJson(G.toJson(this), Cfg.class);
	}

	static int cl(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}

	public static void ld() {
		try {
			if (Files.exists(P)) {
				Cfg n = G.fromJson(Files.readString(P), Cfg.class);
				if (n != null) c = n;
			}
		} catch (Exception e) {
			c = new Cfg();
		}
		fx();
	}

	public static void fx() {
		c.di = cl(c.di, 8, 256);
		c.dx = cl(c.dx, 8, 256);
		c.dp = cl(c.dp, 8, 256);
		c.dd = cl(c.dd, 8, 256);
		c.dm = cl(c.dm, 8, 256);
		c.dpl = cl(c.dpl, 8, 256);
		c.dot = cl(c.dot, 8, 256);
		c.pd = cl(c.pd, 8, 128);
		c.pmax = cl(c.pmax, 50, 5000);
		c.bd = cl(c.bd, 8, 128);
		c.tgt = cl(c.tgt, 20, 360);
		c.low = cl(c.low, 20, 90);
	}

	public static void sv() {
		fx();
		try {
			Files.writeString(P, G.toJson(c));
		} catch (Exception e) {
		}
	}
}
