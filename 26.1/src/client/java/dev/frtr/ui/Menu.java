package dev.frtr.ui;

import dev.frtr.Cfg;
import dev.frtr.Colors;
import dev.frtr.Cull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class Menu {
    public static final int BACK = 1;
    public static final int ENTER = 2;
    public static final int ESCAPE = 3;
    public static final int TAB = 4;
    public static final int PASTE = 5;
    public static final int COPY = 6;
    private static final int INK = 0xFF22201C;
    private static final int SOFT = 0xFF857F73;
    private static final int WELL = 0x14000000;
    private static final int ACC = 0xFF1FA27E;
    private static final int PAPER_TOP = 0xFFF8F3E9;
    private static final int PAPER_BOT = 0xFFEAE3D4;
    private static final String[] TABS = {"Entities", "Particles", "Blocks", "Auto"};

    public interface Drag {
        void to(double x, double y);
    }

    private record Hit(String id, float x, float y, float w, float h, Runnable click, Drag drag, DoubleConsumer scroll) {
        boolean in(double px, double py) {
            return px >= x && py >= y && px < x + w && py < y + h;
        }
    }

    private final Cfg start;
    private final Runnable close;
    private final List<Hit> hits = new ArrayList<>();
    private final Map<String, Float> fx = new HashMap<>();
    private String active;
    private Drag drag;
    private int tab;
    private float open;
    private long last;
    private float dt;
    private double mx;
    private double my;
    private int fps;
    private float fpsT;

    public Menu(Runnable close, Supplier<String> clipboard) {
        this.close = close;
        this.start = Cfg.c.copy();
    }

    public void cancel() {
        Cfg.c = start.copy();
    }

    public void save() {
        Cfg.sv();
    }

    public void draw(Canvas g, int width, int height, double mouseX, double mouseY) {
        long now = System.nanoTime();
        dt = last == 0L ? 0f : Math.min((now - last) / 1e9f, 0.1f);
        last = now;
        mx = mouseX;
        my = mouseY;
        hits.clear();
        open = ease(open, 1f, 8f);
        Cfg c = Cfg.c;
        fpsT += dt;
        if (fpsT > 0.4f || fps == 0) {
            fpsT = 0f;
            fps = net.minecraft.client.Minecraft.getInstance().getFps();
        }

        float cw = Math.min(width - 24, 360);
        float ch = Math.min(height - 20, 262);
        float x = (width - cw) / 2f;
        float y = (height - ch) / 2f + (1f - open) * 24f;

        g.fill(0, 0, width, height, 0, fade(0x30000000), fade(0x70000000));
        g.shadow(x, y + 8, cw, ch, 18, 30, fade(0x80000000));
        g.fill(x, y, cw, ch, 16, fade(PAPER_TOP), fade(PAPER_BOT));
        g.ring(x, y, cw, ch, 16, 1, fade(0x55FFFFFF), fade(0x22000000));

        float pad = 16;
        float iw = cw - pad * 2;
        float ix = x + pad;

        g.text("Frame Trim", ix, y + 17, fade(INK));
        String fp = fps + " fps";
        float tx = x + cw - pad - 22;
        Runnable flip = () -> c.on = !c.on;
        toggle(g, "master", tx, y + 14, c.on, flip);
        g.text(fp, tx - 10 - g.width(fp), y + 17, fade(ACC));
        if (c.on && Cull.q < 0.999f) {
            String qs = "quality " + Math.round(Cull.q * 100) + "%";
            g.text(qs, ix + 70, y + 17, fade(SOFT));
        }

        segmented(g, "tabs", ix, y + 36, iw, 18, TABS, tab, i -> tab = i);

        float top = y + 66;
        float space = y + ch - 42 - top;
        switch (tab) {
            case 0 -> entities(g, c, ix, top, iw, space);
            case 1 -> particles(g, c, ix, top, iw);
            case 2 -> blocks(g, c, ix, top, iw);
            default -> auto(g, c, ix, top, iw);
        }

        float by = y + ch - 32;
        button(g, "reset", "Defaults", ix, by, 64, false, () -> Cfg.c = new Cfg());
        float right = x + cw - pad;
        button(g, "done", "Done", right - 60, by, 60, true, () -> {
            save();
            close.run();
        });
        button(g, "cancel", "Cancel", right - 60 * 2 - 6, by, 60, false, () -> {
            cancel();
            close.run();
        });
    }

    private void entities(Canvas g, Cfg c, float x, float y, float w, float space) {
        row(g, "ent", "Skip far entities", x, y, w, c.ent, () -> c.ent = !c.ent);
        float gap = 16;
        float cw = (w - gap) / 2f;
        float y0 = y + 24;
        float step = 24;
        dist(g, "di", "Items", x, y0, cw, c.di, v -> c.di = v);
        dist(g, "dx", "XP orbs", x, y0 + step, cw, c.dx, v -> c.dx = v);
        dist(g, "dp", "Projectiles", x, y0 + step * 2, cw, c.dp, v -> c.dp = v);
        dist(g, "dd", "Frames and stands", x, y0 + step * 3, cw, c.dd, v -> c.dd = v);
        float x2 = x + cw + gap;
        dist(g, "dm", "Mobs", x2, y0, cw, c.dm, v -> c.dm = v);
        dist(g, "dpl", "Players", x2, y0 + step, cw, c.dpl, v -> c.dpl = v);
        dist(g, "dot", "Everything else", x2, y0 + step * 2, cw, c.dot, v -> c.dot = v);
        g.text("never within 10 blocks", x2, y0 + step * 3 + 2, fade(SOFT));
    }

    private void dist(Canvas g, String id, String label, float x, float y, float w, int v, IntConsumer set) {
        slider(g, id, label, x, y, w, v, 8f, 256f, 2f, d -> set.accept((int) d), v >= 256 ? "Off" : v + "");
    }

    private void particles(Canvas g, Cfg c, float x, float y, float w) {
        row(g, "prt", "Skip far particles", x, y, w, c.prt, () -> c.prt = !c.prt);
        slider(g, "pd", "Distance", x, y + 30, w, c.pd, 8f, 128f, 2f, v -> c.pd = (int) v, c.pd + " blocks");
        slider(g, "pmax", "Most new particles per tick", x, y + 62, w, c.pmax, 50f, 5000f, 50f, v -> c.pmax = (int) v, String.valueOf(c.pmax));
        g.text("skipped last second: " + Cull.sPrts, x, y + 96, fade(SOFT));
    }

    private void blocks(Canvas g, Cfg c, float x, float y, float w) {
        row(g, "blk", "Skip far block entities", x, y, w, c.blk, () -> c.blk = !c.blk);
        slider(g, "bd", "Distance", x, y + 30, w, c.bd, 8f, 128f, 2f, v -> c.bd = (int) v, c.bd + " blocks");
        g.text("chests, signs, banners, beds, skulls and so on", x, y + 64, fade(SOFT));
        g.text("skipped last second: " + Cull.sBlks, x, y + 78, fade(SOFT));
    }

    private void auto(Canvas g, Cfg c, float x, float y, float w) {
        row(g, "ad", "Adaptive quality", x, y, w, c.ad, () -> c.ad = !c.ad);
        slider(g, "tgt", "Target fps", x, y + 30, w, c.tgt, 20f, 360f, 5f, v -> c.tgt = (int) v, String.valueOf(c.tgt));
        slider(g, "low", "Never go below", x, y + 62, w, c.low, 20f, 90f, 5f, v -> c.low = (int) v, c.low + "%");
        g.text("when fps stays under the target every limit above", x, y + 96, fade(SOFT));
        g.text("shrinks, and it grows back when there is room", x, y + 108, fade(SOFT));
        g.text("entities skipped last second: " + Cull.sEnts, x, y + 124, fade(SOFT));
    }

    private void row(Canvas g, String id, String label, float x, float y, float w, boolean on, Runnable flip) {
        boolean over = hover(x - 4, y - 3, w + 8, 20);
        float k = anim(id + "row", over ? 1f : 0f, 14f);
        if (k > 0.01f) g.fill(x - 4, y - 3, w + 8, 20, 8, fade(Colors.alpha(0x0E000000, k)));
        g.text(label, x + 2, y + 2, fade(on ? INK : SOFT));
        toggle(g, id, x + w - 24, y, on, flip);
        hits.add(new Hit(id + "row", x - 4, y - 3, w + 8, 20, flip, null, null));
    }

    private void toggle(Canvas g, String id, float x, float y, boolean on, Runnable flip) {
        float w = 22;
        float h = 12;
        float t = anim(id, on ? 1f : 0f, 14f);
        g.fill(x, y, w, h, h / 2f, fade(Colors.mix(0x26000000, Colors.mix(ACC, 0xFFFFFFFF, 0.1f), t)), fade(Colors.mix(0x3A000000, ACC, t)));
        g.ring(x, y, w, h, h / 2f, 1, fade(0x33000000), fade(0x0F000000));
        float k = 8;
        float kx = x + 2 + (w - 4 - k) * t;
        float ky = y + (h - k) / 2f;
        g.shadow(kx, ky + 0.5f, k, k, k / 2f, 2, fade(0x55000000));
        g.fill(kx, ky, k, k, k / 2f, fade(0xFFFFFFFF), fade(0xFFEDEDED));
        hits.add(new Hit(id, x - 2, y - 2, w + 4, h + 4, flip, null, null));
    }

    private void slider(Canvas g, String id, String label, float x, float y, float w, float v, float lo, float hi, float step, DoubleConsumer set, String shown) {
        g.text(label, x, y, fade(SOFT));
        g.text(shown, x + w - g.width(shown), y, fade(INK));
        float ty = y + 11;
        float th = 5;
        float t = Colors.clamp((v - lo) / (hi - lo), 0f, 1f);
        boolean held = id.equals(active);
        float k = anim(id, held || hover(x - 4, ty - 6, w + 8, th + 12) ? 1f : 0f, 16f);
        float at = anim(id + "v", t, 22f);
        g.fill(x, ty, w, th, th / 2f, fade(0x22000000));
        g.fill(x, ty, Math.max(th, w * at), th, th / 2f, fade(Colors.mix(ACC, 0xFFFFFFFF, 0.2f)), fade(ACC));
        float s = 9 + k * 2;
        float kx = x + w * at - s / 2f;
        float ky = ty + th / 2f - s / 2f;
        g.shadow(kx, ky + 0.5f, s, s, s / 2f, 2.5f, fade(0x66000000));
        g.fill(kx, ky, s, s, s / 2f, fade(0xFFFFFFFF), fade(0xFFE9E9E9));
        g.ring(kx, ky, s, s, s / 2f, 1, fade(0x30000000));
        Drag d = (px, py) -> set.accept(snap(lo + (hi - lo) * Colors.clamp((float) ((px - x) / w), 0f, 1f), step, lo, hi));
        hits.add(new Hit(id, x - 4, ty - 7, w + 8, th + 14, null, d, a -> set.accept(snap(v + (float) Math.signum(a) * Math.max(step, (hi - lo) / 20f), step, lo, hi))));
    }

    private void segmented(Canvas g, String id, float x, float y, float w, float h, String[] items, int sel, IntConsumer pick) {
        g.fill(x, y, w, h, h / 2f, fade(WELL));
        g.ring(x, y, w, h, h / 2f, 1, fade(0x1E000000), fade(0x0A000000));
        int n = items.length;
        float[] edge = new float[n + 1];
        float sum = 0;
        for (String s : items) sum += g.width(s);
        float pad = (w - sum) / n;
        edge[0] = x;
        for (int i = 0; i < n; i++) edge[i + 1] = edge[i] + g.width(items[i]) + pad;
        edge[n] = x + w;
        float px = anim(id, edge[sel], 16f);
        float pw = anim(id + "w", edge[sel + 1] - edge[sel], 16f);
        g.shadow(px + 2, y + 2, pw - 4, h - 4, (h - 4) / 2f, 3, fade(0x40000000));
        g.fill(px + 2, y + 2, pw - 4, h - 4, (h - 4) / 2f, fade(0xFF4A473F), fade(INK));
        for (int i = 0; i < n; i++) {
            float ix = edge[i];
            float iw = edge[i + 1] - ix;
            boolean over = hover(ix, y, iw, h);
            float k = anim(id + i, over ? 1f : 0f, 14f);
            int col = i == sel ? 0xFFFFFFFF : Colors.mix(SOFT, INK, k * 0.7f);
            g.text(items[i], ix + (iw - g.width(items[i])) / 2f, y + (h - 8) / 2f, fade(col));
            int idx = i;
            hits.add(new Hit(id + i, ix, y, iw, h, () -> pick.accept(idx), null, a -> pick.accept(Math.floorMod(sel - (int) Math.signum(a), n))));
        }
    }

    private void button(Canvas g, String id, String label, float x, float y, float w, boolean main, Runnable run) {
        float h = 20;
        boolean over = hover(x, y, w, h);
        float k = anim(id, over ? 1f : 0f, 14f);
        if (main) {
            g.shadow(x, y + 1, w, h, 10, 5, fade(0x55000000));
            g.fill(x, y, w, h, 10, fade(Colors.mix(0xFF4A473F, 0xFF66625A, k)), fade(Colors.mix(INK, 0xFF3A372F, k)));
            g.text(label, x + (w - g.width(label)) / 2f, y + 6, fade(0xFFFFFFFF));
        } else {
            g.fill(x, y, w, h, 10, fade(Colors.mix(0x00000000, 0x16000000, k)));
            g.ring(x, y, w, h, 10, 1, fade(Colors.mix(0x40000000, 0x66000000, k)));
            g.text(label, x + (w - g.width(label)) / 2f, y + 6, fade(INK));
        }
        hits.add(new Hit(id, x, y, w, h, run, null, null));
    }

    public boolean click(double x, double y, int button) {
        mx = x;
        my = y;
        if (button != 0) return false;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.in(x, y)) continue;
            if (h.drag != null) {
                active = h.id;
                drag = h.drag;
                drag.to(x, y);
            }
            if (h.click != null) h.click.run();
            return true;
        }
        return false;
    }

    public boolean release(double x, double y, int button) {
        boolean was = drag != null;
        drag = null;
        active = null;
        return was;
    }

    public boolean dragged(double x, double y) {
        mx = x;
        my = y;
        if (drag == null) return false;
        drag.to(x, y);
        return true;
    }

    public boolean scroll(double x, double y, double amount) {
        if (amount == 0) return false;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (h.scroll != null && h.in(x, y)) {
                h.scroll.accept(amount);
                return true;
            }
        }
        return false;
    }

    public boolean key(int action) {
        return false;
    }

    public boolean type(int codepoint) {
        return false;
    }

    public boolean typing() {
        return false;
    }

    private boolean hover(float x, float y, float w, float h) {
        return drag == null && mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private float anim(String key, float target, float speed) {
        Float cur = fx.get(key);
        float v = cur == null ? target : ease(cur, target, speed);
        fx.put(key, v);
        return v;
    }

    private float ease(float cur, float to, float speed) {
        float v = cur + (to - cur) * (1f - (float) Math.exp(-speed * dt));
        return Math.abs(to - v) < 0.0005f ? to : v;
    }

    private int fade(int argb) {
        return Colors.alpha(argb, open);
    }

    private static float snap(float v, float step, float lo, float hi) {
        float s = Math.round(v / step) * step;
        return Colors.clamp(s, lo, hi);
    }
}
