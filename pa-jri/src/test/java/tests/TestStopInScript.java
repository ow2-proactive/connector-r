/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import org.junit.Test;
import org.ow2.pajri.PAJRIFactory;

/**
 * Tests that the message of stop() is printed in the error writer
 *
 * @author Activeeon Team
 */
public class TestStopInScript extends testabstract.TestStopInScript {

    @Test
    public void test() throws Exception {
        super.test(PAJRIFactory.ENGINE_NAME);
    }

}
