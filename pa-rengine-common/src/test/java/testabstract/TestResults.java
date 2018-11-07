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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


/**
 * Tests for results from previous tasks. In the R script the variable 'results'
 * will be a map with the taskname as key and the value being the result of
 * {@link TaskResult#value()}.
 *
 * @author Activeeon Team
 */
public class TestResults {

    public void test(String engineName) throws Exception {
        // Results from hypothetical previous tasks

        int NB_TASKS = 10;

        String[] taskNames = new String[NB_TASKS];
        TaskResult[] results = new TaskResult[NB_TASKS];
        double[] resValues = new double[NB_TASKS];
        String rScript1 = "result=c(";
        String rScript2 = "result=c(";

        for (int i = 1; i <= NB_TASKS; i++) {
            taskNames[i - 1] = "task" + i;
            resValues[i - 1] = i;
            TaskId id = new MockedTaskId(taskNames[i - 1]);
            results[i - 1] = new MockedTaskResult(id, resValues[i - 1]);
            rScript1 += "results[['" + taskNames[i - 1] + "']]" + (i < NB_TASKS ? "," : "");
            rScript2 += "results[[" + i + "]]" + (i < NB_TASKS ? "," : "");
        }
        rScript1 += ")";
        rScript2 += ")";

        executeScriptAndCheckResults(engineName, results, resValues, rScript1);

        executeScriptAndCheckResults(engineName, results, resValues, rScript2);
    }

    private void executeScriptAndCheckResults(String engineName, Object results, double[] resValues, String rScript1)
            throws InvalidScriptException {
        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.RESULTS_VARIABLE, results);

        SimpleScript ss = new SimpleScript(rScript1, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);

        Serializable value = res.getResult();
        org.junit.Assert.assertTrue("Invalid result type of the R script", value instanceof double[]);
        org.junit.Assert.assertArrayEquals(resValues, (double[]) res.getResult(), 0);
    }

    final class MockedTaskId implements TaskId {
        private String name;

        public MockedTaskId(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(TaskId o) {
            return 0;
        }

        @Override
        public int getIterationIndex() {
            return 0;
        }

        @Override
        public JobId getJobId() {
            return null;
        }

        @Override
        public String getReadableName() {
            return this.name;
        }

        @Override
        public int getReplicationIndex() {
            return 0;
        }

        @Override
        public String value() {
            return this.name;
        }

        @Override
        public long longValue() {
            return 0;
        }

        public String getTag() {
            return "";
        }
    }

    final class MockedTaskResult implements TaskResult {
        private TaskId taskId;

        private Serializable value;

        public MockedTaskResult(TaskId taskId, Serializable value) {
            this.taskId = taskId;
            this.value = value;
        }

        @Override
        public FlowAction getAction() {
            return null;
        }

        @Override
        public Throwable getException() {
            return null;
        }

        @Override
        public TaskLogs getOutput() {
            return null;
        }

        @Override
        public Map<String, byte[]> getPropagatedVariables() {
            return null;
        }

        @Override
        public byte[] getSerializedValue() {
            return null;
        }

        @Override
        public Map<String, String> getMetadata() {
            return null;
        }

        @Override
        public TaskId getTaskId() {
            return this.taskId;
        }

        @Override
        public boolean hadException() {
            return false;
        }

        public boolean isRaw() {
            return false;
        }

        @Override
        public Map<String, Serializable> getResultMap() {
            return null;
        }

        public Map<String, Serializable> getVariables() {
            return null;
        }

        public Serializable getValue() {
            return value;
        }

        @Override
        public Serializable value() throws Throwable {
            return this.value;
        }
    }
}
