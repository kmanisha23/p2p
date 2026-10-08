package sapp2p;

import java.io.Serializable;
import java.math.BigDecimal;

/** Purchase Order (PO): a formal order sent to a supplier. */
public class PurchaseOrder implements Serializable, JsonSerializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public int requisitionId;
    public int supplierId;
    public String supplierName;
    public int materialId;
    public String materialName;
    public int quantity;
    public BigDecimal unitPrice;
    public BigDecimal totalAmount;
    public int receivedQuantity;   // running total of goods received
    public String status;          // Ordered, Partially Received or Completed
    public String date;

    public String code() {
        return Store.code("PO", id);
    }

    @Override
    public String toJson() {
        return "{\"id\":" + id
                + ",\"code\":" + Json.str(code())
                + ",\"prCode\":" + Json.str(Store.code("PR", requisitionId))
                + ",\"supplierName\":" + Json.str(supplierName)
                + ",\"materialName\":" + Json.str(materialName)
                + ",\"quantity\":" + quantity
                + ",\"unitPrice\":" + unitPrice.toPlainString()
                + ",\"totalAmount\":" + totalAmount.toPlainString()
                + ",\"receivedQuantity\":" + receivedQuantity
                + ",\"status\":" + Json.str(status)
                + ",\"date\":" + Json.str(date) + "}";
    }
}
