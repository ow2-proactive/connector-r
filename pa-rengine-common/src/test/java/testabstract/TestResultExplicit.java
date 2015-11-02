package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.Serializable;

/**
 * Explicit result test.
 *
 * @author Activeeon Team
 */
public class TestResultExplicit {

    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String rScript = "rm(list = ls()); result=FALSE";

        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert
                .assertEquals(
                        "The result variable declared explicitely in the script is not used as the result of the script by the engine",
                        Boolean.FALSE, (Boolean) res.getResult());
    }
}
