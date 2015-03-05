/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;

import org.ow2.parscript.PARScriptFactory;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests that the message of warning() is printed in the error writer
 *
 * @author Activeeon Team
 */
public class TestWarningInScript {

    @Test
    public void test() throws Exception {
        String warnMessage = "warn";
        String rScript = "result=FALSE;warning('" + warnMessage + "');";
        SimpleScript ss = new SimpleScript(rScript, PARScriptFactory.ENGINE_NAME);
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ScriptResult<Serializable> res = taskScript.execute(Collections.<String, Object> emptyMap(),
                System.out, new PrintStream(output));

        // Check the customErrorWriter contains the message
        String errOutput = output.toString();
        Assert.assertTrue("The warning message must appear in the error output",
                errOutput.contains(warnMessage));
        Assert.assertEquals("Even if the script contains a warning the result variable must be read",
                Boolean.FALSE, res.getResult());
    }

}
