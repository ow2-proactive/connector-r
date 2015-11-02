package tests;

import org.junit.Test;
import org.ow2.parserve.PARServeFactory;


/**
 * Tests R homedir detection from Windows registry.
 *
 * @author Activeeon Team
 */
public class TestDetectFromRegistry extends testabstract.TestDetectFromRegistry {

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }
}
