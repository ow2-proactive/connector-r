package tests;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.ow2.parscript.PARScriptFactory;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;


/**
 * Test set_progress(x) method in R script.
 *
 * @author Activeeon Team
 */
public class TestProgress {

    @Test
    public void test() throws Exception {
        int expectedProgress = 50;
        String rScript = "set_progress(" + expectedProgress + "); zebi=10;";

        AtomicInteger progress = new AtomicInteger();
        Map<String, Object> aBindings = Collections.singletonMap(TaskScript.PROGRESS_VARIABLE,
                (Object) progress);
        SimpleScript ss = new SimpleScript(rScript, PARScriptFactory.ENGINE_NAME);
        TaskScript taskScript = new TaskScript(ss);
        /* ScriptResult<Serializable> res = */taskScript.execute(aBindings);

        org.junit.Assert.assertEquals("The progress is incorrect, it seems the engine doesn't transmit "
            + " the progress to the script as expected", expectedProgress, progress.intValue());
    }
}
