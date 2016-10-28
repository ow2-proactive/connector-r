/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package testabstract;

import org.apache.log4j.BasicConfigurator;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestResultMetadata {

    public void testEmptyMetadata(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        HashMap<String, String> metadata = new HashMap();

        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.RESULT_METADATA_VARIABLE,
                (Object) metadata);

        String rScript = "resultMetadata[['b']]='valueb'; result = TRUE";

        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);
        org.junit.Assert
                .assertEquals(
                        "The result should be true",
                        true, res.getResult());
        org.junit.Assert
                .assertEquals("The metadata map should contain the metadata defined in the script", "valueb", metadata.get("b"));
    }


    public void testMetadata(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        HashMap<String, String> metadata = new HashMap();
        metadata.put("a", "valuea");

        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.RESULT_METADATA_VARIABLE,
                (Object) metadata);


        String rScript = "resultMetadata[['b']]='valueb'; result = resultMetadata[['a']]";

        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert
                .assertEquals(
                        "The result should contain the returned metadata value",
                        "valuea", res.getResult());
        org.junit.Assert
                .assertEquals("The metadata map should contain the metadata defined in the script", "valueb", metadata.get("b"));
    }
}
