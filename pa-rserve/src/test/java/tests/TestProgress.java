package tests;

import org.junit.Test;
import org.ow2.parserve.PARServeFactory;


/**
 * Test set_progress(x) method in R script.
 *
 * @author Activeeon Team
 */
public class TestProgress extends testabstract.TestProgress {

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }
}
