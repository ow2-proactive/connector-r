package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Verifies that the localspace is set properly.
 *
 * @author Activeeon Team
 */
public class TestLocalSpace {

    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Path path = Paths.get(System.getProperty("java.io.tmpdir")).normalize();

        String rScript = "result=getwd();";

        Map<String, Object> aBindings =
                Collections.singletonMap(SchedulerConstants.DS_SCRATCH_BINDING_NAME, (Object)
                        path.toString());
        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);

        String resPath = (String) res.getResult();
        assertNotNull("No result from R script", resPath);
        Assert.assertEquals("R script working directory is incorrect", path.toString(),
                resPath.replace("/", File.separator));
    }
}
