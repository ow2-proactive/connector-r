package org.ow2.pajri.tests;

import org.junit.Test;
import org.ow2.pajri.PAJRIFactory;
import testabstract.TestLocalSpace;


/**
 * Verifies that the localspace is set properly.
 *
 * @author Activeeon Team
 */
public class TestLocalspace extends TestLocalSpace {

    @Test
    public void test() throws Exception {
        super.test(PAJRIFactory.ENGINE_NAME);
    }

}
