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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


/**
 * Tests that the message of warning() is printed in the error writer
 *
 * @author Activeeon Team
 */
public class TestWarningInScript {

    public void test(String engineName) throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String warnMessage = "warn";
        String rScript = "result=FALSE;warning('" + warnMessage + "');";
        SimpleScript ss = new SimpleScript(rScript, engineName);
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ScriptResult<Serializable> res = taskScript.execute(Collections.<String, Object> emptyMap(),
                                                            new PrintStream(output),
                                                            new PrintStream(output));

        System.out.println("Script output :");
        System.out.println(output);

        // Check the customErrorWriter contains the message
        String errOutput = output.toString();
        Assert.assertTrue("The warning message must appear in the error output", errOutput.contains(warnMessage));
        Assert.assertEquals("Even if the script contains a warning the result variable must be read",
                            Boolean.FALSE,
                            res.getResult());
    }
}
