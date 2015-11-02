package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Test set_progress(x) method in R script.
 *
 * @author Activeeon Team
 */
public class TestProgress {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        int expectedProgress = 50;
        String rScript = ".set_progress(" + expectedProgress + "); zebi=10;";

        AtomicInteger progress = new AtomicInteger();
        Map<String, Object> aBindings = Collections.singletonMap(TaskScript.PROGRESS_VARIABLE,
                (Object) progress);
        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        taskScript.execute(aBindings, System.out, System.err);

        org.junit.Assert.assertEquals("The progress is incorrect, it seems the engine doesn't transmit "
                + " the progress to the script as expected", expectedProgress, progress.intValue());
    }
}
