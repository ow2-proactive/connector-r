package org.ow2.pajri.tests;

import org.junit.Test;
import org.ow2.pajri.PAJRIFactory;


/**
 * Tests script engine output
 *
 * @author Activeeon Team
 */
public class TestOutput extends testabstract.TestOutput {

    @Test
    public void test() throws Exception {
        super.test(PAJRIFactory.ENGINE_NAME);
    }
}
