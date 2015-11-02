package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;

/**
 * Tests that the message of warning() is printed in the error writer
 *
 * @author Activeeon Team
 */
public class TestWarningInScript {

    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String warnMessage = "warn";
        String rScript = "result=FALSE;warning('" + warnMessage + "');";
        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ScriptResult<Serializable> res = taskScript.execute(Collections.<String, Object>emptyMap(),
                new PrintStream(output), new PrintStream(output));

        System.out.println("Script output :");
        System.out.println(output);

        // Check the customErrorWriter contains the message
        String errOutput = output.toString();
        Assert.assertTrue("The warning message must appear in the error output",
                errOutput.contains(warnMessage));
        Assert.assertEquals("Even if the script contains a warning the result variable must be read",
                Boolean.FALSE, res.getResult());
    }
}
