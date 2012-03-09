import org.junit.Test;

import play.test.UnitTest;
import utils.BEncode;
import utils.Utils;


public class UtilsTest extends UnitTest {
	@Test
    public void testByteMultipleSize() {
        assertEquals("1Ko", Utils.byteMultipleSize(1000));        
        assertEquals("1.2Ko", Utils.byteMultipleSize(1200));
        assertEquals("1Mo", Utils.byteMultipleSize(1000000));
        assertEquals("1Go", Utils.byteMultipleSize(1000000000));
        assertEquals("1To", Utils.byteMultipleSize(new Long((long)Math.pow(10, 12))));
    }
}
