package testabstract;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

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

    private void executeScriptAndCheckResults(String engineName, Object results, double[] resValues, String rScript1) throws InvalidScriptException {
        Map<String, Object> aBindings = Collections.singletonMap(TaskScript.RESULTS_VARIABLE,
                results);

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
        public TaskId getTaskId() {
            return this.taskId;
        }

        @Override
        public boolean hadException() {
            return false;
        }

        @Override
        public Serializable value() throws Throwable {
            return this.value;
        }
    }
}
