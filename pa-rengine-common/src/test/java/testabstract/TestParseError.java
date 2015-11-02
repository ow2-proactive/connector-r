package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.Serializable;

/**
 * Tests that an invalid script throws an exception
 *
 * @author Activeeon Team
 */
public class TestParseError {


    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String rScript = "print('invalid'";
        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();
        Throwable t = res.getException();
        org.junit.Assert.assertNotNull("An invalid script must generate an exception", t);
        System.out.println("Script Exception:");
        t.printStackTrace();
        org.junit.Assert.assertTrue("The exception message must contain parse error", t.getMessage().contains("print('invalid'"));
    }

}
