package org.ow2.pajri.tests;

import org.junit.Test;
import org.ow2.pajri.PAJRIFactory;


/**
 * Implicit result test. The default value of an empty script is always true.
 *
 * @author Activeeon Team
 */
public class TestResultDefault extends testabstract.TestResultDefault {

    @Test
    public void test() throws Exception {
        super.test(PAJRIFactory.ENGINE_NAME);
    }
}
