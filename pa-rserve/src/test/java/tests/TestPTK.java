package tests;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.parserve.PARServeFactory;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.Serializable;


/**
 * Tests that the ProcessTreeKiller cookie appears in the RServe sessions.
 *
 * @author Activeeon Team
 */
public class TestPTK {

    @Test
    public void test() throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String rScript = "result = Sys.getenv('PROCESS_KILLER_COOKIE_RServe')";

        SimpleScript ss = new SimpleScript(rScript, PARServeFactory.ENGINE_NAME);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();
        System.out.println("Result : " + res.getResult());
        Assert.assertNotNull("The R session must have env variable cookie set",
                res.getResult());
    }
}
