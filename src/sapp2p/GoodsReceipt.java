package sapp2p;

import java.io.Serializable;

/** Goods Receipt (GR): records goods that physically arrived against a PO. */
public class GoodsReceipt implements Serializable, JsonSerializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public int purchaseOrderId;
    public int quantityOrdered;
    public int quantityReceived;
    public String date;
    public String status;          // Partial or Complete
    public String poStatus;        // PO status after this receipt

    public String code() {
        return Store.code("GR", id);
    }

    @Override
    public String toJson() {
        return "{\"id\":" + id
                + ",\"code\":" + Json.str(code())
                + ",\"poCode\":" + Json.str(Store.code("PO", purchaseOrderId))
                + ",\"quantityOrdered\":" + quantityOrdered
                + ",\"quantityReceived\":" + quantityReceived
                + ",\"date\":" + Json.str(date)
                + ",\"status\":" + Json.str(status)
                + ",\"poStatus\":" + Json.str(poStatus) + "}";
    }
}
