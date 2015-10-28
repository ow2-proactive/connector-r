package tests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.parscript.PARScriptEngine;
import org.ow2.parscript.PARScriptFactory;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.scripting.helper.progress.ProgressFile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * Test set_progress(x) method in R script.
 *
 * @author Activeeon Team
 */
public class TestProgress {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void test() throws Exception {
        int expectedProgress = 50;
        String rScript = "set_progress(" + expectedProgress + "); zebi=10;";

        String progressFilePath = tmpFolder.newFile().getAbsolutePath();
        Map<String, Object> aBindings = new HashMap<String, Object>();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(SchedulerVars.PA_TASK_PROGRESS_FILE.toString(), (Object) progressFilePath);
        aBindings.put(PARScriptEngine.TASK_SCRIPT_VARIABLES, variables);
        SimpleScript ss = new SimpleScript(rScript, PARScriptFactory.ENGINE_NAME);
        TaskScript taskScript = new TaskScript(ss);
        taskScript.execute(aBindings, System.out, System.err);

        assertEquals("The progress is incorrect, it seems the engine doesn't transmit " +
          " the progress to the script as expected", expectedProgress, ProgressFile.getProgress(progressFilePath));
    }
}
