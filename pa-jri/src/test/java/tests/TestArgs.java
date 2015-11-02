package tests;

import org.junit.Test;
import org.ow2.pajri.PAJRIFactory;


/**
 * Tests arguments of a script task.
 *
 * @author Activeeon Team
 */
public class TestArgs extends testabstract.TestArgs {

    @Test
    public void test() throws Exception {
        super.test(PAJRIFactory.ENGINE_NAME);
    }
}
