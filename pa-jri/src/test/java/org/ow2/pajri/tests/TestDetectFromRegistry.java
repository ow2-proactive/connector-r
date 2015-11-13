package org.ow2.pajri.tests;

import org.junit.Test;
import org.ow2.pajri.PAJRIFactory;


/**
 * Tests R homedir detection from Windows registry.
 *
 * @author Activeeon Team
 */
public class TestDetectFromRegistry extends testabstract.TestDetectFromRegistry {


    @Test
    public void test() throws Exception {
        super.test(PAJRIFactory.ENGINE_NAME);
    }
}
