package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.Serializable;

/**
 * Tests script engine output
 *
 * @author Activeeon Team
 */
public class TestOutput {

    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        String stringToPrint = "Hello World";
        String rScript = "print('" + stringToPrint + "')\n";
        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert.assertTrue("R Script output is broken", res.getOutput().toString().contains(stringToPrint));
    }
}
