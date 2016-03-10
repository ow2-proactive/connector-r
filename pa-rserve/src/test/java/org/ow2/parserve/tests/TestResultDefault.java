package org.ow2.parserve.tests;

import org.junit.Test;
import org.ow2.parserve.PARServeFactory;


/**
 * Implicit result test. The default value of an empty script is always true.
 *
 * @author Activeeon Team
 */
public class TestResultDefault extends testabstract.TestResultDefault {

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }
}
