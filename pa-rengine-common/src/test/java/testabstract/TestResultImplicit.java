package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.Serializable;

/**
 * Implicit result test. The result of the script is the last valid expression.
 *
 * @author Activeeon Team
 */
public class TestResultImplicit {

    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        double expectedResult = 1d;
        String rScript = "rm(list = ls()); \n v=" + expectedResult;

        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();

        org.junit.Assert.assertEquals("The implicit result is not used by the engine",
                (Double) expectedResult, (Double) res.getResult());
    }
}
