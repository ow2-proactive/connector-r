package org.ow2.parserve.tests;

import org.junit.Test;
import org.ow2.parserve.PARServeFactory;

/**
 * Tests that a R error produces a faulty task
 *
 * @author Activeeon Team
 */
public class TestError extends testabstract.TestError {

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }

}
