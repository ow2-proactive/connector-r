/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import javax.script.ScriptContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.parscript.PARScriptEngine;
import org.ow2.parscript.PARScriptFactory;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

/**
 * Tests that the message of warning() is printed in the error writer
 *
 * @author Activeeon Team
 */
public class TestWarningInScript {

    Writer disabledErrorWriter;
    StringWriter customErrWriter;

    @Before
    public void disableErrorWriter() {
        ScriptContext sc = ((PARScriptEngine) (new PARScriptFactory()).getScriptEngine()).getContext();
        disabledErrorWriter = sc.getErrorWriter();
        customErrWriter = new StringWriter();
        sc.setErrorWriter(customErrWriter);
    }

    @Test
    public void test() throws Exception {
        String warnMessage = "warn";
        String rScript = "result=FALSE;warning('" + warnMessage + "');";
        SimpleScript ss = new SimpleScript(rScript, PARScriptFactory.ENGINE_NAME);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();

        // Check the customErrorWriter contains the message
        String errOutput = customErrWriter.toString();
        Assert.assertTrue("The warning message must appear in the error output", errOutput.contains(warnMessage));
        Assert.assertEquals("Even if the script contains a warning the result variable must be read", Boolean.FALSE, (Boolean) res.getResult());
    }

    @After
    public void renableErrorWriter() {
        ScriptContext sc = ((PARScriptEngine) (new PARScriptFactory()).getScriptEngine()).getContext();
        sc.setErrorWriter(disabledErrorWriter);
    }
}
