package sapp2p;

import java.io.Serializable;
import java.math.BigDecimal;

public class Material implements Serializable, JsonSerializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public String name;
    public String category;
    public BigDecimal unitPrice;
    public int stockQuantity;

    public String code() {
        return Store.code("M", id);
    }

    @Override
    public String toJson() {
        return "{\"id\":" + id
                + ",\"code\":" + Json.str(code())
                + ",\"name\":" + Json.str(name)
                + ",\"category\":" + Json.str(category)
                + ",\"unitPrice\":" + unitPrice.toPlainString()
                + ",\"stockQuantity\":" + stockQuantity + "}";
    }
}
