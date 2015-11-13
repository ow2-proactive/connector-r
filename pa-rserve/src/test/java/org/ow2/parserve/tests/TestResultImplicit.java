package org.ow2.parserve.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ow2.parserve.PARServeFactory;


/**
 * Implicit result test. The result of the script is the last valid expression.
 *
 * @author Activeeon Team
 */
@RunWith(JUnit4.class)
public class TestResultImplicit extends testabstract.TestResultImplicit {

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }
}
