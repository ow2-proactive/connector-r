/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package testabstract;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.scripting.helper.progress.ProgressFile;


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

        String progressFilePath = tmpFolder.newFile().getAbsolutePath();
        Map<String, Object> aBindings = new HashMap<String, Object>();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(SchedulerVars.PA_TASK_PROGRESS_FILE.toString(), (Object) progressFilePath);
        aBindings.put(SchedulerConstants.VARIABLES_BINDING_NAME, variables);
        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ScriptResult<Serializable> res = taskScript.execute(aBindings,
                                                            new PrintStream(output),
                                                            new PrintStream(output));

        System.out.println("Script output:");
        System.out.println(output);

        assertEquals("The progress is incorrect, it seems the engine doesn't transmit " +
                     " the progress to the script as expected",
                     expectedProgress,
                     ProgressFile.getProgress(progressFilePath));
    }
}
