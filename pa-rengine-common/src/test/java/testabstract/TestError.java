package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
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
 * Tests that a R error produces a faulty task
 *
 * @author Activeeon Team
 */
public class TestError {

    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        HashMap<String, Serializable> variablesMap = new HashMap<String, Serializable>(1);
        variablesMap.put("toto", "badValue");

        String error = "g <- function(){x <- a}; g()";
        String messageAfter = "Must not";

        String expectedVariableValue = "goodValue";

        String rScript = "variables$toto='" + expectedVariableValue + "'\nwarning('attention')\n" + error + "\nprint(\"" + messageAfter + "\")\n";

        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.VARIABLES_BINDING_NAME, (Object) variablesMap);

        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ScriptResult<Serializable> res = taskScript.execute(aBindings,
                new PrintStream(output), new PrintStream(output));

        System.out.println("Script output :");
        System.out.println(output);

        Assert.assertNotNull("The script exception must not be null", res.getException());
        Assert.assertTrue("The script exception must contain the error statement", res.getException().getMessage().contains("'a'"));
        Assert.assertTrue("The script output must contain the error statement", output.toString().contains("'a'"));
        Assert.assertFalse("The script output must not contain the message after the error", output.toString().contains(messageAfter));
        Assert.assertEquals("Even if the script had an error, all changes in the variables map must be done", expectedVariableValue, variablesMap.get("toto"));

    }
}
