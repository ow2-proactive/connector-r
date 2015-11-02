/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.google.common.io.CharStreams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.parserve.PARServeEngine;
import org.ow2.parserve.PARServeFactory;

import javax.script.ScriptContext;
import java.io.Writer;

/**
 * Tests that an invalid script throws an exception
 *
 * @author Activeeon Team
 */
public class TestParseError extends testabstract.TestParseError {

    Writer disabledErrorWriter;

    @Before
    public void disableErrorWriter() {
        ScriptContext sc = ((PARServeEngine) (new PARServeFactory()).getScriptEngine()).getContext();
        disabledErrorWriter = sc.getErrorWriter();
        sc.setErrorWriter(CharStreams.nullWriter());
    }

    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }

    @After
    public void renableErrorWriter() {
        ScriptContext sc = ((PARServeEngine) (new PARServeFactory()).getScriptEngine()).getContext();
        sc.setErrorWriter(disabledErrorWriter);
    }

}
