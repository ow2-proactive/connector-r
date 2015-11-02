package tests;

import org.junit.Test;
import org.ow2.parserve.PARServeFactory;


/**
 * Explicit result test.
 *
 * @author Activeeon Team
 */
public class TestResultExplicit extends testabstract.TestResultExplicit {

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }
}
