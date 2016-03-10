package org.ow2.parserve.tests;

import org.junit.Test;
import org.ow2.parserve.PARServeFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * Tests for results from previous tasks. In the R script the variable 'results'
 * will be a map with the taskname as key and the value being the result of
 * {@link TaskResult#value()}.
 *
 * @author Activeeon Team
 */
public class TestResults extends testabstract.TestResults {
    @Test
    public void test() throws Exception {
        super.test(PARServeFactory.ENGINE_NAME);
    }
}
