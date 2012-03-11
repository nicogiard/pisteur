package utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Map;

public class BEncode {

    public static String encode(Object x) {
        if (x instanceof Integer || x instanceof Long) {
            return encodeNumber((Number) x);
        } else if (x instanceof String) {
            return encodeString((String) x);
        } else if (x instanceof List) {
            return encodeList((List) x);
        } else if (x instanceof JsonObject) {
            return encodeDictionary((JsonObject) x);
        }
        return "";
    }
    
    
    private static String encodeNumber(Number x) {
        return new StringBuilder().append("i").append(x).append("e").toString();
    }

    private static String encodeString(String x) {
        return new StringBuilder().append(x.length()).append(":").append(x).toString();
    }

    private static String encodeList(List x) {
        StringBuilder sb = new StringBuilder().append("l");
        for (Object o : x) {
            sb.append(encode(o));
        }
        return sb.append("e").toString();
    }

    private static String encodeDictionary(JsonObject x) {
        StringBuilder sb = new StringBuilder().append("d");
        for (Map.Entry<String, JsonElement> entry : x.entrySet()) {
            sb.append(entry.getKey().length()).append(":").append(entry.getKey());
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isString()) {
                    sb.append(encodeString(value.getAsString()));
                } else if (primitive.isNumber()) {
                    sb.append(encodeNumber(value.getAsNumber()));
                }

            } else if (value.isJsonObject()) {
                sb.append(encodeDictionary(value.getAsJsonObject()));
            }
        }
        return sb.append("e").toString();
    }
}
