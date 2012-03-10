import org.junit.Test;

import play.test.UnitTest;
import utils.BEncode;
import utils.Utils;


public class UtilsTest extends UnitTest {
	@Test
    public void testByteMultipleSize() {
        assertEquals("1Ko", Utils.byteMultipleSize(1024));
        assertEquals("1Mo", Utils.byteMultipleSize(1048576));
        assertEquals("1,1Mo", Utils.byteMultipleSize(1150976));
        assertEquals("1,01Mo", Utils.byteMultipleSize(1058816));
        assertEquals("1Go", Utils.byteMultipleSize(1073741824));
        assertEquals("1To", Utils.byteMultipleSize(new Long((long)Math.pow(2, 40))));
    }
}
