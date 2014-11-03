package tests;

import java.io.Serializable;

import org.ow2.parscript.PARScriptFactory;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;


/**
 * Basic PARScript tests.
 *
 * @author Activeeon Team
 */
public class TestOutput {

    @Test
    public void test() throws Exception {
        String stringToPrint = "Hello World";
        String rScript = "print('" + stringToPrint + "')";
        SimpleScript ss = new SimpleScript(rScript, PARScriptFactory.ENGINE_NAME);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();
        org.junit.Assert.assertTrue("R Script output is broken", res.getOutput().contains(stringToPrint));
    }
}
