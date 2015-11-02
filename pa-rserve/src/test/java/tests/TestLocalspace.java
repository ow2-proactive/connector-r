package tests;

import org.junit.Test;
import org.ow2.parserve.PARServeFactory;
import testabstract.TestLocalSpace;


/**
 * Verifies that the localspace is set properly.
 *
 * @author Activeeon Team
 */
public class TestLocalspace extends TestLocalSpace {

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }

}
