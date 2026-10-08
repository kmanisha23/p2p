package sapp2p;

import java.io.Serializable;

public class Supplier implements Serializable, JsonSerializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public String name;
    public String contact;

    public String code() {
        return Store.code("S", id);
    }

    @Override
    public String toJson() {
        return "{\"id\":" + id
                + ",\"code\":" + Json.str(code())
                + ",\"name\":" + Json.str(name)
                + ",\"contact\":" + Json.str(contact) + "}";
    }
}
