package sapp2p;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/** The "controller": maps /api/... URLs to Store methods and returns JSON. */
public class ApiHandler implements HttpHandler {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final Store store;

    public ApiHandler(Store store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int status = 200;
        String body;
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            Map<String, String> form = "POST".equals(method) ? readForm(exchange) : new HashMap<String, String>();
            body = route(method, path, form);
        } catch (IllegalArgumentException e) {
            status = 400;
            body = "{\"error\":" + Json.str(e.getMessage()) + "}";
        } catch (Exception e) {
            status = 500;
            body = "{\"error\":" + Json.str("Something went wrong: " + e) + "}";
        }
        byte[] bytes = body.getBytes(UTF8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream out = exchange.getResponseBody();
        out.write(bytes);
        out.close();
    }

    private String route(String method, String path, Map<String, String> form) {
        boolean get = "GET".equals(method);
        boolean post = "POST".equals(method);

        if (path.equals("/api/dashboard") && get) {
            return store.dashboardJson();
        }
        if (path.equals("/api/materials")) {
            if (get) {
                return Json.array(store.getMaterials());
            }
            if (post) {
                return store.addMaterial(form.get("name"), form.get("category"),
                        decimal(form, "unitPrice"), intOrZero(form, "stockQuantity")).toJson();
            }
        }
        if (path.equals("/api/suppliers")) {
            if (get) {
                return Json.array(store.getSuppliers());
            }
            if (post) {
                return store.addSupplier(form.get("name"), form.get("contact")).toJson();
            }
        }
        if (path.equals("/api/purchase-requisitions")) {
            if (get) {
                return Json.array(store.getRequisitions());
            }
            if (post) {
                return store.addRequisition(form.get("department"),
                        integer(form, "materialId", "Material"),
                        integer(form, "quantity", "Quantity")).toJson();
            }
        }
        if (path.equals("/api/purchase-orders")) {
            if (get) {
                return Json.array(store.getOrders());
            }
            if (post) {
                return store.createOrder(integer(form, "requisitionId", "Purchase requisition"),
                        integer(form, "supplierId", "Supplier")).toJson();
            }
        }
        if (path.equals("/api/goods-receipts")) {
            if (get) {
                return Json.array(store.getReceipts());
            }
            if (post) {
                return store.receiveGoods(integer(form, "purchaseOrderId", "Purchase order"),
                        integer(form, "quantityReceived", "Quantity received")).toJson();
            }
        }
        throw new IllegalArgumentException("Unknown request: " + method + " " + path);
    }

    // ---------- reading request data ----------

    private static int integer(Map<String, String> form, String key, String label) {
        String value = form.get(key);
        if (value == null || value.trim().length() == 0) {
            throw new IllegalArgumentException(label + " is required");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a whole number");
        }
    }

    private static int intOrZero(Map<String, String> form, String key) {
        String value = form.get(key);
        if (value == null || value.trim().length() == 0) {
            return 0;
        }
        return integer(form, key, key);
    }

    private static BigDecimal decimal(Map<String, String> form, String key) {
        String value = form.get(key);
        if (value == null || value.trim().length() == 0) {
            throw new IllegalArgumentException("Unit price is required");
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unit price must be a number");
        }
    }

    /** Reads a form-encoded body like  name=Laptop&quantity=10  into a map. */
    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        InputStream in = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        String text = new String(buffer.toByteArray(), UTF8);

        Map<String, String> map = new HashMap<String, String>();
        if (text.length() == 0) {
            return map;
        }
        String[] pairs = text.split("&");
        for (int i = 0; i < pairs.length; i++) {
            int eq = pairs[i].indexOf('=');
            String key = eq < 0 ? pairs[i] : pairs[i].substring(0, eq);
            String value = eq < 0 ? "" : pairs[i].substring(eq + 1);
            map.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
        }
        return map;
    }
}
