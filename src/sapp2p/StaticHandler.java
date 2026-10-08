package sapp2p;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/** Serves the web page files (HTML, CSS, JS) from the "web" folder. */
public class StaticHandler implements HttpHandler {

    private final File root;

    public StaticHandler(File root) {
        this.root = root;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        File file = new File(root, path).getCanonicalFile();

        // security: never serve anything outside the web folder
        boolean inside = file.getPath().startsWith(root.getCanonicalPath());
        if (!inside || !file.isFile()) {
            byte[] msg = "404 Not Found".getBytes("UTF-8");
            exchange.sendResponseHeaders(404, msg.length);
            OutputStream out = exchange.getResponseBody();
            out.write(msg);
            out.close();
            return;
        }

        byte[] bytes = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", contentType(file.getName()));
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream out = exchange.getResponseBody();
        out.write(bytes);
        out.close();
    }

    private static String contentType(String name) {
        if (name.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (name.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        return "application/octet-stream";
    }
}
