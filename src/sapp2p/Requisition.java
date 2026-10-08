package sapp2p;

import java.io.Serializable;

/** Purchase Requisition (PR): an internal request to buy something. */
public class Requisition implements Serializable, JsonSerializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public String department;
    public int materialId;
    public String materialName;
    public int quantity;
    public String status;      // Pending or Converted
    public String date;

    public String code() {
        return Store.code("PR", id);
    }

    @Override
    public String toJson() {
        return "{\"id\":" + id
                + ",\"code\":" + Json.str(code())
                + ",\"department\":" + Json.str(department)
                + ",\"materialId\":" + materialId
                + ",\"materialName\":" + Json.str(materialName)
                + ",\"quantity\":" + quantity
                + ",\"status\":" + Json.str(status)
                + ",\"date\":" + Json.str(date) + "}";
    }
}
