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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


public class TestOtherResults {

    public void testSelection(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Map<String, Object> aBindings = new HashMap<>();

        String selectionRScript = "selected=FALSE";

        SelectionScript selectionScript = new SelectionScript(selectionRScript, engineName, true);
        ScriptResult<Boolean> res = selectionScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert.assertEquals("The result should be false", false, res.getResult());

    }

    public void testLoop(String engineName) throws Exception {
        String loopRScript = "loop='* * * * *'";

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Map<String, Object> aBindings = new HashMap<>();

        FlowScript loopScript = FlowScript.createLoopFlowScript(loopRScript, engineName, "aTarget");
        ScriptResult<FlowAction> res = loopScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert.assertEquals("The result should contain the loop decision",
                                      FlowActionType.LOOP,
                                      res.getResult().getType());

        org.junit.Assert.assertEquals("The result should contain the cron expression",
                                      "* * * * *",
                                      res.getResult().getCronExpr());

        loopRScript = "loop=TRUE";

        loopScript = FlowScript.createLoopFlowScript(loopRScript, engineName, "aTarget");
        res = loopScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert.assertEquals("The result should contain the loop decision",
                                      FlowActionType.LOOP,
                                      res.getResult().getType());

        org.junit.Assert.assertEquals("The result should contain the loop decision",
                                      "aTarget",
                                      res.getResult().getTarget());

    }

    public void testReplicate(String engineName) throws Exception {
        String replicateRScript = "runs=2";

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Map<String, Object> aBindings = new HashMap<>();

        FlowScript replicateScript = FlowScript.createReplicateFlowScript(replicateRScript, engineName);
        ScriptResult<FlowAction> res = replicateScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert.assertEquals("The result should contain the replicate runs",
                                      2,
                                      res.getResult().getDupNumber());

    }

    public void testBranch(String engineName) throws Exception {
        String branchRScript = "branch='if';";

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        Map<String, Object> aBindings = new HashMap<>();

        FlowScript loopScript = FlowScript.createIfFlowScript(branchRScript,
                                                              engineName,
                                                              "ifTarget",
                                                              "elseTarget",
                                                              "continuationTarget");
        ScriptResult<FlowAction> res = loopScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert.assertEquals("The result should contain the branch decision",
                                      FlowActionType.IF,
                                      res.getResult().getType());

        org.junit.Assert.assertEquals("The result should contain the if target",
                                      "ifTarget",
                                      res.getResult().getTarget());

    }
}
