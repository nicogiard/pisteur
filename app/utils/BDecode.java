package utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class BDecode {

    public static Object decode(String x) throws Exception {
        if (x.startsWith("l")) {
            return decodeList(x);
        } else if (x.startsWith("d")) {
            return decodeDictionary(x);
        } else if (x.startsWith("i")) {
            return decodeNumber(x);
        } else if (x.startsWith("0") || x.startsWith("1") || x.startsWith("2") || x.startsWith("3") || x.startsWith("4") || x.startsWith("5") || x.startsWith("6") || x.startsWith("7") || x.startsWith("8") || x.startsWith("9")) {
            return decodeString(x);
        }
        return "";
    }

    private static Object decodeNumber(String x) throws Exception {
        String number = x.substring(1, x.indexOf("e"));
        if (StringUtils.isBlank(number)) {
            // traiter l'erreur
        } else {
            try {
                Integer result = Integer.valueOf(number);
                return result;
            } catch (NumberFormatException nfe) {
                // Traiter l'erreur
            }
        }
        return null;
    }

    private static Object decodeString(String x) {
        int length = Integer.valueOf(x.substring(0, x.indexOf(":")));
        System.out.println(length);
        String result = x.substring(x.indexOf(":") + 1, x.indexOf(":") + 1 + length);
        System.out.println(result);
        return null;
    }

    private static Object decodeList(String x) {
        return null;
    }

    private static Object decodeDictionary(String x) {
        return null;
    }

    public static void main(String[] args) {
        try {
            System.out.println("ie -> " + BDecode.decode("ie"));
            System.out.println("i341foo382e -> " + BDecode.decode("i341foo382e"));
            System.out.println("i4e -> " + BDecode.decode("i4e"));
            System.out.println("i0e -> " + BDecode.decode("i0e"));
            System.out.println("i123456789e -> " + BDecode.decode("i123456789e"));
            System.out.println("i-10e -> " + BDecode.decode("i-10e"));
            //System.out.println("0:0: -> " + BDecode.decode("0:0:"));
            System.out.println("3:abc -> " + BDecode.decode("3:abc"));
            System.out.println("10:abcdefghij -> " + BDecode.decode("10:abcdefghij"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
