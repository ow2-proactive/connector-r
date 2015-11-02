package tests;

import org.junit.Test;
import org.ow2.pajri.PAJRIFactory;


/**
 * Explicit result test.
 *
 * @author Activeeon Team
 */
public class TestResultExplicit extends testabstract.TestResultExplicit {

    @Test
    public void test() throws Exception {
        super.test(PAJRIFactory.ENGINE_NAME);
    }
}
