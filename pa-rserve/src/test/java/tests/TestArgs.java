package tests;

import org.junit.Test;
import org.ow2.parserve.PARServeFactory;


/**
 * Tests arguments of a script task.
 *
 * @author Activeeon Team
 */
public class TestArgs extends testabstract.TestArgs {

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }
}
