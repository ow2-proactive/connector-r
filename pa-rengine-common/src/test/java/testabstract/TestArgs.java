package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Tests arguments of a script task.
 *
 * @author Activeeon Team
 */
public class TestArgs {

    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String[] args = {"one", "two", "three"};
        String rScript = "result = args[3];";

        Map<String, Object> aBindings = Collections.singletonMap(TaskScript.ARGUMENTS_NAME, (Object) args);
        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);

        Assert.assertEquals("The arguments are not transfered by the engine to the script", args[2],
                res.getResult());
    }
}
