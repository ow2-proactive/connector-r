/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.google.common.io.CharStreams;
import java.io.Serializable;
import java.io.Writer;
import javax.script.ScriptContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.parscript.PARScriptEngine;
import org.ow2.parscript.PARScriptFactory;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

/**
 * Tests that an invalid script throws an exception
 *
 * @author Activeeon Team
 */
public class TestParseError {

    Writer disabledErrorWriter;

    @Before
    public void disableErrorWriter() {
        ScriptContext sc = ((PARScriptEngine) (new PARScriptFactory()).getScriptEngine()).getContext();
        disabledErrorWriter = sc.getErrorWriter();
        sc.setErrorWriter(CharStreams.nullWriter());
    }

    @Test
    public void test() throws Exception {
        String rScript = "print('invalid'";
        SimpleScript ss = new SimpleScript(rScript, PARScriptFactory.ENGINE_NAME);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();
        Throwable t = res.getException();
        org.junit.Assert.assertNotNull("An invalid script must generate an exception", t);
        org.junit.Assert.assertTrue("The exception message must contain parse error", t.getMessage().contains("Parse error"));
    }

    @After
    public void renableErrorWriter() {
        ScriptContext sc = ((PARScriptEngine) (new PARScriptFactory()).getScriptEngine()).getContext();
        sc.setErrorWriter(disabledErrorWriter);
    }

}
