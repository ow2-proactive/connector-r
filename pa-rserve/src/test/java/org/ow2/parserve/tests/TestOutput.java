package org.ow2.parserve.tests;

import org.junit.Test;
import org.ow2.parserve.PARServeFactory;


/**
 * Tests script engine output
 *
 * @author Activeeon Team
 */
public class TestOutput extends testabstract.TestOutput {

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }
}
