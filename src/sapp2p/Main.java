package sapp2p;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/** Starts the web server on http://localhost:8080 (works on Java 7 and newer). */
public class Main {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Store store = new Store();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", new ApiHandler(store));
        server.createContext("/", new StaticHandler(new File("web")));
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("===========================================");
        System.out.println(" SAP MM-inspired Purchase-to-Pay is running");
        System.out.println(" Open in your browser:  http://localhost:" + port);
        System.out.println(" Press Ctrl + C to stop");
        System.out.println("===========================================");
    }
}
