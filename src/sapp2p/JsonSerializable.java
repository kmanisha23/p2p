package sapp2p;

/** Anything that can turn itself into a JSON string for the browser. */
public interface JsonSerializable {
    String toJson();
}
