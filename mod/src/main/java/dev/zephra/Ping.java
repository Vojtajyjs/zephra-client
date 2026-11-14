package dev.zephra;

import net.minecraft.client.MinecraftClient;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.naming.directory.InitialDirContext;

/** Measures real latency to the server you are on (TCP round trip). The tab-list value is often fake behind proxies. */
public class Ping {
    static volatile int ms = -1;
    static volatile long last;
    static volatile boolean busy;
    static volatile String key = "";
    static InetAddress cip;
    static int cport;
    static String cfor = "";
    static final ExecutorService EX = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "zephra-ping"); t.setDaemon(true); return t; });

    public static int get(MinecraftClient mc) {
        var info = mc.getCurrentServerEntry();
        if (info == null || mc.isInSingleplayer()) { ms = -1; return -1; }
        if (!info.address.equals(key)) { key = info.address; ms = -1; last = 0; }
        long now = System.currentTimeMillis();
        if (!busy && now - last > 3000) { last = now; busy = true; String addr = info.address; EX.execute(() -> measure(addr)); }
        return ms;
    }

    static void resolve(String addr) throws Exception {
        String host = addr.trim(); int port = 25565; boolean explicit = false;
        int i = host.lastIndexOf(':');
        if (i > 0 && host.indexOf(':') == i) { port = Integer.parseInt(host.substring(i + 1)); host = host.substring(0, i); explicit = true; }
        if (!explicit) {
            try {
                var env = new Hashtable<String, String>();
                env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                env.put("java.naming.provider.url", "dns:");
                var a = new InitialDirContext(env).getAttributes("_minecraft._tcp." + host, new String[]{"SRV"}).get("srv");
                if (a != null) {
                    String[] p = a.get().toString().split(" ");
                    port = Integer.parseInt(p[2]);
                    host = p[3].endsWith(".") ? p[3].substring(0, p[3].length() - 1) : p[3];
                }
            } catch (Exception ignored) {}
        }
        cip = InetAddress.getByName(host); cport = port; cfor = addr;
    }

    static void measure(String addr) {
        try {
            if (cip == null || !addr.equals(cfor)) resolve(addr);
            long t = System.nanoTime();
            try (Socket s = new Socket()) { s.connect(new InetSocketAddress(cip, cport), 2000); }
            ms = (int) Math.max(1, (System.nanoTime() - t) / 1_000_000);
        } catch (Exception e) { ms = -1; cip = null; }
        finally { busy = false; }
    }
}
