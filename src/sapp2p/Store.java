package sapp2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * All data + all business rules live here (the "service" layer).
 * Data is kept in memory and saved to the file data.bin after every change,
 * so it is still there the next time you start the program.
 */
public class Store {

    private static final String DATA_FILE = "data.bin";

    /** Everything we save, in one object. */
    static class Data implements Serializable {
        private static final long serialVersionUID = 1L;
        List<Material> materials = new ArrayList<Material>();
        List<Supplier> suppliers = new ArrayList<Supplier>();
        List<Requisition> requisitions = new ArrayList<Requisition>();
        List<PurchaseOrder> orders = new ArrayList<PurchaseOrder>();
        List<GoodsReceipt> receipts = new ArrayList<GoodsReceipt>();
    }

    private Data data;

    public Store() {
        load();
    }

    /** Display ID such as PR001. */
    public static String code(String prefix, int id) {
        return prefix + String.format("%03d", id);
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    // ---------- saving / loading ----------

    private void load() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                data = (Data) in.readObject();
            } catch (Exception e) {
                System.out.println("Could not read " + DATA_FILE + ", starting with sample data.");
                data = null;
            }
        }
        if (data == null) {
            data = new Data();
            addSampleData();
            save();
        }
    }

    private void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(data);
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    private void addSampleData() {
        addMaterial("Laptop", "Electronics", new BigDecimal("55000"), 5);
        addMaterial("Monitor", "Electronics", new BigDecimal("12000"), 15);
        addMaterial("Keyboard", "Accessories", new BigDecimal("1500"), 40);
        addSupplier("Dell Technologies", "sales@dell.example.com");
        addSupplier("HP India", "orders@hp.example.com");
        addSupplier("Logitech India", "supply@logitech.example.com");
    }

    // ---------- materials ----------

    public synchronized List<Material> getMaterials() {
        return new ArrayList<Material>(data.materials);
    }

    public synchronized Material addMaterial(String name, String category, BigDecimal unitPrice, int stock) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Material name is required");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must be zero or more");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        Material m = new Material();
        m.id = data.materials.size() + 1;
        m.name = name.trim();
        m.category = category == null ? "" : category.trim();
        m.unitPrice = unitPrice;
        m.stockQuantity = stock;
        data.materials.add(m);
        save();
        return m;
    }

    private Material findMaterial(int id) {
        for (Material m : data.materials) {
            if (m.id == id) {
                return m;
            }
        }
        throw new IllegalArgumentException("Material not found");
    }

    // ---------- suppliers ----------

    public synchronized List<Supplier> getSuppliers() {
        return new ArrayList<Supplier>(data.suppliers);
    }

    public synchronized Supplier addSupplier(String name, String contact) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Supplier name is required");
        }
        Supplier s = new Supplier();
        s.id = data.suppliers.size() + 1;
        s.name = name.trim();
        s.contact = contact == null ? "" : contact.trim();
        data.suppliers.add(s);
        save();
        return s;
    }

    private Supplier findSupplier(int id) {
        for (Supplier s : data.suppliers) {
            if (s.id == id) {
                return s;
            }
        }
        throw new IllegalArgumentException("Supplier not found");
    }

    // ---------- purchase requisitions ----------

    public synchronized List<Requisition> getRequisitions() {
        return new ArrayList<Requisition>(data.requisitions);
    }

    public synchronized Requisition addRequisition(String department, int materialId, int quantity) {
        if (department == null || department.trim().length() == 0) {
            throw new IllegalArgumentException("Department is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        Material material = findMaterial(materialId);

        Requisition pr = new Requisition();
        pr.id = data.requisitions.size() + 1;
        pr.department = department.trim();
        pr.materialId = material.id;
        pr.materialName = material.name;
        pr.quantity = quantity;
        pr.status = "Pending";
        pr.date = today();
        data.requisitions.add(pr);
        save();
        return pr;
    }

    private Requisition findRequisition(int id) {
        for (Requisition r : data.requisitions) {
            if (r.id == id) {
                return r;
            }
        }
        throw new IllegalArgumentException("Purchase requisition not found");
    }

    // ---------- purchase orders ----------

    public synchronized List<PurchaseOrder> getOrders() {
        return new ArrayList<PurchaseOrder>(data.orders);
    }

    /** Converts a Pending PR into a PO and marks the PR as Converted. */
    public synchronized PurchaseOrder createOrder(int requisitionId, int supplierId) {
        Requisition pr = findRequisition(requisitionId);
        if (!"Pending".equals(pr.status)) {
            throw new IllegalArgumentException("Only a Pending requisition can be converted to a PO");
        }
        Supplier supplier = findSupplier(supplierId);
        Material material = findMaterial(pr.materialId);

        PurchaseOrder po = new PurchaseOrder();
        po.id = data.orders.size() + 1;
        po.requisitionId = pr.id;
        po.supplierId = supplier.id;
        po.supplierName = supplier.name;
        po.materialId = material.id;
        po.materialName = material.name;
        po.quantity = pr.quantity;
        po.unitPrice = material.unitPrice;
        po.totalAmount = material.unitPrice.multiply(BigDecimal.valueOf(pr.quantity));
        po.receivedQuantity = 0;
        po.status = "Ordered";
        po.date = today();

        pr.status = "Converted";
        data.orders.add(po);
        save();
        return po;
    }

    private PurchaseOrder findOrder(int id) {
        for (PurchaseOrder o : data.orders) {
            if (o.id == id) {
                return o;
            }
        }
        throw new IllegalArgumentException("Purchase order not found");
    }

    // ---------- goods receipts ----------

    public synchronized List<GoodsReceipt> getReceipts() {
        return new ArrayList<GoodsReceipt>(data.receipts);
    }

    /**
     * Records goods received against a PO:
     * 1) updates the PO progress/status, 2) increases material stock, 3) saves the GR.
     * Everything is checked first, so nothing changes if a rule is broken.
     */
    public synchronized GoodsReceipt receiveGoods(int purchaseOrderId, int quantityReceived) {
        PurchaseOrder po = findOrder(purchaseOrderId);
        if ("Completed".equals(po.status)) {
            throw new IllegalArgumentException("This purchase order is already completed");
        }
        if (quantityReceived <= 0) {
            throw new IllegalArgumentException("Quantity received must be greater than zero");
        }
        int remaining = po.quantity - po.receivedQuantity;
        if (quantityReceived > remaining) {
            throw new IllegalArgumentException("Cannot receive " + quantityReceived
                    + ". Only " + remaining + " still pending on this order");
        }

        // 1. PO progress and status
        po.receivedQuantity = po.receivedQuantity + quantityReceived;
        boolean complete = po.receivedQuantity == po.quantity;
        po.status = complete ? "Completed" : "Partially Received";

        // 2. Inventory update
        Material material = findMaterial(po.materialId);
        material.stockQuantity = material.stockQuantity + quantityReceived;

        // 3. Goods receipt record
        GoodsReceipt gr = new GoodsReceipt();
        gr.id = data.receipts.size() + 1;
        gr.purchaseOrderId = po.id;
        gr.quantityOrdered = po.quantity;
        gr.quantityReceived = quantityReceived;
        gr.date = today();
        gr.status = complete ? "Complete" : "Partial";
        gr.poStatus = po.status;
        data.receipts.add(gr);

        save();
        return gr;
    }

    // ---------- dashboard ----------

    public synchronized String dashboardJson() {
        int pending = 0;
        for (Requisition r : data.requisitions) {
            if ("Pending".equals(r.status)) {
                pending++;
            }
        }
        int open = 0;
        for (PurchaseOrder o : data.orders) {
            if (!"Completed".equals(o.status)) {
                open++;
            }
        }
        long inventory = 0;
        for (Material m : data.materials) {
            inventory += m.stockQuantity;
        }
        return "{\"totalMaterials\":" + data.materials.size()
                + ",\"totalSuppliers\":" + data.suppliers.size()
                + ",\"pendingRequisitions\":" + pending
                + ",\"openOrders\":" + open
                + ",\"totalInventory\":" + inventory + "}";
    }
}
