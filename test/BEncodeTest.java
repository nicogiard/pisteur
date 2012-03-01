import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import org.junit.Test;
import play.test.UnitTest;
import utils.BEncode;

public class BEncodeTest extends UnitTest {
    @Test
    public void testSimpleNumber() {
        assertEquals("i4e", BEncode.encode(4));
    }

    @Test
    public void testZeroNumber() {
        assertEquals("i0e", 0);
    }

    @Test
    public void testNegativeNumber() {
        assertEquals("i-10e", BEncode.encode(-10));
    }

    @Test
    public void testLongNumber() {
        assertEquals("i1234567890123456789e", BEncode.encode(1234567890123456789L));
    }

    @Test
    public void testEmptyString() {
        assertEquals("0:", BEncode.encode(""));
    }

    @Test
    public void testSimpleString() {
        assertEquals("3:abc", BEncode.encode("abc"));
    }

    @Test
    public void testNumberString() {
        assertEquals("10:1234567890", BEncode.encode("1234567890"));
    }

    @Test
    public void testEmptyList() {
        assertEquals("le", BEncode.encode(Lists.newArrayList()));
    }

    @Test
    public void testNumberList() {
        assertEquals("li1ei2ei3ee", BEncode.encode(Lists.newArrayList(1, 2, 3)));
    }

    @Test
    public void testMixedStringNumberList() {
        assertEquals("ll5:Alice3:Bobeli2ei3eee", BEncode.encode(Lists.newArrayList(Lists.newArrayList("Alice", "Bob"), Lists.newArrayList(2, 3))));
    }

    @Test
    public void testEmptyDictionary() {
        JsonObject d = new JsonObject();
        assertEquals("de", BEncode.encode(d));
    }

    @Test
    public void testSimpleDictionary() {
        JsonObject d = new JsonObject();
        d.addProperty("age", 25);
        d.addProperty("eyes", "blue");
        assertEquals("d3:agei25e4:eyes4:bluee", BEncode.encode(d));
    }

    @Test
    public void testComplexDictionary() {
        JsonObject d = new JsonObject();
        JsonObject spamMp3 = new JsonObject();
        spamMp3.addProperty("author", "Alice");
        spamMp3.addProperty("length", 100000);
        d.add("spam.mp3", spamMp3);
        assertEquals("d8:spam.mp3d6:author5:Alice6:lengthi100000eee", BEncode.encode(d));
    }
}
