package org.ow2.pajri.tests;

import org.junit.Test;
import org.ow2.pajri.PAJRIFactory;

/**
 * Tests that a R error produces a faulty task
 *
 * @author Activeeon Team
 */
public class TestError extends testabstract.TestError {

    @Test
    public void test() throws Exception {
        super.test(PAJRIFactory.ENGINE_NAME);
    }

}
