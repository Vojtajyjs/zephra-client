package dev.zephra;

import net.minecraft.client.MinecraftClient;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.naming.directory.InitialDirContext;

/** Measures real latency to the server you are on (TCP round trip), because the tab-list value is often fake behind proxies. */
public class Ping {
    static volatile int ms = -1;
    static volatile long last;
    static volatile String key = "";
    static final ExecutorService EX = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "zephra-ping"); t.setDaemon(true); return t; });

    public static int get(MinecraftClient mc) {
        var info = mc.getCurrentServerEntry();
        if (info == null || mc.isInSingleplayer()) { ms = -1; return -1; }
        if (!info.address.equals(key)) { key = info.address; ms = -1; last = 0; }
        long now = System.currentTimeMillis();
        if (now - last > 8000) { last = now; String addr = info.address; EX.execute(() -> measure(addr)); }
        return ms;
    }

    static void measure(String addr) {
        try {
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
            InetAddress ip = InetAddress.getByName(host);
            long best = Long.MAX_VALUE;
            for (int k = 0; k < 3; k++) {
                long t = System.nanoTime();
                try (Socket s = new Socket()) { s.connect(new InetSocketAddress(ip, port), 2500); }
                best = Math.min(best, System.nanoTime() - t);
                Thread.sleep(120);
            }
            ms = (int) Math.max(1, best / 1_000_000);
        } catch (Exception e) { ms = -1; }
    }
}
