package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.ow2.parengine.PAREngine;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests that the message of stop() is printed in the error writer
 *
 * @author Activeeon Team
 */
public class TestStopInScript {

    public void test(String engineName) throws Exception {

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        HashMap<String, Serializable> variablesMap = new HashMap<String, Serializable>(1);
        variablesMap.put("toto", "badValue");

        String errorMessage = "bad input";
        String messageAfter = "Must not";
        String expectedVariableValue = "goodValue";

        String rScript = "variables$toto='" + expectedVariableValue + "'\nwarning('attention')\nstop('" + errorMessage + "')\nprint(\"" + messageAfter + "\")\n";

        Map<String, Object> aBindings = Collections.singletonMap(PAREngine.TASK_SCRIPT_VARIABLES, (Object) variablesMap);

        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ScriptResult<Serializable> res = taskScript.execute(aBindings,
                new PrintStream(output), new PrintStream(output));

        System.out.println("Script output :");
        System.out.println(output);

        Assert.assertNotNull("The script exception must not be null", res.getException());
        Assert.assertTrue("The script exception must contain the message of stop() function", res.getException().getMessage().contains(errorMessage));
        Assert.assertTrue("The script output must contain the stop message", output.toString().contains(errorMessage));
        Assert.assertEquals("Even if the script is stoppped with stop() all changes in the variables map must be done", expectedVariableValue, variablesMap.get("toto"));

    }
}
